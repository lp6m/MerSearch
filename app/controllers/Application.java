package controllers;
import mercariapi.*;

import play.*;
import play.mvc.*;
import java.util.*;
import views.html.*;

public class Application extends Controller {
	public static MercariAPI mercariapi;
    public static Result index() {
		mercariapi = new MercariAPI();
		System.out.println(mercariapi.access_token);
		List<MercariItem> res = mercariapi.GetAllItemsWithSellers("m277420808",new ArrayList<Integer>(Arrays.asList(1,2,3)));
		for(MercariItem item : res){
			System.out.println(item.name);
		}
																  
										  
        return ok(index.render("Your new application is ready."));
    }

}
