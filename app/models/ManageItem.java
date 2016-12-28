package models;
import mercariapi.MercariItem;
import javax.persistence.*;
import javax.validation.*;

import com.avaje.ebean.annotation.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.data.validation.Constraints.*;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class ManageItem extends Model{
 	
    @Id
    public String itemid;

	@Required
	public String username;

	@Required
	@Column(columnDefinition = "TEXT")
	public String itemjson;

	@Required
	public Boolean ignoreflag;

	@Required
	public Integer zaiko;

	/*MercariItemはSQLでのオブジェクトに変換できないためDBに保存する際はnullとなる*/
	/*updateMercariItemInfoを実行することで商品情報が構成される*/
	public MercariItem item;
	
	public ManageItem(){
		
	}
	public ManageItem(String itemid, String username, String itemjson, Boolean ignoreflag, Integer zaiko){
		this.itemid = itemid;
		this.username = username;
		this.itemjson = itemjson;
		this.ignoreflag = ignoreflag;
		this.zaiko = zaiko;
	}

	/*JSON文字列で保存していたデータからMercariItemのデータを構成*/
	public void constructMercariItemInfoFromJSON(){
		try{
			ObjectMapper mapper = new ObjectMapper();
			this.item = mapper.readValue(this.itemjson, MercariItem.class);
			//this.item_forview = JSON.decode(this.itemjson, MercariItem.class);
		}catch(Exception e){
			e.printStackTrace();
			this.item = null;
		}
	}
	
    public static Finder<String, ManageItem> find = new Finder<String, ManageItem>(String.class, ManageItem.class);

	@Override
	public String toString(){
		return this.itemid + ":" + this.username + ":";
	}
}
