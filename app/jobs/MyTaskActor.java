package jobs;

import akka.actor.*;
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
		MercariSearcher ms = new MercariSearcher();
		for(ManageItem mitem : manageItems){
			try{
				User user = User.find.where().eq("username",mitem.username).findList().get(0);
				String phpssid = user.phpssid;
				MercariExhibitter me = new MercariExhibitter(phpssid);
				
				//商品の状態を調べる
				String status = ms.GetItemInfobyItemID(mitem.itemid).status;
				if(status == "on_sale"){
					/*まだ売れていない*/
					MercariItem newitem = me.SellandCancel(mitem.item);
					ManageItem newmanageitem = new ManageItem(newitem.id,
															  user.username,
															  newitem,
															  false,
															  mitem.zaiko);
					newitems.add(newmanageitem);
					mitem.delete();
				}else{
					/*商品が売れた or 元々売れていた*/
					/*在庫がある場合のみ出品*/
					if(mitem.zaiko - 1 > 0){
						MercariExhibitItem sellitem = new MercariExhibitItem(mitem.item);
						MercariItem newitem =  me.Sell(sellitem);
						ManageItem newmanageitem = new ManageItem(newitem.id,
																  user.username,
																  newitem,
																  false,
																  mitem.zaiko - 1);
						newitems.add(newmanageitem);
					}else{
						System.out.println("商品の在庫がなくなりました; " + mitem.itemid);
					}
					mitem.delete();
					
				}
			}catch(Exception e){
				System.out.println("商品の再出品に失敗(ReExhibit)" + mitem.itemid);
			}
		}
		/*最後にすべての商品をDBに保存する*/
		for(ManageItem newitem : newitems){
			newitem.save();
			System.out.println("商品をDBに追加しました: " + newitem.itemid);
		}
	}
		
}
