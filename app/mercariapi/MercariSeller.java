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

public class MercariSeller{
	public Long id;
	public String name;
	public String photo_url;
	public String photo_thumbnail_url;
	public String register_sms_confirmation;
	//public String register_sms_confirmation_at;

	public MercariSeller(JSONObject json){
		try{
			this.id = json.getLong("id");
			this.name = json.getString("name");
			this.photo_url = json.getString("photo_url");
			this.photo_thumbnail_url = json.getString("photo_thumbnail_url");
			this.register_sms_confirmation = json.getString("register_sms_confirmation");
			//this.register_sms_confirmation_at = json.getString("register_sms_confirmation_at");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
