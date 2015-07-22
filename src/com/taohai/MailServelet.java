package com.taohai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.htmlparser.util.ParserException;

import com.taohai.mail.MailServer;

public class MailServelet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		String host = "imap.qq.com";
		List<String> list = this.readTxtFile("/home/andy/mail.txt");
//		List<String> list = this.readTxtFile("D:\\mail.txt");
		for (int i = 0; i < list.size(); i++) {
			String str = list.get(i);
			String username =str.substring(0,str.indexOf(":"));
			String password =str.substring(str.indexOf(":")+1);
			try {
				MailServer mail = new MailServer(host, username, password);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static List<String> readTxtFile(String filePath) {
		List<String> list = new ArrayList<String>();
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
//					System.out.println(lineTxt);
					list.add(lineTxt);
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		return list;
	}

	public static void main(String[] args) {
//		com.taohai.amazon.DetailParser_amazon amazon = null;
//		try {
//
//			amazon = new com.taohai.amazon.DetailParser_amazon("test");
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		String product = amazon.makeJSONString();
//		System.out.println(product);
//		readTxtFile("d:\\mail.txt");
		String host = "imap.qq.com";
		List<String> list = readTxtFile("d:\\mail.txt");
		for (int i = 0; i < list.size(); i++) {
			String str = list.get(i);
			String username =str.substring(0,str.indexOf(":"));
			String password =str.substring(str.indexOf(":")+1);
			System.out.println(username);
			System.out.println(password);
			try {
				MailServer mail = new MailServer(host, username, password);
				System.out.println(username+"=============================================");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
