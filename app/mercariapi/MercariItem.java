package mercariapi;

import play.*;
import play.mvc.*;
import java.util.*;

import views.html.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.nio.charset.Charset;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class MercariItem{
	public String id;
	public MercariSeller seller;
	public String status; //on_sale trading sold_out
	public String name; //商品タイトル
	public Integer price;
	public String description;
	public Integer num_likes; //いいね数
	public Integer num_comments; //コメント数
	public Long updated; //更新日時UNIXタイムスタンプ10桁 たしか売れたら更新される コメントされたらではなさそう？
	public Long created; //作成日時UNIXタイムスタンプ10桁
	public Long pager_id; //商品ページのインデックス?,get_itemsで60件以上あるときは最後のitemのpager_idを使って2回目以降叩く
	//public Long item_pv; //なにこれ？？たぶんアクセス数
	public Integer shipping_from_area; //発送都道府県
	public ItemCategory category;
	public String[] imageurls = new String[4]; //画像URL

	/*出品用に必要な属性*/
	/*以下の属性はDetailをとったとき以外はnullになるので注意
	  MercariItemDetailクラスをつくるべき?*/
	public Integer size;
	public Integer brand_name;
	public Integer item_condition;
	public Integer shipping_payer;
	public Integer shipping_method;
	public Integer shipping_duration;
	/*出品に必要な属性ここまで*/
	
	public String updated_str; //Unixタイムスタンプを変換したもの
	public String created_str;
	
	public class ItemCategory{
		public Integer id;
		public String name; //フィルム
		public Integer display_order; //なにこれ
		public Integer parent_category_id;
		public String parent_category_name;//スマホアクセサリー
		public Integer root_category_id;
		public String root_category_name; //家電・スマホ・カメラ
		public ItemCategory(JSONObject json){
			this.id = getIntOrNull(json,"id");
			this.name = getStringOrNull(json, "name");
			this.display_order = getIntOrNull(json,"display_order");
			this.parent_category_id = getIntOrNull(json,"parent_category_id");
			this.parent_category_name = getStringOrNull(json, "parent_category_name");
			this.root_category_id = getIntOrNull(json,"root_category_id");
			this.root_category_name = getStringOrNull(json, "root_category_name");
		}
	}
	
	public MercariItem(JSONObject json){
		try{
			this.id = getStringOrNull(json, "id");
			this.seller = new MercariSeller(json.getJSONObject("seller"));
			this.status = getStringOrNull(json,"status");
			this.name = getStringOrNull(json, "name");
			this.price = getIntOrNull(json,"price");
			this.description = getStringOrNull(json, "description");
			this.num_likes = getIntOrNull(json,"num_likes");
			this.num_comments = getIntOrNull(json,"num_comments");
			this.updated = getLongOrNull(json,"updated");
			this.created = getLongOrNull(json,"created");
			this.pager_id = getLongOrNull(json,"pager_id");
			//this.item_pv = json.getLong("item_pv");
			this.shipping_from_area = json.getJSONObject("shipping_from_area").getInt("id");
		    this.category = new ItemCategory(json.getJSONObject("item_category"));
			this.imageurls = new String[4];
			JSONArray photos = json.getJSONArray("photos");
			for(int i = 0; i < this.imageurls.length; i++) this.imageurls[i] = "";
			for(int i = 0; i < Math.min(this.imageurls.length, photos.length()); i++){
				this.imageurls[i] = (photos.getString(i));
			}

			this.size = getIntOrNull(getJSONObjectOrNull(json,"item_size"),"id");
			this.brand_name = getIntOrNull(getJSONObjectOrNull(json,"item_brand"),"id");
			this.item_condition = getIntOrNull(getJSONObjectOrNull(json,"item_condition"),"id");
			this.shipping_payer = getIntOrNull(getJSONObjectOrNull(json,"shipping_payer"),"id");
			this.shipping_method = getIntOrNull(getJSONObjectOrNull(json,"shipping_method"),"id");
			this.shipping_duration = getIntOrNull(getJSONObjectOrNull(json,"shipping_duration"),"id");

			Date updated_date = new Date(this.updated * 1000);
			Date created_date = new Date(this.created * 1000);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			this.updated_str = sdf.format(updated_date.getTime()).toString();
			this.created_str = sdf.format(created_date.getTime()).toString();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//商品のstatusのリストからリクエスト用の文字列を作成する
	static String ItemStatusListToString(List<Integer> op){
		List<String> t = new ArrayList<String>();
		for(Integer o : op){
			if(o == 1) t.add("on_sale");
			if(o == 2) t.add("trading");
			if(o == 3) t.add("sold_out");
		}
		if(t.size() == 0) return "";
		else return String.join(",",t);
	}

	public String getStringOrNull(JSONObject json,String key){
		try{
			String rst = json.isNull(key) ? null : json.getString(key);
			return rst;
		}catch(Exception e){
			return null;
		}
	}
	public Long getLongOrNull(JSONObject json,String key){
		try{
			Long rst = json.isNull(key) ? null : json.getLong(key);
			return rst;
		}catch(Exception e){
			return null;
		}
	}
	public Integer getIntOrNull(JSONObject json,String key){
		try{
			Integer rst = json.isNull(key) ? null : json.getInt(key);
			return rst;
		}catch(Exception e){
			return null;
		}
	}
	public JSONObject getJSONObjectOrNull(JSONObject json,String key){
		try{
			JSONObject rst = json.isNull(key) ? null : json.getJSONObject(key);
			return rst;
		}catch(Exception e){
			return null;
		}
	}
}
