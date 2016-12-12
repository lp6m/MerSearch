package controllers;
import mercariapi.*;

import play.*;
import play.data.*;
import play.mvc.*;
import static play.data.Form.*;
import java.util.*;
import views.html.*;
import actions.*;
import models.*;
public class Application extends Controller {
	public static MercariSearcher mercariapi;
    public static Result index() {
		User admin = new User();
		admin.username = "user";
		admin.password = "pass";  
		admin.save();
        return ok(index.render("Your new application is ready."));
    }
	public static class SearchForm{
		public String sellerid;
	}
	@With(BasicAuthAction.class)
 	public static Result searchresult(){
		Form<SearchForm> searchform = new Form<SearchForm>(SearchForm.class).bindFromRequest();
		String sellerid = searchform.get().sellerid; //"220249289"
		System.out.println(sellerid);
		mercariapi = new MercariSearcher();
		System.out.println(mercariapi.access_token);
		List<MercariItem> res = mercariapi.GetAllItemsWithSellers(sellerid,new ArrayList<Integer>(Arrays.asList(2,3)));
		mercariapi.GetItemInfobyItemID("m170271875"); 
		return ok(searchresult.render(res));		
	}

}
