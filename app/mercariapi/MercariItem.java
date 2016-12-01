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
	public Long item_pv; //なにこれ？？たぶんアクセス数
	public String shipping_from_area; //発送都道府県
	public ItemCategory category;
	public List<String> imageurls; //画像URL

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
			this.id = json.getInt("id");
			this.name = json.getString("name");
			this.display_order = json.getInt("display_order");
			//this.parent_category_id = json.getInt("parent_category_id");
			//this.parent_category_name = json.getString("parent_category_name");
			this.root_category_id = json.getInt("root_category_id");
			this.root_category_name = json.getString("root_category_name");
		}
	}
	
	public MercariItem(JSONObject json){
		try{
			this.id = json.getString("id");
			this.seller = new MercariSeller(json.getJSONObject("seller"));
			this.status = json.getString("status");
			this.name = json.getString("name");
			this.price = json.getInt("price");
			this.description = json.getString("description");
			this.num_likes = json.getInt("num_likes");
			this.num_comments = json.getInt("num_comments");
			this.updated = json.getLong("updated");
			this.created = json.getLong("created");
			this.pager_id = json.getLong("pager_id");
			this.item_pv = json.getLong("item_pv");
			this.shipping_from_area = json.getJSONObject("shipping_from_area").getString("name");
		    this.category = new ItemCategory(json.getJSONObject("item_category"));
			this.imageurls = new ArrayList<String>();
			JSONArray thumburls = json.getJSONArray("thumbnails");
			for(int i = 0; i < thumburls.length(); i++){
				this.imageurls.add(thumburls.getString(i));
			}
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
		else return String.join("%2C",t); //%2Cは,のこと
	}
}
