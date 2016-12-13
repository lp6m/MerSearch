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
	
    public static Finder<String, ManageItem> find = new Finder<String, ManageItem>(String.class, ManageItem.class);

}
