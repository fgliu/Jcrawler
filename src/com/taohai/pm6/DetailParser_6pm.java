package com.taohai.pm6;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.BulletList;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LabelTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;
import org.json.JSONArray;
import org.json.JSONObject;

import sun.misc.BASE64Encoder;

public class DetailParser_6pm {
	private HashMap<String, ArrayList<String>> pImgMV = new HashMap();
	private HashMap<String, String> colorIds = new HashMap();
	private JSONObject colorNames = null;
	private HashMap<String, JSONArray> dimIds = new HashMap();
	private HashMap<String, String> dimNames = new HashMap();
	private HashMap<String, String> dimValues = new HashMap();

	private String pid = "";
	private String pname = "";
	private String category = "";
	private String subCategory = "";
	private String brand = "";
	private String weight = "";
	private String freight = "";
	private String intro = "";
	private JSONArray items = null;

	private BASE64Encoder B64Encoder = new BASE64Encoder();
	private String url;

	public DetailParser_6pm(String url) throws ParserException, IOException,
			ParseException {
		this.url = url;
		scanPage();
	}

	public DetailParser_6pm(String url, int i) throws ParserException,
			IOException, ParseException {
		this.url = url;
		updatePage();
	}

	public String makeJSONString() {
		JSONObject jo = makeJSON();
		if (jo != null) {
			return jo.toString();
		}
		return "";
	}

	public JSONArray updatePrice() {
		JSONArray myitem = new JSONArray();
		if(null != this.items && this.items.length()>0){
			int len = this.items.length();
			for (int i = 0; i < len; i++) {
				JSONObject jo = (JSONObject)this.items.get(i);
				JSONObject t = new JSONObject();
				t.put("sku", jo.get("item_id"));
				t.put("price", jo.get("price"));
				myitem.put(t);
			}
		}
		
		return myitem;
	}

