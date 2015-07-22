package com.taohai.vitacost;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserUtils;
import org.htmlparser.util.SimpleNodeIterator;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mysql.jdbc.StringUtils;
import com.sun.xml.internal.ws.wsdl.parser.ParserUtil;

public class StationCrawler_vitacost {

	private static final String HOME_URL = "http://www.vitacost.com";
	private static InetSocketAddress addr = new InetSocketAddress(
			"web-proxy.oa.com", 8080);
	private static Proxy proxy = null;
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	private Connection conn = null;
	private static final String INS_SQL = "INSERT INTO self_products(goods_id,products_no,spec_array,store_nums,market_price,sell_price,cost_price, weight, tiji, tiji_cost, fix_weight, sku, source_url, img,paiyunimg,LENGTH,width, height, innerShipCost,is_self_salse) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private List<String> categories = new ArrayList<String>();
	private List<String> details = new ArrayList<String>();
	static String DIR = "d://data/vitacost0425/";

	public static void main_bak(String[] args) throws ParseException, IOException {
		int argLen = args.length;
		if (argLen < 2) {
			System.err.println("Usage: -s/-w params...");
			System.exit(0);
		}
		StationCrawler_vitacost sc;
		if (args[0].equalsIgnoreCase("-s")) {
			try {
				DetailParser_vitacost dp = new DetailParser_vitacost(args[1]);
				JSONObject jo = dp.makeJSON();
				System.out.println(jo.toString());
			} catch (ParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (args[0].equalsIgnoreCase("-w")) {
			if (argLen < 6) {
				System.err
						.println("Usage: -w host port database user password");
				System.exit(0);
			}
			String host = args[1];
			int port = Integer.parseInt(args[2]);
			String db = args[3];
			String user = args[4];
			String pswd = args[5];
			sc = new StationCrawler_vitacost(host, port, db, user, pswd);
		}
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
		conn.setConnectTimeout(3000000);
		conn.setReadTimeout(3000000);
		return conn;
	}

	public StationCrawler_vitacost() throws ClassNotFoundException,
			SQLException {
		String host = "127.0.0.1";
		int port = 3306;
		String db = "6pm";
		String user = "6pm";
		String pswd = "6pm";
		this.conn = getConnection(host, port, db, user, pswd);
		this.conn.setAutoCommit(false);
	}

	public StationCrawler_vitacost(String host, int port, String db,
			String user, String pswd) throws IOException {
		try {
			this.conn = getConnection(host, port, db, user, pswd);
			this.conn.setAutoCommit(false);
			categories = getCategories("http://www.vitacost.com/Categories");
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			return;
		} catch (ParserException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		int catSize = categories.size();// 分类的数量
		int cc = 0;
		for (String cat : categories) {
			cc++;
			//生成类目文件夹
			File f = new File(DIR + "Categories_" + cat);
			if (!f.exists()) {
//				f.createNewFile();
				f.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(f);
//			fos.write(str.getBytes());
			fos.write(cat.getBytes());
			fos.flush();
			fos.close();
			System.out.println(new StringBuilder().append("[").append(cc)
					.append("/").append(catSize).append("] ").append(cat)
					.toString());
			try {
				List<String> details = getDetailPages(cat);// 每个分类的详细页集合
				int pageSize = categories.size();
				int pc = 0;
				JSONObject jo = null;
				for (String d : details) {
					if(pc>1){
						break;
					}
					pc++;
					File cf = new File(f.getPath() + "Details_" + d);
					if (!cf.exists()) {
						cf.createNewFile();
					}
					FileOutputStream cfos = new FileOutputStream(cf);
//					fos.write(str.getBytes());
					cfos.write(d.getBytes());
					cfos.flush();
					cfos.close();
					
					System.out.println(new StringBuilder().append("--[")
							.append(pc).append("/").append(pageSize)
							.append("] ").append(d).toString());
					try {
						 DetailParser_vitacost dp = new
						 DetailParser_vitacost(d);//详细页的信息抓取
						 jo = dp.makeJSON();
						 insertProduct(jo);
					}
					 catch (ParserException e) {
					 System.err.println(e.getMessage());
					 }
					 catch (IOException e) {
					 System.err.println(e.getMessage());
					 }
					 catch (SQLException e) {
					 System.err.println(e.getMessage());
					 }
					catch (Exception e) {
						System.err.println(e.getMessage());
					}

				}

				Thread.sleep(30000L);
			} catch (ParserException e1) {
				System.err.println(e1.getMessage());
			} catch (IOException e1) {
				System.err.println(e1.getMessage());
			} catch (InterruptedException e) {
			}
		}
		try {
			this.conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void insertProduct(JSONObject jo) throws SQLException {
		PreparedStatement pstmt = this.conn
				.prepareStatement("INSERT INTO self_products_vitacost(goods_id,products_no,spec_array,store_nums,market_price,sell_price,cost_price, weight, tiji, tiji_cost, fix_weight, sku, source_url, img,paiyunimg,LENGTH,width, height, innerShipCost,is_self_salse) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		JSONObject joItem = jo.getJSONObject("item");
		JSONArray ref_items = jo.getJSONArray("ref_items");
		int ic =0;
		if(null != ref_items){
			ic = ref_items.length();
		}
		StringBuffer sbf = new StringBuffer();
		for (int i = 0; i < ic; i++) {//规格信息
			sbf.append(ref_items.toString());
		}
		pstmt.setInt(1, joItem.getInt("-pid"));
		pstmt.setString(2, "-");
//		pstmt.setString(3, joItem.getJSONObject("-spec_tag").toString()); // 多规格字段值 
		pstmt.setString(3, sbf.toString());// 多规格字段值 
		
		String stock = joItem.getString("-stock");
		int stockNum =0;
		if(stock.equalsIgnoreCase("In stock")){
			stockNum =999;
		}
		pstmt.setInt(4, stockNum);
		if(StringUtils.isNullOrEmpty(joItem.getString("-list_price"))){
			pstmt.setDouble(5, 0);
		}else{
			pstmt.setDouble(5, joItem.getDouble("-list_price"));
		}
		double price =0;
		if(StringUtils.isNullOrEmpty(joItem.getString("-price"))){
			pstmt.setDouble(6, price);
		}else{
			price = joItem.getDouble("-price");
			pstmt.setDouble(6, price);
		}
		pstmt.setDouble(7, price);
		pstmt.setString(8, joItem.getString("-weight"));
		pstmt.setDouble(9, 0.0D);
		pstmt.setDouble(10, 0.0D);
		pstmt.setDouble(11, 0.0D);
		pstmt.setString(12, joItem.getString("-pid"));
		pstmt.setString(13, jo.getString("source_url"));
		String imgs = joItem.getString("-images");
//		if (imgs != null) {
//			StringBuilder sb = new StringBuilder();
//			int c = imgs.length();
//			for (int k = 0; k < c; k++) {
//				if (k > 0) {
//					sb.append("|");
//				}
//				sb.append(imgs.getString(k));
//			}
		pstmt.setString(14, imgs);
//		}
		pstmt.setString(15, "");
		pstmt.setInt(16, 0);
		pstmt.setInt(17, 0);
		pstmt.setInt(18, 0);
		String ship = joItem.getString("-freight");
		double shipCost = 0.0D;
		if (!ship.toUpperCase().contains("FREE")) {
			shipCost = Double.parseDouble(ship);
		}
		pstmt.setDouble(19, shipCost);
		pstmt.setInt(20, 1);

		pstmt.addBatch();
		int[] cnt = pstmt.executeBatch();
		pstmt.close();
		this.conn.commit();
	}

	private Connection getConnection(String host, int port, String db,
			String user, String pswd) throws ClassNotFoundException,
			SQLException {
		String connStr = new StringBuilder()
				.append("jdbc:mysql://")
				.append(host)
				.append(":")
				.append(port)
				.append("/")
				.append(db)
				.append("?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&rewriteBatchedStatements=true")
				.toString();

		Class.forName("com.mysql.jdbc.Driver");

		// Class.forName("com.mysql.jdbc.Driver");
		// Connection
		// con=DriverManager.getConnection("jdbc:mysql://localhost:3306/karthicraj","mysql","mysql");

		return DriverManager.getConnection(connStr, user, pswd);
	}

	public HashSet<String> getLinksText(String url) throws IOException,
			ParserException {
		ArrayList<LinkTag> links = getLinks(url);
		HashSet texts = new HashSet();
		for (LinkTag lt : links) {
			String link = lt.extractLink();
			if (link.startsWith("http://www.vitacost.com")) {
				texts.add(link);
			}
		}
		return texts;
	}

	// 获取所有的分类页地址连接
	public List<String> getCategories(String home) throws ParserException,
			IOException {
		ArrayList<LinkTag> links = getLinks(home);
		for (LinkTag lt : links) {
			String cls = lt.getAttribute("class");
			String id = lt.getAttribute("id");
			if (null == cls && null == id) {
				if (null != lt.getLink() && lt.getLink().length() > 5)
					categories.add(lt.getLink());
				System.out.println(lt.getLink());
			}
			// if (cls.startsWith("gae-click*Homepage*Category-Navigation")) {
			// categories.add(lt.getLink());
			// }
		}
		// for(String cat : categories){
		// HttpURLConnection hrc = getHttpConnection(cat);
		// Parser parser = new Parser(hrc);
		// parser.setEncoding("UTF-8");
		//
		// NodeFilter filter = new NodeClassFilter(LinkTag.class);
		// NodeList list = parser.extractAllNodesThatMatch(filter);
		// SimpleNodeIterator iter = list.elements();
		// while(iter.hasMoreNodes()){
		// Node node = iter.nextNode();
		//
		// }
		// }
		HashSet h = new HashSet(categories);
		categories.clear();
		categories.addAll(h);
		return categories;
	}

	public static void main(String[] args) throws IOException {
//		String host = "127.0.0.1";
//		int port = 3690;
//		String db = "6pm";
//		String user = "6pm";
//		String pswd = "6pm";
		StationCrawler_vitacost vita = null;
		List<String> list = null;
		try {
			try {
				vita = new StationCrawler_vitacost();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			list = vita.getCategories("http://www.vitacost.com/Categories");
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("获取类别数------------------------------" + list.size());
		int catSize = vita.categories.size();// 分类的数量
		int cc = 0;

//		try {
//			List<String> details = vita
//					.getDetailPages("http://www.vitacost.com/productResults.aspx?N=32+1300532&No=0&Ns=P_SoldQuantity%7c1");
//			System.out.println("商品详细页数量："+details.size());
//		} catch (ParserException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		 //每个分类的详细页集合
		for (Iterator<String> it = list.iterator(); it.hasNext();) {
			String cat = it.next().toString();
			cc++;
			File f = new File(DIR + "Categories_" + cc);
			if (!f.exists()) {
//				f.createNewFile();
				f.mkdirs();
			}
//			FileOutputStream fos = new FileOutputStream(f);
//			fos.write(str.getBytes());
//			fos.write(cat.getBytes());
//			fos.flush();
//			fos.close();
			System.out.println(new StringBuilder().append("[").append(cc)
					.append("/").append(catSize).append("] ").append(cat)
					.toString());
			try {
				List<String> details = vita.getDetailPages(cat);// 每个分类的详细页集合
				System.out.println(new StringBuilder().append("-----[")
						.append(cc).append("/").append(details.size())
						.append("] ").toString());
				int pageSize = vita.categories.size();
				int pc = 0;
				JSONObject jo = null;
				for (String d : details) {
					pc++;
					File cf = new File(f.getPath() + "Details_" + pc);
					if (!cf.exists()) {
						cf.createNewFile();
					}
					FileOutputStream cfos = new FileOutputStream(cf);
					cfos.write(cat.getBytes());
					cfos.write(d.getBytes());
					cfos.flush();
					cfos.close();
					System.out.println(new
							StringBuilder().append("--------[").append(pc)
									.append("/").append(details.size()).append("] ")
									.append(d).toString());
					try {
						 DetailParser_vitacost dp = new  DetailParser_vitacost(d);//详细页的信息抓取
						 jo = dp.makeJSON();
						 vita.insertProduct(jo);
					}catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

	}

	public ArrayList<LinkTag> getLinks(String url) throws ParserException,
			IOException {
		ArrayList links = new ArrayList();
		HttpURLConnection hrc = getHttpConnection(url);
		Parser parser = new Parser(hrc);
		parser.setEncoding("UTF-8");

		NodeFilter filter = new NodeClassFilter(LinkTag.class);
		NodeList list = parser.extractAllNodesThatMatch(filter);
		SimpleNodeIterator iter = list.elements();
		LinkTag lt = null;
		while (iter.hasMoreNodes()) {
			lt = (LinkTag) iter.nextNode();
			links.add(lt);
		}
		return links;
	}

	public ArrayList<LinkTag> getCategoryLinks(String url)
			throws ParserException, IOException {
		ArrayList links = new ArrayList();
		HttpURLConnection hrc = getHttpConnection(url);
		Parser parser = new Parser(hrc);
		parser.setEncoding("UTF-8");

		NodeFilter filter = new NodeClassFilter(LinkTag.class);
		NodeList list = parser.extractAllNodesThatMatch(filter);
		SimpleNodeIterator iter = list.elements();
		LinkTag lt = null;
		while (iter.hasMoreNodes()) {
			lt = (LinkTag) iter.nextNode();
			links.add(lt);
		}
		return links;
	}

	// 获取每个分类的详细列表页 srListingNavPages //http://www.vitacost.com/saw-palmetto-5
	// http://www.vitacost.com/productResults.aspx?N=32+1300532&No=0&Ns=P_SoldQuantity%7c1
	// 多翻页数据
	public List<String> getDetailPages(String category) throws ParserException,
			IOException {
		ArrayList<String> urls = new ArrayList<String>();
		urls = getUrlPages(category, urls, 1);
		System.out.println("URL Page ==============" + urls.size());
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < urls.size(); i++) {
			List temp = new ArrayList();
			ArrayList<LinkTag> links = getLinks(urls.get(i));
			for (LinkTag lt : links) {
				String id = lt.getAttribute("id");
				String cls = lt.getAttribute("class"); // pltgR
				if (cls == null && null == id) {
					continue;
				}
				if (null != cls && cls.equalsIgnoreCase("pNameM cf")) {
					temp.add(lt.getLink());// 商品详细页
				}
			}
			System.out.println("---------------" + i + "page: ----------"
					+ temp.size());
			list.addAll(temp);
		}
		// srListingNavPages
		// HashSet h = new HashSet(list);
		// list.clear();
		// list.addAll(h);
		return list;
	}

	// 分页的连接数
	public ArrayList<String> getUrlPages(String category,
			ArrayList<String> urls, int i) throws ParserException, IOException {
		ArrayList<LinkTag> links = getLinks(category);
		urls.add(category);
		for (LinkTag lt : links) {
			String id = lt.getAttribute("id");
			if (null != id
					&& id.startsWith("IamMasterFrameYesIam_ctl02_EndecaTopPagination_pages")) {// 分页的链接
				String url = lt.getLink();
				if (!urls.contains(url)) {// 去除重复的数据
					if (null != url && url.trim().length() > 0) {
						url = url.replace("amp;", "");
						urls.add(url);

					}
				}
			}
			if (null != id
					&& id.startsWith("IamMasterFrameYesIam_ctl02_EndecaTopPagination_nextElipse")) {
				String url = lt.getLink();
				if (null != url && url.trim().length() > 0) {
					url = lt.getLink().replace("amp;", "");
					getUrlPages(url, urls, i);// 获取新的分页地址的信息
				}

			}
		}
		return urls;
	}

}
