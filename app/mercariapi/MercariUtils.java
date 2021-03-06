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
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;
import java.awt.image.BufferedImage;

public class MercariUtils{
	/*URLから画像を取得してBase64文字列に変換する*/
	public static String GetBase64ImageFromURL(String imageUrl){
		/*URL imgurl = MercariUtils.class.getClassLoader().getResource(filename);
		File file = new File(imgurl.getFile());
		BufferedImage image = null;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baos);
		image.flush();
		try {
			ImageIO.write(image, "jpg", bos);
			//bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
			}*/
		byte[] imageBytes = getImageByteArray(imageUrl, "jpg");
		String imageString = new String(Base64.encodeBase64(imageBytes));
		return "data:image/jpeg;base64," + imageString;
	}
	/*画像URLから画像を取得する*/
	static public byte[] getImageByteArray(String imageUrl, String fileNameExtension) {
		try{
			URL url = new URL(imageUrl);
			BufferedImage readImage = ImageIO.read(url);
			ByteArrayOutputStream outPutStream = new ByteArrayOutputStream();
			ImageIO.write(readImage, fileNameExtension, outPutStream);
			return outPutStream.toByteArray();
		}catch(Exception e){
			return null;
		}
	}
}
