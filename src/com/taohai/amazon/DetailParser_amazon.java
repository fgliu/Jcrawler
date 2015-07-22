package com.taohai.amazon;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import com.alienmegacorp.amazonproducts.AmazonProductsAPI;
import com.alienmegacorp.amazonproducts.Endpoint;
import com.alienmegacorp.amazonproducts.internals.DecimalWithUnits;
import com.alienmegacorp.amazonproducts.internals.EditorialReviews;
import com.alienmegacorp.amazonproducts.internals.Item;
import com.alienmegacorp.amazonproducts.internals.Item.VariationAttributes;
import com.alienmegacorp.amazonproducts.internals.ItemAttributes;
import com.alienmegacorp.amazonproducts.internals.ItemAttributes.ItemDimensions;
import com.alienmegacorp.amazonproducts.internals.ItemAttributes.PackageDimensions;
import com.alienmegacorp.amazonproducts.internals.Offer;
import com.alienmegacorp.amazonproducts.internals.OfferListing;
import com.alienmegacorp.amazonproducts.internals.Price;
import com.alienmegacorp.amazonproducts.internals.VariationAttribute;
import com.alienmegacorp.amazonproducts.internals.Variations;

public class DetailParser_amazon {

	
//	define('AWS_API_KEY', 'AWS_API_KEY');
//	define('AWS_API_SECRET_KEY','AWS_API_SECRET_KEY');
//	define('AWS_ASSOCIATE_TAG', 'AWS_ASSOCIATE_TAG');
	public static final String AMAZON_MERCHANT = "Amazon.com";
	
	
	public static String awsAccessKey = "awsAccessKey";
	
	public static String awsSecretKey = "awsSecretKey";
	
	public static String awsAssociateTag = "awsAssociateTag";
	
	public  String sku;

	public DetailParser_amazon(String url){
//		this.sku = "B002DGSWS6"; //
		this.setSku("B002DGSWS6");//B00A88EV5Y
	}
	
	public String makeJSONString() {
		JSONObject jo = makeJSON();
		if (jo != null) {
			return jo.toString();
		}
		return "";
	}

