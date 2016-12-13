package models;
import javax.persistence.*;
import javax.validation.*;

import com.avaje.ebean.annotation.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.data.validation.Constraints.*;

@Entity
public class BasicAuthUser extends Model{
 	
    @Id
    public String username;

	@Required
	public String password;

    public static Finder<String, BasicAuthUser> find = new Finder<String, BasicAuthUser>(String.class,BasicAuthUser.class);

    public static BasicAuthUser authenticate(String username, String password) {
		return find.where().eq("username", username).eq("password", password).findUnique();
	}

		
}
