package models;
import mercariapi.MercariItem;
import javax.persistence.*;
import javax.validation.*;

import com.avaje.ebean.annotation.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.data.validation.Constraints.*;

@Entity
public class ManageItem extends Model{
 	
    @Id
    public String itemid;

	@Required
	public String username;

	@Required
	public MercariItem item;

	@Required
	public Boolean ignoreflag;

	@Required
	public Integer zaiko;

	public ManageItem(){
		
	}
	public ManageItem(String itemid, String username, MercariItem item, Boolean ignoreflag, Integer zaiko){
		this.itemid = itemid;
		this.username = username;
		this.item = item;
		this.ignoreflag = ignoreflag;
		this.zaiko = zaiko;
	}
    public static Finder<String, ManageItem> find = new Finder<String, ManageItem>(String.class, ManageItem.class);

}
