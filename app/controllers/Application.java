package controllers;
import mercariapi.*;

import play.*;
import play.data.*;
import play.mvc.*;
import static play.data.Form.*;
import java.util.*;
import views.html.*;

public class Application extends Controller {
	public static MercariAPI mercariapi;
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
	public static class SearchForm{
		public String sellerid;
	}
 	public static Result searchresult(){
		Form<SearchForm> searchform = new Form<SearchForm>(SearchForm.class).bindFromRequest();
		String sellerid = searchform.get().sellerid; //"220249289"
		System.out.println(sellerid);
		mercariapi = new MercariAPI();
		System.out.println(mercariapi.access_token);
		List<MercariItem> res = mercariapi.GetAllItemsWithSellers(sellerid,new ArrayList<Integer>(Arrays.asList(2,3)));
		return ok(searchresult.render(res));
		
	}

}
