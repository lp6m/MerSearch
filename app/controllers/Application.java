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

	//@With(BasicAuthAction.class)
    public static Result index() {
		String pop_message = session("message") == null ? "" : session("message");
		List<ManageItem> items = ManageItem.find.all();
	    return ok(index.render(pop_message));
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
		if(session("username") == null || !session("username").equals("admin")) return redirect("/");
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
        return ok(login.render("login"));
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
			return badRequest(login.render("ERROR"));
		}catch(Exception e){
			return badRequest(login.render("ERROR"));
		}
	}

	@With(BasicAuthAction.class)
	public static Result additem(){
		/*ログイン必須*/
		if(session("username")==null){
			session("message","ログインしてください");
			return redirect("/");
		}
		//POSTのみ
		try{
			Map<String,String[]> f = request().body().asFormUrlEncoded();
			String itemid = f.get("itemid")[0];
			Integer zaikonum = Integer.parseInt(f.get("zaikonum")[0]);
			/*商品IDから商品の情報を検索*/
			MercariSearcher s = new MercariSearcher();
			MercariItem item = s.GetItemInfobyItemID(itemid);
			if(item != null){
				String warnstr = "";
				/*まずその商品を即時削除する.*/
				MercariExhibitter me = new MercariExhibitter(session("phpssid"));
				Boolean cancel_rst = me.Cancel(itemid);
				if(cancel_rst == false){ /*他人の商品の場合は削除に失敗する(はず)*/
					warnstr = "警告: 商品の削除に失敗（他人の商品？）";
				}
				/*即時商品の出品*/
				MercariExhibitItem new_item = new MercariExhibitItem(item);
				me.Sell(new_item);
				/*商品を管理データベースに追加する*/
				ManageItem manageitem = new ManageItem();
				manageitem.itemid = itemid;
				manageitem.item = item;
				manageitem.username = session("username");
				manageitem.zaiko = zaikonum;
				manageitem.ignoreflag = false;
				manageitem.save();
				session("message","商品を追加しました" + warnstr);
				return redirect("/");
			}else{
				/*商品が見つからなかった*/
				session("message","商品が見つからなかったため,商品を追加できませんでした");
				return redirect("/");
			}
		}catch(Exception e){
			session("message","商品の追加に失敗しました");
			return redirect("/");
		}
	}
	@With(BasicAuthAction.class)
	public static Result deleteitem(){
		//POSTのみ
		try{
			return redirect("/");
		}catch(Exception e){
			return redirect("/");
		}
	}

	//管理データベースの商品情報を更新する
	@With(BasicAuthAction.class)
	public static  Result updateManageInfo(){
		//POSTのみ
		try{
			List<ManageItem> items = ManageItem.find.all();
			MercariSearcher ms = new MercariSearcher();
			for(ManageItem manageitem : items){
				manageitem.item = ms.GetItemInfobyItemID(manageitem.itemid);
				manageitem.update();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return redirect("/index");
	}
}
	
