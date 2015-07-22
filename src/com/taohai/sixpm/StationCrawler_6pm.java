package com.taohai.sixpm;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;
import org.json.JSONArray;
import org.json.JSONObject;

public class StationCrawler_6pm {
	private static final String HOME_URL = "http://www.6pm.com";
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	private Connection conn = null;
	private static final String INS_SQL = "INSERT INTO self_products(goods_id,products_no,spec_array,store_nums,market_price,sell_price,cost_price, weight, tiji, tiji_cost, fix_weight, sku, source_url, img,paiyunimg,LENGTH,width, height, innerShipCost,is_self_salse) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static void main(String[] args) throws ParseException {
		int argLen = args.length;
		if (argLen < 2) {
			System.err.println("Usage: -s/-p params...");
			System.exit(0);
		}
		StationCrawler_6pm sc;
		if (args[0].equalsIgnoreCase("-s")) {
			try {
				DetailParser_6pm dp = new DetailParser_6pm(args[1]);
				JSONObject jo = dp.makeJSON();
				System.out.println(jo.toString());
//				return jo.toString();
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
			sc = new StationCrawler_6pm(host, port, db, user, pswd);
		}
	}

	public StationCrawler_6pm(String host, int port, String db, String user,
			String pswd) throws ParseException {
		try {
			ArrayList<String> categories;
			int catSize;
			int cc;
			int pageSize;
			int pc;
			JSONObject jo;
			this.conn = getConnection(host, port, db, user, pswd);

			categories = getCategories("http://www.6pm.com");
			catSize = categories.size();
			cc = 0;
			for (String cat : categories) {
				cc++;
				System.out.println(new StringBuilder().append("[").append(cc)
						.append("/").append(catSize).append("] ").append(cat)
						.toString());

				ArrayList<String> details = getDetailPages(cat);
				pageSize = categories.size();
				pc = 0;
				jo = null;
				for (String d : details) {
					pc++;
					System.out.println(new StringBuilder().append("--[")
							.append(pc).append("/").append(pageSize)
							.append("] ").append(d).toString());

					DetailParser_6pm dp = new DetailParser_6pm(d);
					jo = dp.makeJSON();
					insertProduct(jo);
				}
			}
		} catch (ParserException e) {

			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (this.conn != null)
				try {
					this.conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	private void insertProduct(JSONObject jo) throws SQLException {
		PreparedStatement pstmt = this.conn
				.prepareStatement("INSERT INTO self_products(goods_id,products_no,spec_array,store_nums,market_price,sell_price,cost_price, weight, tiji, tiji_cost, fix_weight, sku, source_url, img,paiyunimg,LENGTH,width, height, innerShipCost,is_self_salse) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		JSONObject joData = jo.getJSONObject("data");
		JSONArray items = joData.getJSONArray("-items");
		int ic = items.length();
		JSONObject joItem = null;
		for (int i = 0; i < ic; i++) {
			joItem = items.getJSONObject(i);
			pstmt.setInt(1, joItem.getInt("-item_id"));
			pstmt.setString(2, "-");
			pstmt.setString(3, joItem.getJSONObject("-spec_tag").toString());
			pstmt.setInt(4, joItem.getInt("-stock"));
			pstmt.setDouble(5, joItem.getDouble("-list_price"));
			pstmt.setDouble(6, joItem.getDouble("-price"));
			pstmt.setDouble(7, joItem.getDouble("-price"));
			pstmt.setString(8, joItem.getString("-weight"));
			pstmt.setDouble(9, 0.0D);
			pstmt.setDouble(10, 0.0D);
			pstmt.setDouble(11, 0.0D);
			pstmt.setString(12, joData.getString("-pid"));
			pstmt.setString(13, jo.getString("source_url"));
			JSONArray imgs = joItem.getJSONArray("-images");
			StringBuilder sb = new StringBuilder();
			int c = imgs.length();
			for (int k = 0; k < c; k++) {
				if (k > 0) {
					sb.append("|");
				}
				sb.append(imgs.getString(k));
			}
			pstmt.setString(14, sb.toString());
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
		}
		int[] cnt = pstmt.executeBatch();
		pstmt.close();
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
		return DriverManager.getConnection(connStr, user, pswd);
	}

	public HashSet<String> getLinksText(String url) throws IOException,
			ParserException {
		ArrayList<LinkTag> links = getLinks(url);
		HashSet texts = new HashSet();
		for (LinkTag lt : links) {
			String link = lt.extractLink();
			if (link.startsWith("http://www.6pm.com")) {
				texts.add(link);
			}
		}
		return texts;
	}

	public ArrayList<String> getCategories(String home) throws ParserException,
			IOException {
		ArrayList<LinkTag> links = getLinks(home);
		ArrayList categories = new ArrayList();
		for (LinkTag lt : links) {
			String cls = lt.getAttribute("class");
			if (cls == null) {
				continue;
			}
			if (cls.startsWith("gae-click*Homepage*Category-Navigation")) {
				categories.add(lt.getLink());
			}
		}
		return categories;
	}

	public ArrayList<LinkTag> getLinks(String url) throws ParserException,
			IOException {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		Parser parser = new Parser(conn);
		parser.setEncoding("UTF-8");

		NodeFilter filter = new NodeClassFilter(LinkTag.class);
		NodeList list = parser.extractAllNodesThatMatch(filter);
		SimpleNodeIterator iter = list.elements();
		LinkTag lt = null;
		ArrayList links = new ArrayList();
		while (iter.hasMoreNodes()) {
			lt = (LinkTag) iter.nextNode();
			links.add(lt);
		}
		return links;
	}

	public ArrayList<String> getDetailPages(String category)
			throws ParserException, IOException {
		ArrayList<LinkTag> links = getLinks(category);
		ArrayList details = new ArrayList();
		for (LinkTag lt : links) {
			String cls = lt.getAttribute("class");
			if (cls == null) {
				continue;
			}
			if (cls.startsWith("product product-")) {
				details.add(lt.getLink());
			}
		}
		return details;
	}
}