	private JSONObject makeJSON() {
		
		AmazonProductsAPI api = AmazonProductsAPI.getInstance(Endpoint.US, awsAccessKey, awsSecretKey, awsAssociateTag);
		JSONObject json = new JSONObject();
		try {
			
			List<Item> itemList = new ArrayList<Item>();
			Item item = api.itemLookup(this.getSku(), "Large");	//��SKU B003ZJH98O  B002WJIPVI  B003HS5JMQ
			
			String parentASIN = item.getParentASIN();//B002DGSWS6
			
			String category = null;	//����
			int is_spec = 0;
			
			if(parentASIN != null) {
				System.out.println("has parent asin:" + parentASIN);
				is_spec = 1;
				Item items = api.itemLookup(parentASIN, "Variations");
				Variations variations = items.getVariations();
				itemList = variations.getItem();
//				itemList = api.itemLookup(parentASIN, "Variations").getVariations().getItem();
			} else {
				System.out.println("��SKU...");
				itemList.add(item);
				is_spec = 0;
			}
			
			JSONObject data = new JSONObject();
			json = json.put("data", data);
			
			if(item.getItemAttributes() != null) {
				category = item.getItemAttributes().getProductTypeName();
			}
			
			for(Item t:itemList) {
				String asin = t.getASIN();
				
				if(t.getOffers() == null) {
					System.out.println("û�б���...");
					continue;
				}
				
				//��ȡamazon��Ӫ
				List<Offer> offers = t.getOffers().getOffer();
				Offer amzOffer = null;
				for(Offer of : offers) {
					String name = of.getMerchant().getName();
					if(AMAZON_MERCHANT.equalsIgnoreCase(name)){
						amzOffer = of;
					} else {
						System.out.println("�����:" + name);
					}
				}
				if(amzOffer == null) {
					System.out.println("��ASIN��amazon��Ӫ:" + asin);
					continue;
				}
				
				List<OfferListing> offerListingList = amzOffer.getOfferListing();
				if(offerListingList == null || offerListingList.size() == 0) {
					System.out.println("��ASIN�޼۸���Ϣ:" + asin);
					continue;
				}
				
				//��Ʒ�����
				JSONObject varData = new JSONObject();
				
				//��Ӫ��Ʒ�ֶ�
				varData.put("is_selfsupport", 1);
				
				//��ȡ��ǰ���۵���Ʒ�۸�
				OfferListing offerListing = offerListingList.get(0);
				Price price = offerListing.getPrice();
				Price salePrice = offerListing.getSalePrice();
				//����д����, ��ʹ�ô����
				if(salePrice != null) {
					price = salePrice;
				}
				varData.put("CurrencyCode", price.getCurrencyCode());
				varData.put("sell_price", price.getAmount().intValue()/100.00);	//���ۼ۸�
				
				//��ȡ���
				String stock = offerListing.getAvailability();
				varData.put("stock", stock);
				
				//��ȡͼƬ
				if(t.getLargeImage() != null) {
					varData.put("Images", t.getLargeImage().getURL());
				}
				
				//��ȡ����
				if(item.getEditorialReviews() != null) {
					EditorialReviews ers = item.getEditorialReviews();
					if(ers != null && ers.getEditorialReview() != null
							&& ers.getEditorialReview().size() > 0){
						String intro = ers.getEditorialReview().get(0).getContent();
						String base64 = new String(Base64.encodeBase64(intro.getBytes()));
						varData.put("Intro", base64);
						//System.out.println("intro:" + base64);
					}
				}
				
				//��ȡ���
				VariationAttributes vars = t.getVariationAttributes();
				if(vars != null) {
					List<VariationAttribute> varlist = vars.getVariationAttribute();
					if(varlist != null && varlist.size() > 0) {
						JSONObject spec = new JSONObject();
						varData.put("spec", spec);
						for(VariationAttribute var:varlist) {
							spec.put(var.getName(), "" + var.getValue());
						}
					}
				}
				
				ItemAttributes attr = t.getItemAttributes();
				if(attr != null) {
					//��ȡ���������, ����ȡPackageDimensions
					DecimalWithUnits dec_w = null, dec_h = null, dec_wt = null, dec_l = null;
					PackageDimensions pdim = attr.getPackageDimensions();
					ItemDimensions dim = attr.getItemDimensions();
					if(pdim != null) {
						dec_w = pdim.getWidth();
						dec_h = pdim.getHeight();
						dec_wt = pdim.getWeight();
						dec_l = pdim.getLength();
					} else if(dim != null){
						dec_w = dim.getWidth();
						dec_h = dim.getHeight();
						dec_wt = dim.getWeight();
						dec_l = dim.getLength();
					}
					if(dec_w != null) {
						JSONArray ja = new JSONArray();
						ja.put("" + dec_w.getValue());
						ja.put(dec_w.getUnits());
						varData.put("Width", ja);
					}
					if(dec_h != null) {
						JSONArray ja = new JSONArray();
						ja.put("" + dec_h.getValue());
						ja.put(dec_h.getUnits());
						varData.put("Height", ja);
					}
					if(dec_wt != null) {
						JSONArray ja = new JSONArray();
						ja.put("" + dec_wt.getValue());
						ja.put(dec_wt.getUnits());
						varData.put("Weight", ja);
					}
					if(dec_l != null) {
						JSONArray ja = new JSONArray();
						ja.put("" + dec_l.getValue());
						ja.put(dec_l.getUnits());
						varData.put("Length", ja);
					}
					
					//��ȡ��Ʒԭ��
					Price listPrice = attr.getListPrice();
					if(listPrice != null) {
						varData.put("market_price", listPrice.getAmount().intValue()/100.00);	
					}
					
					//��ȡƷ��
					varData.put("Brand", attr.getBrand());
					
					//��ȡ����
					varData.put("Title", attr.getTitle());
					
					varData.put("type", category);
					varData.put("is_spec", is_spec);
				}
				
				data.put(asin, varData);
				System.out.println("asin:" + asin);
				
			}
			
			json.put("status", "0"); 
		} catch (Exception e) {
			e.printStackTrace();
			JSONObject jsone = new JSONObject();
			jsone.put("status", "1");
			jsone.put("error", "Catch Exception...");
			System.out.println(jsone);
		}
		return json;
		
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}
	
	public static void main(String[] args) {
		String url = "";
		DetailParser_amazon amazon = new DetailParser_amazon(url);
		String str = amazon.makeJSONString();
		System.out.println(str);
		
	}
	


}
