package mercariapi;

import play.*;
import play.mvc.*;
import java.util.*;

import views.html.*;
import java.io.*;
import java.net.URL;
import java.net.URI;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.nio.charset.Charset;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.net.URLDecoder;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.params.HttpParams;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

public class MercariSearcher{
    private final String USER_AGENT = "Mercari_r/511 (Android 23; ja; arm64-v8a,; samsung SC-02H Build/6.0.1)";
    private final String XPLATFORM = "android";
    private final String XAPPVERSION = "511";
    //private final String ACCEPTENCODING = "deflate,gzip";

    //GET,POSTのRequestのResponse
    private class MercariRawResponse{
        public Boolean error = true;
        public String response = "";
    }
    
    //get_items でのOption
    private class GetItemsOption{
        public String sellerid = "";
        public List<Integer> status_list = new ArrayList<Integer>();
		List<SimpleEntry<String,String>> ToPairList(){
			//空文字列あるいは空リストの場合そのオプションはなし
			List<SimpleEntry<String,String>> rst = new ArrayList<SimpleEntry<String,String>>();
			if(this.sellerid != "") rst.add(new SimpleEntry<String,String>("seller_id",this.sellerid));
			if(this.status_list.size() != 0) rst.add(new SimpleEntry<String,String>("status",MercariItem.ItemStatusListToString(this.status_list)));
			return rst;
		}
    }
    
    public String access_token;
    public String global_access_token;
    public String global_refresh_token; //未使用

	/*アクセストークンを使用してインスタンスを生成*/
	public MercariSearcher(String access_token,String global_access_token){
		this.access_token = access_token;
		this.global_access_token = global_access_token;
	}
    public MercariSearcher(){
        MercariAPIInitialize();
    }
    
    //UUIDを生成,アクセストークンを取得 返り値: 成功:true 失敗:false
    private Boolean MercariAPIInitialize(){
        Boolean rst = false;
        this.access_token = GetMercariAccessToken();
        List<String> global_tokens = GetMercariGlobalAccessToken();
        if(this.access_token != "" && global_tokens.size() == 2){
            this.global_access_token = global_tokens.get(0);
            this.global_refresh_token = global_tokens.get(1);
            rst = true;
        }
        return rst;
    }

	//ログインを試行し,グローバルアクセストークンを更新する.
	//返り値: 成功時:true 失敗 :false
	public Boolean tryMercariLogin(String email, String password){
		/*revert,android_id,device_id,app_generated_idなどのパラメタはなくてもOKなので送らない*/
		List<SimpleEntry<String,String>> param = new ArrayList<SimpleEntry<String,String>>();
		param.add(new SimpleEntry<String,String>("email",email));
		param.add(new SimpleEntry<String,String>("password",password));
		/*iv_certは恐らく招待コードを確認するためのもの.140文字のダミー文字列でOK*/
		String iv_cert = "";
		for(int i = 0; i < 140; i++) iv_cert += "a";
		param.add(new SimpleEntry<String,String>("iv_cert",iv_cert));
		String url = "https://api.mercari.jp/users/login?_access_token=" + this.access_token + "&_global_access_token=" + this.global_access_token;
		MercariRawResponse rawres = SendMercariAPIwithPOST(url, param);
		if(rawres.error) return false;
		try{
			/*グローバルアクセストークンを更新*/
			JSONObject resjson = new JSONObject(rawres.response);
			String new_global_access_token = resjson.getJSONObject("data").getString("global_access_token");
			this.global_access_token = new_global_access_token;
			Logger.info("ログイン成功");
			return true;
		}catch(Exception e){
			Logger.info("ログイン失敗");
			return false;
		}
	}

	private String getExhibitToken(){
		String uuid = UUID.randomUUID().toString();
		String[] uuidarray = uuid.split("");
		String rst = "";
		for(int i = 0; i < uuidarray.length; i++) if(!uuidarray[i].equals("-")) rst += uuidarray[i];
		return rst;
	}
	public File[] getFileObjectFromURL(String[] urlstr){
		File[] rstfile = new File[4];
		for(int i = 0; i < 4; i++){
			if(urlstr[i].equals("")) continue;//空文字列の場合は画像が存在しない
			try{
				URL url = new URL(urlstr[i]);
				URLConnection conn = url.openConnection();
				InputStream in = conn.getInputStream();
				
				File file = new File("tmp"+Integer.toString(i)+".jpg");
				FileOutputStream out = new FileOutputStream(file, false);
				int b;
				while((b = in.read()) != -1){
					out.write(b);
				}
				out.close();
				in.close();
				rstfile[i] = file;
			}catch(Exception e){
				Logger.info("出品時の画像の取得に失敗");
			}
		}
		return rstfile;
	}

