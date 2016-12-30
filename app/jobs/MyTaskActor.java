package jobs;

import akka.actor.*;
import mercariapi.*;
import slackapi.SlackSender;
import forms.*;
import play.*;
import play.data.*;
import play.mvc.*;
import static play.data.Form.*;
import java.util.*;
import views.html.*;
import actions.*;
import models.*;

public class MyTaskActor extends UntypedActor{
	@Override
	public void onReceive(Object message){
		if(message.equals("reexhibit")){
			ReExhibit();
		}else{
			unhandled(message);
		}
	}
	/*DB上の全てのデータに対して再出品を行う*/
	public static void ReExhibit(){
		List<ManageItem> manageItems = ManageItem.find.all();
		List<ManageItem> newitems = new ArrayList<ManageItem>();
		//MercariSearcher ms = new MercariSearcher();
		for(ManageItem mitem : manageItems){
			/*商品データを構成*/
			mitem.constructMercariItemInfoFromJSON();
			User user = User.find.where().eq("username",mitem.username).findList().get(0);
			SlackSender ss = new SlackSender(user.slackurl, user.channel);
			try{
				MercariSearcher ms = new MercariSearcher(user.access_token, user.global_access_token);
				//商品の状態を調べる
				MercariItem now_onMercariItem = ms.GetItemInfobyItemID(mitem.itemid);
				if(now_onMercariItem.status.equals("on_sale") && now_onMercariItem.num_comments == 0){
					/*まだ売れていないかつコメントが0*/
					MercariItem newitem = ms.CancelandSell(mitem.itemid);
					if(newitem == null) throw new IllegalArgumentException();
					ManageItem newmanageitem = new ManageItem(newitem.id,
															  user.username,
															  newitem.toJSON(),
															  false,
															  mitem.zaiko);
					newitems.add(newmanageitem);
					mitem.delete();
					ss.sendMessage("商品を再出品しました :" + newitem.name);
				}else{
					/*商品が売れた or 元々売れていた or 手動で削除した or 売れていないがコメントがついた*/
					/*在庫がある場合のみ出品*/
					if(mitem.zaiko - 1 > 0){
						MercariItem newitem =  ms.Sell(mitem.item);
						if(newitem == null) throw new IllegalArgumentException();
						ManageItem newmanageitem = new ManageItem(newitem.id,
																  user.username,
																  newitem.toJSON(),
																  false,
																  mitem.zaiko - 1);
						newitems.add(newmanageitem);
						if(now_onMercariItem.status == "on_sale"){
							ss.sendMessage("商品が販売中でなくなり,再出品しました :" + newitem.name);
						}else{
							ss.sendMessage("商品にコメントがついたので再出品しました :" + newitem.name);
						}
					}else{
						ss.sendMessage("商品の在庫が足りないため出品できませんでした :" + mitem.itemid);
					}
					mitem.delete();
				}
			}catch(IllegalArgumentException e){
				ss.sendMessage("商品の再出品に失敗 :" + mitem.itemid);
			}catch(Exception e){
				ss.sendMessage("商品の再出品中に何らかのエラーが発生 :" + mitem.itemid);
			}
		}
		/*最後にすべての商品をDBに保存する*/
		for(ManageItem newitem : newitems){
			newitem.save();
			Logger.info("商品をDBに追加しました: " + newitem.itemid);
		}
	}
		
}
