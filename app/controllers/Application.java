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

	@With(BasicAuthAction.class)
    public static Result index() {
		String pop_message = session("message") == null ? "" : session("message");
		List<ManageItem> items = ManageItem.find.all();
		for(ManageItem item : items) item.updateMercariItemforView();
		
	    return ok(index.render(pop_message, items));
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
		}
		return redirect("/");
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
			Boolean deleteflag = f.get("deleteflag")[0].equals("1");
			Boolean addflag = f.get("adddataflag")[0].equals("1");
			System.out.println("deleteflag: " + Boolean.toString(deleteflag));
			System.out.println("addflag: " + Boolean.toString(addflag));
			
			/*商品IDから商品の情報を検索*/
			MercariSearcher s = new MercariSearcher();
			MercariItem item = s.GetItemInfobyItemID(itemid);
			if(item != null){
				String warnstr = "";
				MercariExhibitter me = new MercariExhibitter(session("phpssid"));
				/*まずその商品を即時削除する.*/
				if(deleteflag){
					Boolean cancel_rst = me.Cancel(itemid);
					if(cancel_rst == false){ /*他人の商品の場合は削除に失敗する(はず)*/
						warnstr = "警告: 商品の削除に失敗（他人の商品？）";
					}
				}
				/*即時商品の出品*/
				MercariExhibitItem sell_item = new MercariExhibitItem(item);
				MercariItem new_item = me.Sell(sell_item);
				if(addflag){
					/*商品を管理データベースに追加する*/
					ManageItem manageitem = new ManageItem(new_item.id,
														   session("username"),
														   new_item.toJSON(),
														   false,
														   zaikonum);
					manageitem.save();
				}
				session("message","商品を追加しました" + warnstr);
				return redirect("/");
			}else{
				/*商品が見つからなかった*/
				session("message","商品が見つからなかったため,商品を追加できませんでした");
				return redirect("/");
			}
		}catch(Exception e){
			e.printStackTrace();
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
				manageitem.itemjson = ms.GetItemInfobyItemID(manageitem.itemid).toJSON();
				manageitem.update();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return redirect("/index");
	}

	/*商品管理データベースから当該商品を削除*/
	@With(BasicAuthAction.class)
	public static Result deleteManageItem(String itemid){
		try{
			ManageItem item = ManageItem.find.where().eq("itemid",itemid).findList().get(0);
			item.delete();
			session("message","DBから商品を削除しました : " + itemid);
		}catch(Exception e){
			session("message","DBから商品を削除するのに失敗しました");
		}
		return redirect("/");
	}
}
	
