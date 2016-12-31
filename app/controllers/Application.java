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
import slackapi.*;

public class Application extends Controller {
	public static MercariSearcher mercariapi;

	@With(BasicAuthAction.class)
	public static Result index() {
	    String pop_message = session("message") == null ? "" : session("message");
		String username = session("username");
		/*そのユーザーの管理している商品一覧を取得*/
		List<ManageItem> items = new ArrayList<ManageItem>();
		if(username != null) items = ManageItem.find.where().eq("username", username).findList();
		for(ManageItem item : items) item.constructMercariItemInfoFromJSON();
		mercariapi = new MercariSearcher();
	    return ok(index.render(username, pop_message, items));
    }
	public static class SearchForm{
		public String sellerid;
	}
	@With(BasicAuthAction.class)
 	public static Result searchresult(){
		Form<SearchForm> searchform = new Form<SearchForm>(SearchForm.class).bindFromRequest();
		String sellerid = searchform.get().sellerid; //"220249289"
		mercariapi = new MercariSearcher();
		List<MercariItem> res = mercariapi.GetAllItemsWithSellers(sellerid,new ArrayList<Integer>(Arrays.asList(2,3)));
		return ok(searchresult.render(res));		
	}
	
	@With(BasicAuthAction.class)
	public static Result createuser(){
		/*adminでないとindexに送り返す*/
		if(session("username") == null || !session("username").equals("admin")) return redirect(routes.Application.index());
		else{
			if("GET".equals(request().method())){
				//GET 画面表示
				Form<User> f = new Form<User>(User.class);
				return ok(createuser.render(""));
			}else{
				//POST ユーザ作成
				Map<String,String[]> f = request().body().asFormUrlEncoded();
				String username = f.get("username")[0];
				String password = f.get("password")[0];
				String mercari_email = f.get("mercari_email")[0];
				String mercari_password = f.get("mercari_password")[0];
				String slackurl = f.get("slackurl")[0];
				String channel = f.get("channel")[0];
				/*アクセストークンを取得する*/
				MercariSearcher ms = new MercariSearcher();
				Boolean ok = ms.tryMercariLogin(mercari_email, mercari_password);
				if(ok == false) return badRequest(createuser.render("メルカリのログインに失敗"));
				User user = new User();
				user.username = username;
				user.password = password;
				user.access_token = ms.access_token;
				user.global_access_token = ms.global_access_token;
				user.slackurl = slackurl;
				user.channel = channel;
				user.save();
				SlackSender ss = new SlackSender(slackurl, channel);
				ss.sendMessage("ユーザを作成しました");
			}
		}
		return redirect(routes.Application.login());
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
				return redirect(routes.Application.index());
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
			return redirect(routes.Application.login());
		}
		//POSTのみ
		try{
			Map<String,String[]> f = request().body().asFormUrlEncoded();
			String itemid = f.get("itemid")[0];
			Integer zaikonum = Integer.parseInt(f.get("zaikonum")[0]);
			Boolean deleteflag = f.get("deleteflag")[0].equals("1");
			Boolean addflag = f.get("adddataflag")[0].equals("1");
			User user = User.find.byId(session("username"));
			/*商品IDから商品の情報を検索*/
			MercariSearcher ms = new MercariSearcher(user.access_token, user.global_access_token);
			MercariItem item = ms.GetItemInfobyItemID(itemid);
			if(item != null){
				String warnstr = "";
				/*まずその商品を即時削除する.*/
				if(deleteflag){
					Boolean cancel_rst = ms.Cancel(item);
					if(cancel_rst == false){ /*他人の商品の場合は削除に失敗する(はず)*/
						warnstr = "警告: 商品の削除に失敗（他人の商品？）";
					}
				}
				/*即時商品の出品*/
				MercariItem new_item = ms.Sell(item);
				if(addflag){
					/*商品を管理データベースに追加する*/
					ManageItem manageitem = new ManageItem(new_item.id,
														   user.username,
														   new_item.toJSON(),
														   false,
														   zaikonum);
					manageitem.save();
				}
				session("message","商品を追加しました" + warnstr);
				return redirect(routes.Application.index());
			}else{
				/*商品が見つからなかった*/
				session("message","商品が見つからなかったため,商品を追加できませんでした");
				return redirect(routes.Application.index());
			}
		}catch(Exception e){
			e.printStackTrace();
			session("message","商品の追加に失敗しました");
			return redirect(routes.Application.index());
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
		return redirect(routes.Application.index());
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
		return redirect(routes.Application.index());
	}

	/*商品管理データベースの在庫数を更新*/
	@With(BasicAuthAction.class)
	public static Result updateZaikoNum(String itemid){
		try{
			Map<String, String[]> f = request().queryString();
			Integer zaikonum  = Integer.parseInt(f.get("zaikonum")[0]);
			ManageItem item = ManageItem.find.where().eq("itemid",itemid).findList().get(0);
			item.zaiko = zaikonum;
			item.update();
			session("message","DBの在庫数を変更しました : " + itemid);
		}catch(Exception e){
			session("message","DBから商品を削除するのに失敗しました");
		}
		return redirect(routes.Application.index());
	}
}
	
