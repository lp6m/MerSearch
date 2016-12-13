package mercariapi;

import play.*;
import play.mvc.*;
import java.util.*;

import views.html.*;
import java.util.AbstractMap.SimpleEntry;

public class MercariExhibitItem{
	public String name = "";
	public String description = "";
	public String category_id = "";
	public String size = "";
	public String brand_name = "";
	public String item_condition = "";
	public String shipping_payer = "";
	public String shipping_method = "";
	public String shipping_from_area = "";
	public String shipping_duration = "";
	public String price = "";
	public String sales_fee = "";
	public String image1path = "";
	public String image2path = "";
	public String image3path = "";
	public String image4path = "";

	public MercariExhibitItem(){
	}
	/*MercariItemから出品用のオブジェクトを作成*/
	public MercariExhibitItem(MercariItem item){
		this.name = item.name;
		this.description = item.description;
		this.category_id = item.category.id.toString();
		//this.size =
		//this.brand_name
		//this.item_condition =
	    //this.shipping_payer
		//this.shipping_method
		this.shipping_from_area = item.shipping_from_area.toString();
		//this.shipping_duration
		this.price = item.price.toString();
		//this.sales_fee
		//this.
	}
	public List<SimpleEntry<String,String>> toParamList(){
		List<SimpleEntry<String,String>> rst = new ArrayList<SimpleEntry<String,String>>();
		
		rst.add(new SimpleEntry<String,String>("name",name));	
		rst.add(new SimpleEntry<String,String>("description",description));
		rst.add(new SimpleEntry<String,String>("category_id",category_id));
		rst.add(new SimpleEntry<String,String>("size",size));
		rst.add(new SimpleEntry<String,String>("brand_name",brand_name));
		rst.add(new SimpleEntry<String,String>("item_condition",item_condition));
		rst.add(new SimpleEntry<String,String>("shipping_payer",shipping_payer));
		rst.add(new SimpleEntry<String,String>("shipping_method",shipping_method));
		rst.add(new SimpleEntry<String,String>("shipping_from_area",shipping_from_area));
		rst.add(new SimpleEntry<String,String>("shipping_duration",shipping_duration));
		rst.add(new SimpleEntry<String,String>("price",price));
		rst.add(new SimpleEntry<String,String>("sales_fee",sales_fee));
		String image1str, image2str, image3str, image4str;
		if(image1path != "") image1str = MercariUtils.GetBase64ImageString(image1path);
		else image1str = "";
		if(image2path != "") image2str = MercariUtils.GetBase64ImageString(image2path);
		else image2str = "";
		if(image3path != "") image3str = MercariUtils.GetBase64ImageString(image3path);
		else image3str = "";
		if(image4path != "") image4str = MercariUtils.GetBase64ImageString(image4path);
		else image4str = "";
		rst.add(new SimpleEntry<String,String>("image1",image1str));
		rst.add(new SimpleEntry<String,String>("image2",image2str)); 
		rst.add(new SimpleEntry<String,String>("image3",image3str));
		rst.add(new SimpleEntry<String,String>("image4",image4str));
		return rst;
	}
}
