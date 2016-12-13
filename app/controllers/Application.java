package controllers;
import mercariapi.*;

import forms.*;
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
	@With(BasicAuthAction.class)
	public static Result createuser(){
		/*adminでないとindexに送り返す*/
		if(!session("username").equals("admin")) return redirect("/");
		if("GET".equals(request().method())){
			//GET 画面表示
			Form<User> f = new Form<User>(User.class);
			return ok(createuser.render());
		}else{
			//POST ユーザ作成
			Map<String,String[]> f = request().body().asFormUrlEncoded();
			String username = f.get("username")[0];
			String password = f.get("password")[0];
			String phpssid = f.get("phpssid")[0];
			User user = new User();
			user.username = username;
			user.password = password;
			user.phpssid = phpssid;
			user.save();
			return redirect("/");
		}
		
	}

	@With(BasicAuthAction.class)
	public static Result login(){/*ログインページを表示するだけ*/
		Form<LoginForm> loginForm = new Form<LoginForm>(LoginForm.class);
        return ok(login.render("login",loginForm));
	}
	@With(BasicAuthAction.class)
	public static Result authenticate(){
	    //POST
		try{
			Map<String,String[]> f = request().body().asFormUrlEncoded();
			String username = f.get("username")[0];
			User user = User.find.byId(username);
			if(user != null){
				session("username",user.username);
				session("password",user.password);
				if(user.phpssid != null) session("phpssid",user.phpssid);
				return redirect("/");
			}
			return badRequest(login.render("ERROR",new Form<LoginForm>(LoginForm.class)));
		}catch(Exception e){
			return badRequest(login.render("ERROR",new Form<LoginForm>(LoginForm.class)));
		}
	}

	@With(BasicAuthAction.class)
	public static Result additem(){
		
	}
	@With(BasicAuthAction.class)
	public static Result deleteitem(){
		
	}
}
