package com.taohai.vitacost;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.BulletList;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserUtils;
import org.htmlparser.util.SimpleNodeIterator;
import org.json.JSONArray;
import org.json.JSONObject;

import sun.misc.BASE64Encoder;

public class DetailParser_vitacost {
	private String pid = "";
	private String pname = "";
	private String category = "";
	private String brand = "";
	private String weight = "";
	private String freight = "0";
	private String intro = "";
	private String price="";
	private String listPrice="";
	private String stock ="";
	private String images="";

	private static Proxy proxy = null;
	private BASE64Encoder B64Encoder = new BASE64Encoder();
	private String url;
	private JSONObject joSpec = new JSONObject();
	private JSONObject joMeasure = new JSONObject();
	private JSONArray refItems = new JSONArray();
	private ArrayList<String> ks=new ArrayList<String>();
	private ArrayList<String> vs =  new ArrayList<String>(); 
	
	public DetailParser_vitacost(String url) throws ParserException,
			IOException, ParseException {
		this.url = url;
		getContent(this.url);
	}
	
	public DetailParser_vitacost(String url,int i) throws ParserException,
			IOException, ParseException {
		this.url = url;
		getRefItem(this.url);
	}

	public String upatePrice() throws ParserException, IOException, ParseException {
		JSONArray jsons = this.refItems;
		JSONArray myItems = new JSONArray();
		JSONObject obj = this.makePriceJSON();
		myItems.put(obj);
		int len = jsons.length();
		for (int j = 0; j < len; j++) {
			JSONObject jo = (JSONObject) jsons.get(j);
			String curl = jo.get("argv").toString();
			getRefItem(curl);
			obj = this.makePriceJSON();
			myItems.put(obj);
		}
		return (myItems.toString());
	}
	
	
	public String makeJSONString() {
		JSONObject jo = makeJSON();
		if (jo != null) {
			return jo.toString();
		}
		return "";
	}
	
	public JSONObject makePriceJSON() {
		JSONObject joData = new JSONObject();
		joData.put("sku", this.pid);
		joData.put("price", this.price);
		return joData;
	}
	

	public JSONObject makeJSON() {
		JSONObject joProd = new JSONObject();
		joProd.put("status", 0);
		joProd.put("error", "");
		joProd.put("source_url", this.url);
		joProd.put("time", System.currentTimeMillis());

		JSONObject joData = new JSONObject();
		joData.put("purl", this.url);
		joData.put("pid", this.pid);
		joData.put("id_type", "SKU");
		joData.put("brand", this.brand);
		joData.put("title", this.pname);
		joData.put("intor", this.intro);
		
		joData.put("category", myReplace(this.category));
		joData.put("currency", "$");
		joData.put("merchant", "www.vitacost.com");
		joData.put("price", this.price);
		joData.put("list_price", this.listPrice);
		joData.put("stock", this.stock);
		joData.put("spec_attr", joSpec);
		
		joData.put("weight", this.weight);
		joData.put("images", this.images);//�����ͼƬ
		joData.put("measure", joMeasure);//Ϊ��ֵ
		joData.put("freight", this.freight);
		joProd.put("item", joData);
		joProd.put("recommend_purls", "");
		joProd.put("ref_items", refItems);
		return joProd;
	}
	
	public String myReplace(String str){
		if(null != str){
			 str =  str.replace("'", "");
		}
		return str.trim(); 
	}
	
