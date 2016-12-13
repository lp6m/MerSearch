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
import java.io.FileNotFoundException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.net.URLDecoder;

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
    public String global_refresh_token;
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

	//特定のitemIDの商品情報を取得
	//このAPIではコメントなどの情報も取得できるが現時点では取り出していない
	public MercariItem GetItemInfobyItemID(String itemid){
		try{
			List<SimpleEntry<String,String>> param = GetTokenParamListForMercariAPI();
			param.add(new SimpleEntry<String,String>("id",itemid));
			MercariRawResponse rawres = SendMercariAPIwithGET("https://api.mercari.jp/items/get",param);
			System.out.println(rawres.response);
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
            System.out.println("Fail to get access token");
            return res;
        }
        System.out.println("Success to get access token");
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
            System.out.println("Fail to get global access token");
            return res;
        }
        System.out.println("Success to get global access token");
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
            System.out.println(url);
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
            System.out.println("error");
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