	//商品の出品.要ログイン
	//返り値: 成功:新しい商品オブジェクト 失敗:null
	public MercariItem Sell(MercariItem item){
		try{
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpParams params = httpclient.getParams();

			String url = "https://api.mercari.jp/sellers/sell?_access_token=" + this.access_token + "&_global_access_token=" + this.global_access_token;
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("User-Agent",USER_AGENT);
			httpPost.setHeader("X-PLATFORM",XPLATFORM);
            httpPost.setHeader("X-APP-VERSION",XAPPVERSION); 

			File[] uploadFile = getFileObjectFromURL(item.imageurls);
			
			/*手数料を計算する*/
			Integer sales_fee = GetSalesFee(item.price, item.category_id);
			MultipartEntity reqEntity = new MultipartEntity();

			reqEntity.addPart("name",new StringBody(item.name,Charset.forName("UTF-8")));
			reqEntity.addPart("price",new StringBody(item.price.toString()));
			reqEntity.addPart("sales_fee",new StringBody(sales_fee.toString()));
			reqEntity.addPart("description",new StringBody(item.description,Charset.forName("UTF-8")));
			reqEntity.addPart("category_id",new StringBody(item.category_id.toString()));
			reqEntity.addPart("item_condition",new StringBody(item.item_condition.toString()));
			reqEntity.addPart("shipping_payer",new StringBody(item.shipping_payer.toString()));
			reqEntity.addPart("shipping_method",new StringBody(item.shipping_method.toString()));
			reqEntity.addPart("shipping_from_area",new StringBody(item.shipping_from_area.toString()));
			reqEntity.addPart("shipping_duration",new StringBody(item.shipping_duration.toString()));
			reqEntity.addPart("_ignore_warning",new StringBody("false"));
			reqEntity.addPart("exhibit_token",new StringBody(getExhibitToken()));
			reqEntity.addPart("pixel_ratio",new StringBody("4.0"));
			if(!item.imageurls[0].equals("")) reqEntity.addPart("photo_1", new FileBody(uploadFile[0]));
			if(!item.imageurls[1].equals("")) reqEntity.addPart("photo_2", new FileBody(uploadFile[1]));
			if(!item.imageurls[2].equals("")) reqEntity.addPart("photo_3", new FileBody(uploadFile[2]));
			if(!item.imageurls[3].equals("")) reqEntity.addPart("photo_4", new FileBody(uploadFile[3]));
			
			httpPost.setEntity(reqEntity);

			HttpResponse response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			JSONObject resjson = new JSONObject(responseString);
			MercariItem rstitem = new MercariItem(resjson.getJSONObject("data"));
			System.out.println(rstitem.name);
			return rstitem;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	//商品を削除する.要ログイン
	//返り値: 成功:true 失敗:false
	public Boolean Cancel(MercariItem item){
		try{
			String url = "https://api.mercari.jp/items/update_status?_access_token=" + this.access_token + "&_global_access_token=" + this.global_access_token;
			List<SimpleEntry<String,String>> param = new ArrayList<SimpleEntry<String,String>>();
			param.add(new SimpleEntry<String,String>("item_id",item.id));
			param.add(new SimpleEntry<String,String>("status","cancel"));
		
			MercariRawResponse rawres = SendMercariAPIwithPOST(url, param);
			if(rawres.error) return false;
			/*グローバルアクセストークンを更新*/
			JSONObject resjson = new JSONObject(rawres.response);
			String result = resjson.getString("result");
			if(!result.equals("OK")) throw new IllegalArgumentException();
			Logger.info("商品の削除に成功");
			return true;
		}catch(Exception e){
			Logger.info("商品の削除に失敗");
			return false;
		}
	}

	//商品を削除して出品する.要ログイン
	//返り値: 成功:新しい商品オブジェクト 失敗:null
	public MercariItem CancelandSell(String itemid){
		MercariItem item = GetItemInfobyItemID(itemid);
		Cancel(item); //削除失敗した場合も出品はする
		return Sell(item);
	}
	
	//最新の手数料のレートに応じて手数料を求める
	//手数料取得失敗時は負の値が返る
	public Integer GetSalesFee(Integer price,Integer category_id){
		try{
			List<SimpleEntry<String,String>> param = GetTokenParamListForMercariAPI();
			MercariRawResponse rawres = SendMercariAPIwithGET("https://api.mercari.jp/sales_fee/get",param);
			JSONObject resjson = new JSONObject(rawres.response);
			JSONArray sales_cond = resjson.getJSONObject("data").getJSONArray("parameters");
			/*カテゴリを再優先, 次に金額の条件を満たすか*/
			for(int i = 0; i < sales_cond.length(); i++){
				JSONObject cond = sales_cond.getJSONObject(i);
				/*カテゴリIDの条件があってそれを満たさない場合は次の条件へ*/
				if(cond.has("category_id") && cond.getInt("category_id") != category_id) continue;
				/*金額の条件を満たしていれば手数料決定*/
				Integer min_price = cond.isNull("min_price") ? -1 : cond.getInt("min_price");
				Integer max_price = cond.isNull("max_price") ? -1 : cond.getInt("max_price");
				Integer fixed_fee = cond.isNull("fixed_fee") ? 0 : cond.getInt("fixed_fee");
				if(min_price <= price && price <= max_price){
					/*手数料計算*/
					double rate = cond.isNull("rate") ? -1 : cond.getDouble("rate");
				    Integer fee = (int)Math.floor(rate * price) + fixed_fee;
					return fee;
				}
			}
			return -1; /*どの条件にもマッチしなかった*/
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
	}
	//特定のitemIDの商品情報を取得
	//このAPIではコメントなどの情報も取得できるが現時点では取り出していない
	public MercariItem GetItemInfobyItemID(String itemid){
		try{
			List<SimpleEntry<String,String>> param = GetTokenParamListForMercariAPI();
			param.add(new SimpleEntry<String,String>("id",itemid));
			MercariRawResponse rawres = SendMercariAPIwithGET("https://api.mercari.jp/items/get",param);
			//Logger.info(rawres.response);
			JSONObject resjson = new JSONObject(rawres.response);
		    JSONObject iteminfo = resjson.getJSONObject("data");
			MercariItem item = new MercariItem(iteminfo);
			return item;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
    //特定のsellerIDの商品をすべて取得
    //List<Integer> status_option := 商品の状態1:on_sale 2:trading 3:sold_out
    public List<MercariItem> GetAllItemsWithSellers(String sellerid,List<Integer> status_list){
		GetItemsOption option = new GetItemsOption();
		option.sellerid = sellerid;
		option.status_list = status_list;
        return GetItems(option);
    }
    //コンディションに応じて商品を取得する
    //一度のリクエストで取れるのは最大で60個 60個を超える場合は複数回APIを叩いて結果を取得する.
    private List<MercariItem> GetItems(GetItemsOption option){
        List<SimpleEntry<String,String>> default_param = GetTokenParamListForMercariAPI();
		default_param.addAll(option.ToPairList());
		default_param.add(new SimpleEntry<String,String>("limit","60"));
        //default_param.add(new SimpleEntry<String,String>("pixel_ratio","4.0"));
        List<MercariItem> res = new ArrayList<MercariItem>();

		//60個以上あるか
		Boolean has_next = false;
		//2回目以降でつかうmax_pager_id
	    String max_pager_id = "";
		do{
			List<SimpleEntry<String,String>> param = new ArrayList<SimpleEntry<String,String>>();
			param.addAll(default_param);
			 if(max_pager_id != "") param.add(new SimpleEntry<String,String>("max_pager_id",max_pager_id));
			MercariRawResponse rawres = SendMercariAPIwithGET("https://api.mercari.jp/items/get_items",param);

			if(rawres.error) return res;
			try{
				JSONObject resjson = new JSONObject(rawres.response);
				JSONArray datas = resjson.getJSONArray("data");
				has_next = resjson.getJSONObject("meta").getBoolean("has_next");
				//1件ずつデータとりだし
				for(int i = 0; i < datas.length(); i++){
					JSONObject iteminfo = datas.getJSONObject(i);
					MercariItem item = new MercariItem(iteminfo);
					res.add(item);
					max_pager_id = item.pager_id.toString(); //次のリクエストで使うためにmax_pager_id更新
				}
			}catch(Exception e){
				e.printStackTrace();
				return res;
			}
		}while(has_next == true);
        return res;
    }
	
    //アクセストークンを取得
    private String GetMercariAccessToken(){
        String uuid = UUID.randomUUID().toString();
        List<SimpleEntry<String,String>> param = new ArrayList<SimpleEntry<String,String>>();
        param.add(new SimpleEntry<String,String>("uuid",uuid));
        MercariRawResponse rawres= SendMercariAPIwithGET("https://api.mercari.jp/auth/create_token",param);

        String res = "";
        if(rawres.error) return res;
        try{
            JSONObject resjson = new JSONObject(rawres.response);
            res = resjson.getJSONObject("data").getString("access_token");
        }catch(Exception e){
            e.printStackTrace();
            Logger.info("Fail to get access token");
            return res;
        }
        Logger.info("Success to get access token");
        return res;
    }
    
    //グローバルアクセストークンの取得
    private List<String> GetMercariGlobalAccessToken(){
        String uuid = UUID.randomUUID().toString();
        List<SimpleEntry<String,String>> param = new ArrayList<SimpleEntry<String,String>>();
        param.add(new SimpleEntry<String,String>("_access_token",this.access_token));
        MercariRawResponse rawres= SendMercariAPIwithGET("https://api.mercari.jp//global_token/get",param);

        List<String> res = new ArrayList<String>();
        if(rawres.error) return res;
        JSONObject resjson = new JSONObject(rawres.response);
        try{
            res.add(resjson.getJSONObject("data").getString("global_access_token"));
            res.add(resjson.getJSONObject("data").getString("global_refresh_token"));
        }catch(Exception e){
            e.printStackTrace();
            Logger.info("Fail to get global access token");
            return res;
        }
        Logger.info("Success to get global access token");
        return res;
    }
    
    //メルカリのAPIをGETでたたく
    private MercariRawResponse SendMercariAPIwithGET(String url,List<SimpleEntry<String,String>> param){
        MercariRawResponse res = new MercariRawResponse();
        try{
            //パラメータをURLに付加 ?param1=val1&param2=val2...
            url += "?";
            List<String> paramstr = new ArrayList<String>();
            for (SimpleEntry p:param) {
				String k = URLEncoder.encode(p.getKey().toString(),"UTF-8");
				String v = URLEncoder.encode(p.getValue().toString(),"UTF-8");
                paramstr.add(k + "=" + v);
            }
            url += String.join("&",paramstr);
            
            URL obj = new URL(url);
                    
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            //パラメータ設定
            con.setRequestProperty("User-Agent",USER_AGENT);
            con.setRequestProperty("X-PLATFORM",XPLATFORM);
            con.setRequestProperty("X-APP-VERSION",XAPPVERSION); 
            //con.setRequestProperty("Accept-Encoding",ACCEPTENCODING);
            Logger.info(url);
            int responseCode = con.getResponseCode();
        
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            res.response = response.toString();
            res.error = false;
        }catch(FileNotFoundException e){
			//4xx 5xx error
			e.printStackTrace();
		}catch(Exception e){
            e.printStackTrace();
            Logger.info("error");
        }
        return res;
    }
    //メルカリのAPIをPOSTでたたく
    private MercariRawResponse SendMercariAPIwithPOST(String url,List<SimpleEntry<String,String>> param){
        //未実装
        MercariRawResponse res = new MercariRawResponse();
		try{
			URL obj = new URL(url);
            
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add reuqest header
			con.setRequestMethod("POST");
			//パラメータ設定
            con.setRequestProperty("User-Agent",USER_AGENT);
            con.setRequestProperty("X-PLATFORM",XPLATFORM);
            con.setRequestProperty("X-APP-VERSION",XAPPVERSION); 

			//パラメータ作成
			String urlParameters = "";
			List<String> paramstr = new ArrayList<String>();
            for (SimpleEntry p:param) {
				String k = URLEncoder.encode(p.getKey().toString(),"UTF-8");
				String v = URLEncoder.encode(p.getValue().toString(),"UTF-8");
                paramstr.add(k + "=" + v);
            }
            urlParameters = String.join("&",paramstr);
            
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
            res.response = response.toString();
            res.error = false;
		}catch(Exception e){
			e.printStackTrace();
		}
        return res;
    }
	
	//access_tokenとglobal_access_tokenのはいったListを返す関数
	private List<SimpleEntry<String,String>> GetTokenParamListForMercariAPI(){
		List<SimpleEntry<String,String>> param = new ArrayList<SimpleEntry<String,String>>();
        param.add(new SimpleEntry<String,String>("_access_token",this.access_token));
        param.add(new SimpleEntry<String,String>("_global_access_token",this.global_access_token));
		return param;
	}
	
}