	public static void main(String[] args) throws ParserException, IOException,
			ParseException {
		long b =System.currentTimeMillis();
//		String url = "http://www.vitacost.com/earth-mama-angel-baby-lotion-vanilla-orange-8-fl-oz";
//		String url = "http://www.vitacost.com/earth-mama-angel-baby-body-butter";
//		String url = "http://www.vitacost.com/earth-mama-angel-baby-body-butter-8-fl-oz";
//		String url ="http://www.vitacost.com/earth-mama-angel-baby-body-butter#pv=57872";
//		String url ="http://www.vitacost.com/earth-mama-angel-baby-earth-mama-bottom-balm-2-oz";
//		String url ="http://www.vitacost.com/pamelas-products-gluten-and-wheat-free-pancake-and-baking-mix";
//		String url ="http://www.vitacost.com/cytosport-muscle-milk-naturals-vanilla-2-47-lbs";
//		String url ="http://www.vitacost.com/atkins-day-break-bar-apple-crisp";
//		String url ="http://www.vitacost.com/halo-purely-for-pets-spots-stew-indoor-cat-formula-wholesome-chicken-3-lbs";
//		String url ="http://www.vitacost.com/halo-purely-for-pets-spots-stew-indoor-cat-formula-wholesome-chicken-3-lbs#pv=94525";
//		String url ="http://www.vitacost.com/california-baby-super-sensitive-spf-30-sunscreen-no-fragrance";
//		String url ="http://www.vitacost.com/kong-air-dog-squeakeair-ball-small-dog-toy";
//		String url = "http://www.vitacost.com/vitacost-extra-virgin-certified-organic-coconut-oil-54-fl-oz-22";
//		String url ="http://www.vitacost.com/vitacost-extra-virgin-certified-organic-coconut-oil-54-fl-oz-22";
//		String url ="http://www.vitacost.com/california-baby-super-sensitive-spf-30-sunscreen-no-fragrance";
//		String url ="http://www.vitacost.com/optimum-nutrition-bcaa-1000-caps-1000-mg-400-capsules-1#pv=33593";
//		String url ="http://www.vitacost.com/prince-of-peace-premium-peony-white-tea-100-tea-bags-1";
//		String url ="http://www.vitacost.com/munchkin-baby-food-organizer-1-piece";
//		String url ="http://www.vitacost.com/halo-purely-for-pets-spots-stew-indoor-cat-formula-wholesome-chicken-11-5-lbs";
		String url ="http://www.vitacost.com/nature-made-super-b-complex-60-tablets-2";
		DetailParser_vitacost vita = new DetailParser_vitacost(url);
		System.out.println(vita.makeJSONString());
//		
		
		
//		DetailParser_vitacost vita = new DetailParser_vitacost(url,1); //���¼۸�
//		System.out.println(vita.upatePrice());
		long e =System.currentTimeMillis();
		long ssec = (e-b) % 1000;
		System.out.println("时间："+ssec);
	}
	