	private void updatePage() throws IOException, ParserException,
			ParseException {

		HttpURLConnection hrc = StationCrawler_6pm.getHttpConnection(this.url);
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
			if (!(tag instanceof SelectTag)) // 替换上行代码
			{
				if ((this.items == null) && ((tag instanceof ScriptTag))) { // 替换上行代码
					this.items = readPriceScript((ScriptTag) tag);
				}
			}
		}
	}

	public JSONArray readPriceScript(ScriptTag script) throws ParserException,
			IOException, ParseException {

		JSONObject colorPrices = null;
		JSONArray stock = null;

		String code = script.getScriptCode();
		StringReader sr = new StringReader(code);
		boolean bBlank = true;
		boolean bFirstLine = true;
		StringBuilder sb = new StringBuilder();
		int x = -1;
		String line = null;
		while ((x = sr.read()) != -1) {
			char c = (char) x;
			if (bBlank) {
				if ((c == ' ') || (c == '\n') || (c == '\r')) {
					continue;
				}
				bBlank = false;
				if (c != '/') {
					return null;
				}
			}

			if ((c != '\n') && (c != '\r')) {
				sb.append(c);
			}

			if (c == ';') {
				line = sb.toString();
				sb = new StringBuilder();
				if (bFirstLine) {
					bFirstLine = false;
					if (!line.startsWith("// Product page private namespace")) {
						return null;
					}
				}
				String[] strs = splitKV(line);
				if (strs == null) {
					continue;
				}
				 else if (strs[0].equalsIgnoreCase("colorPrices")) {
				 colorPrices = new JSONObject(strs[1]);
				 }
				 if (strs[0].equalsIgnoreCase("stockJSON")) {
				 stock = new JSONArray(strs[1]);
				 }

			}

		}

		JSONArray items = new JSONArray();
		int sc = stock.length();
		for (int k = 0; k < sc; k++) {
			JSONObject j = stock.getJSONObject(k);
			String colorId = j.getString("color");
			JSONObject joItem = new JSONObject();

			joItem.put("item_id", j.getString("id"));
			if (colorPrices.has(colorId)) {
				JSONObject joPrice = colorPrices.getJSONObject(colorId);
				joItem.put("price", joPrice.getDouble("nowInt"));
				// joItem.put("list_price", joPrice.getDouble("wasInt"));
			} else {
				joItem.put("price", 0);
				joItem.put("list_price", 0);
			}

			items.put(joItem);
		}
		return items;

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
		joData.put("title", new StringBuilder().append(this.brand).append(" ")
				.append(this.pname).toString());
		joData.put("intor", this.intro);
		joData.put(
				"category",
				new StringBuilder().append(this.category).append(">")
						.append(this.subCategory).append(">")
						.append(this.brand).toString());
		joData.put("currency", "$");

		JSONObject joSpec = new JSONObject();
		JSONArray joColorSpec = new JSONArray();
		Iterator iterColor = this.colorIds.entrySet().iterator();
		Map.Entry entColor = null;
		while (iterColor.hasNext()) {
			entColor = (Map.Entry) iterColor.next();
			joColorSpec.put(this.colorNames.get((String) entColor.getKey()));
		}
		joSpec.put("Color", joColorSpec);

		Iterator iterDim = this.dimIds.entrySet().iterator();
		Map.Entry entDim = null;
		while (iterDim.hasNext()) {
			entDim = (Map.Entry) iterDim.next();
			JSONArray joSizeSpec = new JSONArray();
			String dimName = (String) this.dimNames.get(entDim.getKey());
			// dimName = dimName.replace("'", "\\'");
			JSONArray array = (JSONArray) entDim.getValue();
			int ac = array.length();
			for (int a = 0; a < ac; a++) {
				String di = String.valueOf(array.getInt(a));
				joSizeSpec.put(this.dimValues.get(di));
			}
			joSpec.put(dimName, joSizeSpec);
		}
		joData.put("spec_tags", joSpec);

		if (this.items != null) {
			int ic = this.items.length();
			for (int i = 0; i < ic; i++) {
				JSONObject joItem = this.items.getJSONObject(i);
				joItem.put("purl", this.url);
				joItem.put("weight", this.weight);
				joItem.put("freight", this.freight);
			}
			joData.put("items", this.items);
		}
		joData.put("recommend_purls", "");

		joProd.put("data", joData);
		return joProd;
	}

	private void scanPage() throws IOException, ParserException, ParseException {
		HttpURLConnection hrc = StationCrawler_6pm.getHttpConnection(this.url);
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
			if ((tag instanceof LinkTag)) {
				LinkTag lt = (LinkTag) tag;

				if (cls == null) {
					continue;
				}
				if (cls.startsWith("gae-click*Product-Page*Breadcrumb*Category")) {
					this.category = lt.getStringText();
					continue;
				}
				if (cls.startsWith("gae-click*Product-Page*Breadcrumb*Sub-Category")) {
					this.subCategory = lt.getStringText();
					continue;
				}
				if (cls.startsWith("gae-click*Product-Page*Breadcrumb*Brand")) {
					this.brand = lt.getStringText();
					continue;
				}
				if (cls.startsWith("gae-click*Product-Page*PrForm*Free-Shipping")) {
					this.freight = "Free Shipping!";
				} else if (cls.equalsIgnoreCase("link fn")) {
					this.pname = lt.getStringText();
					continue;
				}
			} else if ((tag instanceof LabelTag)) {
				LabelTag lt = (LabelTag) tag;
				if ((id != null) && (id.startsWith("label")) && (cls != null)
						&& (cls.startsWith("d"))) {
					String l = lt.getLabel();
					l = l.replace("\n", "");
					int idx = l.indexOf(40);
					if (idx > 0) {
						l = l.substring(0, idx);
					}
					this.dimNames.put(cls, l);
				}
			} else if (!(tag instanceof SelectTag)) {
				if ((tag instanceof Span)) {
					if ((id != null) && (id.equalsIgnoreCase("sku"))) {
						String sku = tag.getStringText();
						this.pid = sku.substring(sku.indexOf(35) + 1);
					}
				} else if ((tag instanceof Bullet)) {
					Bullet b = (Bullet) tag;
					String text = b.getStringText().trim();

					if (text.startsWith("Weight")) {
						int idx = text.indexOf(":");
						this.weight = text.substring(idx + 1).trim();
					}

				} else if ((tag instanceof Div)) {
					Div div = (Div) tag;
					if (cls == null) {
						continue;
					}
					if (cls.equalsIgnoreCase("description")) {
						StringBuilder sb = new StringBuilder();
						BulletList bullets = (BulletList) div.getChild(0);
						SimpleNodeIterator bls = bullets.elements();
						while (bls.hasMoreNodes()) {
							Node n = bls.nextNode();
							if ((n instanceof Bullet)) {
								Bullet bl = (Bullet) n;
								sb.append(bl.getStringText());
							}
						}
						this.intro = this.B64Encoder.encode(sb.toString()
								.getBytes());
					}
				} else if ((this.items == null) && ((tag instanceof ScriptTag))) {
					this.items = readScript((ScriptTag) tag);
				}
			}
		}
	}

	private JSONArray readScript(ScriptTag script) throws ParserException,
			IOException, ParseException {
		JSONObject colorPrices = null;
		JSONArray stock = null;

		String code = script.getScriptCode();
		StringReader sr = new StringReader(code);
		boolean bBlank = true;
		boolean bFirstLine = true;
		StringBuilder sb = new StringBuilder();
		int x = -1;
		String line = null;
		while ((x = sr.read()) != -1) {
			char c = (char) x;
			if (bBlank) {
				if ((c == ' ') || (c == '\n') || (c == '\r')) {
					continue;
				}
				bBlank = false;
				if (c != '/') {
					return null;
				}
			}

			if ((c != '\n') && (c != '\r')) {
				sb.append(c);
			}

			if (c == ';') {
				line = sb.toString();
				sb = new StringBuilder();
				if (bFirstLine) {
					bFirstLine = false;
					if (!line.startsWith("// Product page private namespace")) {
						return null;
					}
				}

				String[] strs = splitKV(line);
				if (strs == null) {
					continue;
				}
				if (strs[0].equalsIgnoreCase("colorNames")) {
					this.colorNames = new JSONObject(strs[1]);
				} else if (strs[0].equalsIgnoreCase("colorPrices")) {
					colorPrices = new JSONObject(strs[1]);
				} else if (strs[0].equalsIgnoreCase("stockJSON")) {
					stock = new JSONArray(strs[1]);
				} else if (strs[0].equalsIgnoreCase("colorIds")) {
					JSONObject j = new JSONObject(strs[1]);
					Iterator iter = j.keys();
					while (iter.hasNext()) {
						String k = (String) iter.next();
						int cid = j.getInt(k);
						this.colorIds.put(new StringBuilder().append("")
								.append(cid).toString(), k);
					}
				} else if ((strs[0].startsWith("pImgs["))
						&& (strs[0].contains("MULTIVIEW"))
						&& (!strs[0].contains("THUMBNAILS"))) {
					int idx1 = strs[0].indexOf(91);
					int idx2 = strs[0].indexOf(93);
					if ((idx1 > 0) && (idx2 > idx1)) {
						String cid = strs[0].substring(idx1 + 1, idx2);
						ArrayList imgs = (ArrayList) this.pImgMV.get(cid);
						if (imgs == null) {
							imgs = new ArrayList();
							this.pImgMV.put(cid, imgs);
						}
						imgs.add(strs[1].replace("'", ""));
					}

				} else if (strs[0].equalsIgnoreCase("dimToUnitToValJSON")) {
					JSONObject j = new JSONObject(strs[1]);
					Iterator iterDim = j.keys();
					while (iterDim.hasNext()) {
						String k = (String) iterDim.next();
						JSONObject vj = j.getJSONObject(k);
						String vk = (String) vj.keys().next();
						this.dimIds.put(k, vj.getJSONArray(vk));
					}
				} else if (strs[0].equalsIgnoreCase("valueIdToNameJSON")) {
					JSONObject j = new JSONObject(strs[1]);
					Iterator iterDim = j.keys();
					while (iterDim.hasNext()) {
						String k = (String) iterDim.next();
						JSONObject v = j.getJSONObject(k);
						this.dimValues.put(k, v.getString("value"));
					}
				}

			}

		}

		JSONArray items = new JSONArray();
		int sc = stock.length();
		for (int k = 0; k < sc; k++) {
			JSONObject j = stock.getJSONObject(k);
			String colorId = j.getString("color");
			JSONObject joItem = new JSONObject();

			joItem.put("item_id", j.getString("id"));
			joItem.put("merchant", "6pm");
			if (colorPrices.has(colorId)) {
				JSONObject joPrice = colorPrices.getJSONObject(colorId);
				joItem.put("price", joPrice.getDouble("nowInt"));
				joItem.put("list_price", joPrice.getDouble("wasInt"));
			} else {
				joItem.put("price", 0);
				joItem.put("list_price", 0);
			}
			joItem.put("stock", j.getInt("onHand"));
			JSONObject joSpec = new JSONObject();

			if (this.colorNames.has(colorId)) {
				joSpec.put("Color", this.colorNames.getString(colorId));
			} else {
				joSpec.put("Color", "");
			}

			for (Map.Entry ent : this.dimNames.entrySet()) {
				String dimId = (String) ent.getKey();
				String dimName = ((String) ent.getValue());
				// String dimName = ((String)ent.getValue()).replace("'",
				// "\\'");
				String dk = j.getString(dimId);
				if (dk != null) {
					String dv = (String) this.dimValues.get(dk);
					joSpec.put(dimName, dv);
				}
			}
			joItem.put("spec_tag", joSpec);

			String cid = (String) this.colorIds.get(colorId);
			JSONArray jimgs = new JSONArray();
			if (cid != null) {
				ArrayList<String> imgs = (ArrayList) this.pImgMV.get(cid);
				if (imgs != null) {
					for (String s : imgs) {
						if (s.length() > 2) {
							jimgs.put(s);
						}
					}
				}
			}
			joItem.put("images", jimgs);
			joItem.put("measure", "");

			items.put(joItem);
		}
		return items;
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
		} else {
			strs[0] = str0.trim();
		}
		String str1 = line.substring(idx + 1, line.length() - 1).trim();
		if (str1.startsWith("\"")) {
			strs[1] = str1.replace("\"", "");
		} else {
			strs[1] = str1;
		}
		return strs;
	}

	public static void main(String[] args) throws ParserException, IOException,
			ParseException {
		String url = "http://www.6pm.com/tommy-hilfiger-allcott-brown";
		// String url ="http://www.6pm.com/nine-west-smooch-black-leather";
		DetailParser_6pm pm = new DetailParser_6pm(url,1);
		System.out.println(pm.updatePrice());
		// System.out.println(pm.makeJSONString());

	}
}
