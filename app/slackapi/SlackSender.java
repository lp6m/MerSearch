package slackapi;
import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;

import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.net.URLEncoder;
import java.net.URLDecoder;


public class SlackSender{
	private String slackurl;
	private String channel;
	public SlackSender(String slackurl, String channel){
		this.slackurl = slackurl;
		this.channel = channel;
	}

	 public void sendMessage(String message){
		try{
			URL obj = new URL(this.slackurl);            
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
            String urlParameters = "payload=" + URLEncoder.encode("{\"channel\": \"" + this.channel + "\", \"text\": \"" + message + "\",}\"", "UTF-8");
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
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