	public void getRefItem(String url) throws IOException, ParserException, ParseException {

		StringBuffer imgs = new StringBuffer();
		int myint =0;
		HttpURLConnection hrc = getHttpConnection(url);
		Parser parser = new Parser(hrc);
		parser.setEncoding("UTF-8");
		NodeFilter filter = new NodeClassFilter(CompositeTag.class);
		NodeList tags = parser.extractAllNodesThatMatch(filter);
		SimpleNodeIterator iter = tags.elements();
		CompositeTag tag = null;
		String nid="";
		while (iter.hasMoreNodes()) {
			tag = (CompositeTag) iter.nextNode();
			String id = tag.getAttribute("id");
			String cls = tag.getAttribute("class");
			if (tag instanceof Div) {
				Div div = (Div) tag;
				if(null != id && id.equalsIgnoreCase("pdTitleBlock")){//商品名称，sku,weight 区域
					SimpleNodeIterator iter1 = div.children();
					while (iter1.hasMoreNodes()) {
						Node node = iter1.nextNode();
						if(node instanceof BulletList){
							NodeList nodelist = node.getChildren();
							for (int i = 0; i < nodelist.size(); i++) {
								Node nodec = nodelist.elementAt(i);
								if(nodec instanceof Bullet){
									String str = ((Bullet) nodec).getStringText();
									if (str.contains("SKU")) {
										this.pid = str.substring(str.indexOf(":") + 1);
									}
								}
								
							}
						}
					}
				}
				
				String key = null;
				if (null != id && id.equalsIgnoreCase("productVariations")) {// 规格信息
					SimpleNodeIterator iter1 = div.children();
					while (iter1.hasMoreNodes()) {
						Node node = iter1.nextNode();
						if (node instanceof HeadingTag) {
							String str = ((HeadingTag) node).getStringText().trim();
							key = str.substring(0,str.indexOf(":"));
							String val = str.substring(str.indexOf(">") + 1,str.indexOf("</"));
							joSpec.put(key, val);
						}
						if(node instanceof BulletList){
							NodeList nodelist = node.getChildren();
							for (int i = 0; i < nodelist.size(); i++) {
								String str = null;
								String argv = null;
								Node nodec = nodelist.elementAt(i);
								if(nodec instanceof Bullet){
									str= ((Bullet) nodec).getStringText();
									int b = str.indexOf("p>")+2;
									int e =  str.indexOf("</p");
									str = str.substring(b, e);
									String argStr = ((Bullet) nodec).getAttribute("onclick");
									if(null != argStr){
										argv =  argStr.substring(argStr.indexOf("'/")+1,argStr.indexOf("')"));
									}else if(str.indexOf("href")>0){
										int b1 = str.indexOf("=")+2;
										int e1 = str.indexOf("\">");
										argv = str.substring(b1,e1);
										int b2 = str.indexOf("\">")+2;
										int e2 = str.indexOf("</");
										str = str.substring(b2,e2);
									}
								}
								JSONObject jso= new JSONObject();
								if(null != key && null != argv){
									jso.put(key, str);
									jso.put("argv", "http://www.vitacost.com"+argv);
									refItems.put(jso);
								}
							
							}
						}
					}
				}
				if (null != id && id.equalsIgnoreCase("RSTR_TopBlock")) {
					SimpleNodeIterator iter1 = div.children();
					while (iter1.hasMoreNodes()) {
						Node node = iter1.nextNode();
						if (node instanceof TagNode) {
							TagNode tn = (TagNode) node;
							if (tn.getAttribute("class").equalsIgnoreCase(
									"RSTR_TopRetail_Product")) {
								NodeList nl = tn.getChildren();
								for (int k = 0; k < nl.size(); k++) {
									Node n = nl.elementAt(k);
									if (n instanceof TagNode) {
										TagNode tnc = (TagNode) n;
										if (tnc.getAttribute("class")
												.equalsIgnoreCase(
														"pRetailPrice")) {
											String Retail_price = tnc
													.getFirstChild().getText();
											this.listPrice =  ParserUtils
													.trimChars(
															Retail_price,
															"Retail price:$");
										}
									}
								}
							}
							if (tn.getAttribute("class").equalsIgnoreCase(
									"RSTR_TopValue_Product")) {
								NodeList nl = tn.getChildren();
								for (int k = 0; k < nl.size(); k++) {
									Node n = nl.elementAt(k);
									if (n instanceof TagNode) {
										TagNode tnc = (TagNode) n;
										if (tnc.getAttribute("class")
												.equalsIgnoreCase("pOurPriceM")) {
											String pOurPriceM = tnc
													.getFirstChild().getText();
											this.price = ParserUtils
													.trimChars(
															pOurPriceM,
															"Vitacost price:$");
										}
									}
								}
							}
						}
					}

				}
			}
		}
	}

