package models;
import javax.persistence.*;
import javax.validation.*;

import com.avaje.ebean.annotation.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.data.validation.Constraints.*;

@Entity
public class User extends Model{
 	
    @Id
    public String username;

	@Required
	public String password;
	
	//public String phpssid;
	public String global_access_token;
	public String access_token;

	public String slackurl;
	public String channel;
	
    public static Finder<String, User> find = new Finder<String, User>(String.class, User.class);

    public static User authenticate(String username, String password) {
		return find.where().eq("username", username).eq("password", password).findUnique();
	}

		
}
