package mercariapi;

import play.*;
import play.mvc.*;
import java.util.*;

import views.html.*;

import java.net.URL;
import java.net.HttpURLConnection;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.URLConnection;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;


//複数アカウント使用可能にするため,staticにはしていない
public class MercariExhibitter{

	private String cookiestr;
	static private String content_type = "application/x-www-form-urlencoded; charset=UTF-8";
	static private String accept_encoding = "UTF-8";
	static private String accept = "application/json";
	static private String user_agent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36";

    
	public MercariExhibitter(String PHPSESSID){
		cookiestr = "G_ENABLED_IDPS=google; PHPSESSID=" + PHPSESSID;
	}
	
	/*商品の削除と再出品を行う*/
	public MercariItem SellandCancel(MercariItem item){
		MercariExhibitItem new_item = new MercariExhibitItem(item);
		Cancel(item);
		return Sell(new_item);
	}
	/*商品の出品 商品情報を返す*/
	public MercariItem Sell(MercariExhibitItem item){
		try{
			/*パラメータの用意*/
			List<SimpleEntry<String,String>> param = item.toParamList();
			param.add(GetCSRFParam());
			param.add(GetExhibitParam());
			String res = SendPostMercariform("https://www.mercari.com/jp/sell/selling/",param);
			/*itemidの取り出し*/
			JSONObject resjson = new JSONObject(res);
			String itemid = resjson.getJSONObject("item").getString("id");
			/*出品したあと出品IDから商品情報をとってきて返す*/
			MercariSearcher s = new MercariSearcher();
			return s.GetItemInfobyItemID(itemid);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("出品に失敗しました");
			return null;
		}
	}
	/*商品の削除*/
	public Boolean Cancel(String itemID){
		try{
			SendPostMercariform("https://www.mercari.com/jp/items/cancel/" + itemID + "/",Arrays.asList(GetCSRFParam()));
			/*TODO:商品ページに実際にアクセスして消えていればOK*/
			return true;
		}catch(Exception e){
			System.out.println("商品の削除に失敗しました");
			return false;
		}
	}
	public Boolean Cancel(MercariItem item){
		return Cancel(item.id);
	}
	/*商品出品ページのHTMLからCSRFトークンを取得してSimpleEntryを返す
	  毎回同じとは限らないので出品するごとに取得する*/
	private SimpleEntry<String,String> GetCSRFParam() throws Exception {
		URL url = new URL("https://www.mercari.com/jp/sell/");
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("cookie",this.cookiestr);
		String charset = Arrays.asList(conn.getContentType().split(";") ).get(1);
		String encoding = Arrays.asList(charset.split("=") ).get(1);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding )); 
		StringBuffer response = new StringBuffer();
		String line;
		while ((line= in.readLine()) != null) 
            response.append(line+"\n");
		in.close();
		//Pattern csrf_pattern = Pattern.compile("App.setCsrfToken\\(\'([^<]+)\'\\)",Pattern.CASE_INSENSITIVE);
	    Pattern csrf_pattern = Pattern.compile("}, '([^<]+)', '",Pattern.CASE_INSENSITIVE); 
		Matcher matcher1 = csrf_pattern.matcher(response.toString());
		if(matcher1.find() ) {
			String rst = matcher1.group(1);
			return new SimpleEntry<String,String>("__csrf_value",rst);
		}
		return null;
	}
	
	/*商品出品ページのHTMLからexhibitトークンを取得してSimpleEntryを返す
	  毎回同じとは限らないので出品するごとに取得する*/
	private SimpleEntry<String,String> GetExhibitParam() throws Exception {
		URL url = new URL("https://www.mercari.com/jp/sell/");
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("cookie",this.cookiestr);
		String charset = Arrays.asList(conn.getContentType().split(";") ).get(1);
		String encoding = Arrays.asList(charset.split("=") ).get(1);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding )); 
		StringBuffer response = new StringBuffer();
		String line;
		while ((line= in.readLine()) != null) 
            response.append(line+"\n");
		in.close();
		//Pattern csrf_pattern = Pattern.compile("App.setCsrfToken\\(\'([^<]+)\'\\)",Pattern.CASE_INSENSITIVE);
		Pattern csrf_pattern = Pattern.compile("', '([^<]+)'\\);",Pattern.CASE_INSENSITIVE);
		Matcher matcher1 = csrf_pattern.matcher(response.toString());
		if(matcher1.find() ) {
			String rst = matcher1.group(1);
			return new SimpleEntry<String,String>("exhibit_token",rst);
		}
		return null;
	}
	
	/*メルカリAPIを使用するのではなくメルカリのWebサイトのフォームのPOSTを行う*/
	public String SendPostMercariform(String url,List<SimpleEntry<String,String>> param){
		try{
			URL obj = new URL(url);            
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("content-type",content_type);
			con.setRequestProperty("accept-encoding",accept_encoding);
			con.setRequestProperty("accept",accept);
			con.setRequestProperty("cookie",this.cookiestr);
			con.setRequestProperty("user-agent",user_agent);
			
			//パラメータ作成
			String urlParameters = "";
			List<String> paramstr = new ArrayList<String>();
            for (SimpleEntry p:param) {
			    String k = URLEncoder.encode(p.getKey().toString(),"UTF-8");
				String v = URLEncoder.encode(p.getValue().toString(),"UTF-8");
				paramstr.add(k + "=" + v);
            }
			System.out.println("unko");
            urlParameters = String.join("&",paramstr);
            System.out.println(urlParameters);
			//POST送信
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
 
			return response.toString();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
	}
}