	public void getContent(String url) throws IOException, ParserException, ParseException {
		StringBuffer imgs = new StringBuffer();
		int myint =0;
		HttpURLConnection hrc = getHttpConnection(url);
		Parser parser = new Parser(hrc);
		parser.setEncoding("UTF-8");
		NodeFilter filter = new NodeClassFilter(CompositeTag.class);
		NodeList tags = parser.extractAllNodesThatMatch(filter);
		SimpleNodeIterator iter = tags.elements();
		CompositeTag tag = null;
		String nid="";
		while (iter.hasMoreNodes()) {
			tag = (CompositeTag) iter.nextNode();
			String id = tag.getAttribute("id");
			String cls = tag.getAttribute("class");
			if (tag instanceof Div) {
				Div div = (Div) tag;
				if(null != id && id.equalsIgnoreCase("pdTitleBlock")){//商品名称，sku,weight 区域
					SimpleNodeIterator iter1 = div.children();
					while (iter1.hasMoreNodes()) {
						Node node = iter1.nextNode();
						if (node instanceof HeadingTag) {
							String str = ParserUtils.trimSpaces(((HeadingTag) node).getStringText(),
									"&nbsp;");
							this.pname = str;
						}
						if(node instanceof BulletList){
							NodeList nodelist = node.getChildren();
							for (int i = 0; i < nodelist.size(); i++) {
								Node nodec = nodelist.elementAt(i);
								if(nodec instanceof Bullet){
									String str = ((Bullet) nodec).getStringText();
									if (str.contains("SKU")) {
										this.pid = str.substring(str.indexOf(":") + 1);
									}
									if (str.contains("Count")) {
										String v = str.substring(str.indexOf(":") + 1);
									}
									if (str.contains("Weight")) {
										String gwei = str.substring(str.indexOf(":") + 1);
										//0.22 lb
										String regex ="([\\d]+[/.]?[\\d]+)[\\s]*([\\w]*)";
										Pattern pattern = Pattern.compile(regex);   
										Matcher m = pattern.matcher(gwei);  
										while(m.find()){  
											String o = m.group(1);
											StringBuffer sb = new StringBuffer();
											if(null != o){
												sb.append(o);
											}
											String t = m.group(2);
											if(null != t){
												sb.append("|");
												sb.append(t);
											}
											this.weight = sb.toString();
										}  
										
									}
								}
								
							}
						}
					}
				}
				String key = null;
				if (null != id && id.equalsIgnoreCase("productVariations")) {// 规格信息
					SimpleNodeIterator iter1 = div.children();
					while (iter1.hasMoreNodes()) {
						Node node = iter1.nextNode();
						if (node instanceof HeadingTag) {
							String str = ((HeadingTag) node).getStringText().trim();
							key = str.substring(0,str.indexOf(":"));
							String val = str.substring(str.indexOf(">") + 1,str.indexOf("</"));
							joSpec.put(key, val);
							
						}
						if(node instanceof BulletList){
							NodeList nodelist = node.getChildren();
							for (int i = 0; i < nodelist.size(); i++) {
								String str = null;
								String argv = null;
								Node nodec = nodelist.elementAt(i);
								if(nodec instanceof Bullet){
									str= ((Bullet) nodec).getStringText();
									int b = str.indexOf("p>")+2;
									int e =  str.indexOf("</p");
									str = str.substring(b, e);
									
									String argStr = ((Bullet) nodec).getAttribute("onclick");
									
									if(null != argStr){
										argv =  argStr.substring(argStr.indexOf("'/")+1,argStr.indexOf("')"));
									}else if(str.indexOf("href")>0){
										int b1 = str.indexOf("=")+2;
										int e1 = str.indexOf("\">");
										argv = str.substring(b1,e1);
										int b2 = str.indexOf("\">")+2;
										int e2 = str.indexOf("</");
										str = str.substring(b2,e2);
									}
								}
								JSONObject jso= new JSONObject();
								if(null != key && null != argv){
									jso.put(key, str);
									jso.put("argv", "http://www.vitacost.com"+argv);
									refItems.put(jso);
								}
							
							}
							JSONObject jso= new JSONObject();
							jso.put(key, joSpec.get(key));
							jso.put("argv", url);
							refItems.put(jso);
						}
					}
				}
				if (null != id && id.equalsIgnoreCase("RSTR_TopBlock")) {
					SimpleNodeIterator iter1 = div.children();
					while (iter1.hasMoreNodes()) {
						Node node = iter1.nextNode();
						if (node instanceof TagNode) {
							TagNode tn = (TagNode) node;
							if (tn.getAttribute("class").equalsIgnoreCase(
									"RSTR_TopRetail_Product")) {
								NodeList nl = tn.getChildren();
								for (int k = 0; k < nl.size(); k++) {
									Node n = nl.elementAt(k);
									if (n instanceof TagNode) {
										TagNode tnc = (TagNode) n;
										if (tnc.getAttribute("class")
												.equalsIgnoreCase(
														"pRetailPrice")) {
											String Retail_price = tnc
													.getFirstChild().getText();
											this.listPrice =  ParserUtils
													.trimChars(
															Retail_price,
															"Retail price:$");
										}
									}
								}
							}
							if (tn.getAttribute("class").equalsIgnoreCase(
									"RSTR_TopValue_Product")) {
								NodeList nl = tn.getChildren();
								for (int k = 0; k < nl.size(); k++) {
									Node n = nl.elementAt(k);
									if (n instanceof TagNode) {
										TagNode tnc = (TagNode) n;
										if (tnc.getAttribute("class")
												.equalsIgnoreCase("pOurPriceM")) {
											String pOurPriceM = tnc
													.getFirstChild().getText();
											this.price = ParserUtils
													.trimChars(
															pOurPriceM,
															"Vitacost price:$");
										}
										if (tnc.getAttribute("class")
												.equalsIgnoreCase(
														"RSTR_TopInStock_Product")) {
											String pBuyMsgOOS = tnc
													.getFirstChild()
													.getFirstChild().getText();
											this.stock = pBuyMsgOOS;
										}
									}
								}
							}
						}
					}

				}
				if (null != id && id.equalsIgnoreCase("productDetails")) {//����
					Lexer lexer = new Lexer(div.toPlainTextString());// ȥ����html���
					StringBuffer sb = new StringBuffer();
					for(Node node = null;(node = lexer.nextNode())!=null;){
						if(node instanceof RemarkNode){//ע��
							
						}
						else if(node instanceof TextNode){ // �ı�
							sb.append(node.getText());
						}
						else{ //��ǩ
							TagNode tagNode = (TagNode) node;
							tagNode.removeAttribute("class");
							sb.append(tagNode.getText());
						}
						
					}
					String str = ParserUtils.removeEscapeCharacters(sb.toString());
					this.intro = this.B64Encoder.encode(str.getBytes());

				}
				if(null != cls && (cls.equalsIgnoreCase("RSTL_Left_Product") || cls.equalsIgnoreCase("czMnZCnr") || cls.equalsIgnoreCase("pd-column-1"))){//ͼƬ��Ϣ
					NodeList nodeList = div.getChildren();
					// ���еĽڵ�
					// ����һ���ڵ�filter���ڹ��˽ڵ�
					NodeFilter filte = new TagNameFilter("IMG");
					// �õ����й��˺���Ҫ�Ľڵ�
					nodeList = nodeList.extractAllNodesThatMatch(filte, true);
					for (int i = 0; i < nodeList.size(); i++) {
						ImageTag img = (ImageTag)nodeList.elementAt(i);
						String iurl = img.getImageURL();
						if(iurl.contains("/Images/Products/")){//��ȡ��ƷͼƬ
							if(imgs.indexOf(iurl)==-1){
								imgs.append(img.getImageURL());
							}
							
						}
					}
				}
			}
			if (tag instanceof ScriptTag) {
				ScriptTag script = (ScriptTag)tag;
				readScript(script);
			}
		}
		this.images = imgs.toString();
	}
	
