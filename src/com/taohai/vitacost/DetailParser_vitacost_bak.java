package com.taohai.vitacost;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserUtils;
import org.htmlparser.util.SimpleNodeIterator;
import org.json.JSONArray;
import org.json.JSONObject;

import sun.misc.BASE64Encoder;

public class DetailParser_vitacost_bak {
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
	
	public DetailParser_vitacost_bak(String url) throws ParserException,
			IOException, ParseException {
		this.url = url;
		getContent(this.url);
	}

	public String makeJSONString() {
		JSONObject jo = makeJSON();
		if (jo != null) {
			return jo.toString();
		}
		return "";
	}
	
	

	public JSONObject makeJSON() {
		JSONObject joProd = new JSONObject();
		joProd.put("status", 0);
		joProd.put("error", "");
		joProd.put("source_url", this.url);
		joProd.put("time", System.currentTimeMillis());

		JSONObject joData = new JSONObject();
		joData.put("-purl", this.url);
		joData.put("-pid", this.pid);
		joData.put("-id_type", "SKU");
		joData.put("-brand", this.brand);
		joData.put("-title", this.pname);
		joData.put("-intor", this.intro);
		joData.put("-category", this.category);
		joData.put("-currency", "$");
		joData.put("-spec_attr", joSpec);
		joData.put("-weight", this.weight);
		joData.put("-freight", this.freight);
		joData.put("-merchant", "www.vitacost.com");
		joData.put("-price", this.price);
		joData.put("-list_price", this.listPrice);
		joData.put("-stock", this.stock);
		joData.put("-images", this.images);//添加上图片
		joData.put("-measure", joMeasure);//为空值

		joProd.put("item", joData);
		joProd.put("-recommend_purls", "");
		joProd.put("ref_items", refItems);
		return joProd;
	}
	public static void main(String[] args) throws ParserException, IOException,
			ParseException {
//		String url = "http://www.vitacost.com/earth-mama-angel-baby-lotion-vanilla-orange-8-fl-oz";
//		String url = "http://www.vitacost.com/earth-mama-angel-baby-body-butter";
//		String url = "http://www.vitacost.com/earth-mama-angel-baby-body-butter-8-fl-oz";
//		String url ="http://www.vitacost.com/earth-mama-angel-baby-body-butter#pv=57872";
//		String url ="http://www.vitacost.com/earth-mama-angel-baby-earth-mama-bottom-balm-2-oz";
//		String url ="http://www.vitacost.com/pamelas-products-gluten-and-wheat-free-pancake-and-baking-mix";
//		String url ="http://www.vitacost.com/cytosport-muscle-milk-naturals-vanilla-2-47-lbs";
		String url ="http://www.vitacost.com/atkins-day-break-bar-apple-crisp";
		DetailParser_vitacost_bak vita = new DetailParser_vitacost_bak(url);
		System.out.println(vita.makeJSONString());
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
		while (iter.hasMoreNodes()) {
			tag = (CompositeTag) iter.nextNode();
			String id = tag.getAttribute("id");
			String cls = tag.getAttribute("class");
//			if (tag instanceof HeadingTag) {
//				HeadingTag html = (HeadingTag) tag;
//				if (null != cls && cls.equalsIgnoreCase("bcs cf")) {
//					SimpleNodeIterator bls = html.elements();
//					StringBuffer sb = new StringBuffer();
//					while (bls.hasMoreNodes()) {
//						Node n = bls.nextNode();
//						if (n instanceof LinkTag) {
//							String title = ((LinkTag) n).getLinkText();
//							sb.append(title);
//							if (bls.hasMoreNodes()) {
//								sb.append(">>");
//							}
//						}
//					}
//					this.category = sb.toString();
//					System.out.println("category---------------"
//							+ this.category);
//				}
//			}
			if (tag instanceof Div) {
				Div div = (Div) tag;
				if (null != cls
						&& cls.equalsIgnoreCase("RSTL_RightTitle_Product")) {
					this.pname = div.getStringText();
				}
				if (null != cls
						&& cls.equalsIgnoreCase("RSTL_RightCount_Product")) {
					SimpleNodeIterator bls = div.elements();
					while (bls.hasMoreNodes()) {
						Node node = bls.nextNode();
						NodeList list = node.getChildren();
						if (null != list && list.size() > 0) {
							String str = list.asString();
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

				// pdpvLabel 规格名称
				if (null != cls && cls.equalsIgnoreCase("pdpvLabel")) {
					SimpleNodeIterator iter1 = div.children();
					while (iter1.hasMoreNodes()) {
						Node node = iter1.nextNode();
						if (node instanceof TextNode) {
							String str = ParserUtils.trimSpaces(node.getText(),
									"&nbsp;");
							ks.add(str);
						}
					}
				}
				// "pdpvTextNotAvailable" 无库存的规格值
				if (null != cls && cls.equalsIgnoreCase("pdpvTextNotAvailable")) {
					String v = ParserUtils.removeEscapeCharacters(div
							.getStringText());
					vs.add(v);
					JSONObject jso= new JSONObject();
					jso.put(ks.get(0), v);
					jso.put("-argv", "");
					refItems.put(jso);
				}
				//pdpvTextSelected 有库存的选中规格值
				if (null != cls && cls.equalsIgnoreCase("pdpvTextSelected")) {
					String v = ParserUtils.removeEscapeCharacters(div
							.getStringText());
					vs.add(v);
					JSONObject jso= new JSONObject();
					jso.put(ks.get(0), v);
					refItems.put(jso);
					joSpec.put(ks.get(0),v);
					jso.put("-argv", this.url);

				}
				//pdpvTextAvailable 有库存的未选 中值 
				if (null != cls && cls.equalsIgnoreCase("pdpvTextAvailable")) {
					String v = ParserUtils.removeEscapeCharacters(div
							.getStringText());
					vs.add(v);
					JSONObject jso= new JSONObject();
					jso.put(ks.get(0), v);
					
					Node node =div.getParent().getPreviousSibling();
					if(node instanceof LinkTag){
						LinkTag lNode = (LinkTag)node;
						jso.put("-argv", lNode.getLink());
					}
					refItems.put(jso);
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
				if (null != id && id.equalsIgnoreCase("productDetails")) {//描述
					Lexer lexer = new Lexer(div.toPlainTextString());// 去除了html标记
					StringBuffer sb = new StringBuffer();
					for(Node node = null;(node = lexer.nextNode())!=null;){
						if(node instanceof RemarkNode){//注释
							
						}
						else if(node instanceof TextNode){ // 文本
							sb.append(node.getText());
						}
						else{ //标签
							TagNode tagNode = (TagNode) node;
							tagNode.removeAttribute("class");
							sb.append(tagNode.getText());
						}
						
					}
					String str = ParserUtils.removeEscapeCharacters(sb.toString());
					this.intro = this.B64Encoder.encode(str.getBytes());

				}
				if(null != cls && cls.equalsIgnoreCase("RSTL_Left_Product")){//图片信息
					NodeList nodeList = div.getChildren();
					// 所有的节点
					// 建立一个节点filter用于过滤节点
					NodeFilter filte = new TagNameFilter("IMG");
					// 得到所有过滤后，想要的节点
					nodeList = nodeList.extractAllNodesThatMatch(filte, true);
					for (int i = 0; i < nodeList.size(); i++) {
						ImageTag img = (ImageTag)nodeList.elementAt(i);
						String iurl = img.getImageURL();
						if(iurl.contains("/Images/Products/")){//获取商品图片
							imgs.append(img.getImageURL());
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
