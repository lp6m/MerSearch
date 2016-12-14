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
	public String[] imageurls = new String[4];

	public MercariExhibitItem(){
	}
	/*MercariItemから出品用のオブジェクトを作成*/
	public MercariExhibitItem(MercariItem item){
		try{
			this.name = item.name;
			this.description = item.description;
			this.category_id = item.category.id.toString();
			this.size = (item.size == null ? "" : item.size.toString());
			this.brand_name = (item.brand_name == null ? "" : item.size.toString());
			this.item_condition = item.item_condition.toString();
			this.shipping_payer = item.shipping_payer.toString();
			this.shipping_method = item.shipping_method.toString();
			this.shipping_from_area = item.shipping_from_area.toString(); 
			this.shipping_duration = item.shipping_duration.toString();
			this.price = item.price.toString();
			MercariSearcher s = new MercariSearcher();
			Integer fee = s.GetSalesFee(item.price, item.category.id);
			this.sales_fee = fee.toString();
			for(int i = 0; i < this.imageurls.length; i++) this.imageurls[i] = item.imageurls[i];
		}catch(Exception e){
			/*手数料計算失敗した場合や,size,brand_name以外の必須項目がnullになっていた場合は例外発生する*/
			System.out.println("MercariItemからMercariExhibitItemへの変換に失敗");
		}
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
		String[] imagestr = new String[4];
		for(int i = 0; i < this.imageurls.length; i++){
			if(imageurls[i] != "") imagestr[i] = MercariUtils.GetBase64ImageFromURL(imageurls[i]);
			else imagestr[i] = "";
		}
		rst.add(new SimpleEntry<String,String>("image1",imagestr[0]));
		rst.add(new SimpleEntry<String,String>("image2",imagestr[1])); 
		rst.add(new SimpleEntry<String,String>("image3",imagestr[2]));
		rst.add(new SimpleEntry<String,String>("image4",imagestr[3]));
		return rst;
	}
}