	private void readScript(ScriptTag script) throws ParserException, IOException, ParseException {
	     String code = script.getScriptCode();
	     StringReader sr = new StringReader(code);
	     boolean bBlank = true;
	     boolean bFirstLine = true;
	     StringBuilder sb = new StringBuilder();
	     int x = -1;
	     String line = null;
	     while ((x = sr.read()) != -1) {
	       char c = (char)x;
	       if (bBlank) {
	         if ( (c == '\n') || (c == '\r')) //(c == ' ') ||
	         {
	           continue;
	         }
//	         bBlank = false;
//	         if (c != '/')
//	         {
//	           return null;
//	         }
	       }
	 
	       if ((c != '\n') && (c != '\r')) {
	         sb.append(c);
	       }
	 
	       if (c == ';')
	       {
	         line = sb.toString();
	         sb = new StringBuilder();
	         if (bFirstLine) {
	           bFirstLine = false;
	           if (!line.startsWith("<!--")) {
	             return;
	           }
	         }
	         String[] strs = splitKV(line);
	    
	         if (strs == null)
	         {
	           continue;
	         }
	         if (strs[0].contains("vPBrandName")) {
	        	 this.brand = strs[1];
	        	 
	         }
	         if(strs[0].contains("vBreadcrumbs")){
	        	 this.category =strs[1];
	         }
	       }
	     }
	     return;
	}
	
	
	 private String[] splitKV(String line) {
	     int idx = line.indexOf(61);
	     if ((idx <= 0) || (idx >= line.length() - 1)) {
	       return null;
	     }
	     String[] strs = new String[2];
	     String str0 = line.substring(0, idx).trim();
	     int idx2 = str0.indexOf(32);
	     if (idx2 > 0) {
	       strs[0] = str0.substring(idx2 + 1).trim();
	     }
	     else {
	       strs[0] = str0.trim();
	     }
	     String str1 = line.substring(idx + 1, line.length() - 1).trim();
	     if (str1.startsWith("\""))
	     {
	       strs[1] = str1.replace("\"", "");
	     }
	     else {
	       strs[1] = str1;
	     }
	     return strs;
	   }

	public static synchronized HttpURLConnection getHttpConnection(String url)
			throws IOException {
		URL u = new URL(url);
		HttpURLConnection conn = null;
		if (proxy != null) {
			conn = (HttpURLConnection) u.openConnection(proxy);
		} else {
			conn = (HttpURLConnection) u.openConnection();
		}
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(30000);
		return conn;
	}
}
