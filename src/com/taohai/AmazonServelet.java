package com.taohai;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.htmlparser.util.ParserException;

public class AmazonServelet extends HttpServlet {
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {
	        response.setContentType("text/html; charset=UTF-8");
	        String   url = null;
	       Object obj =  request.getParameter("url");
	       if(null != obj){
	    	   url= obj.toString();
	       }
	       com.taohai.amazon.DetailParser_amazon amazon = null;
	       try{
	    	   
	    	   amazon = new com.taohai.amazon.DetailParser_amazon("test");
	       }catch (Exception e) {
			// TODO: handle exception
		}
	       String product = amazon.makeJSONString();
	        PrintWriter out = response.getWriter();
	        out.println(product);
	    }

	public static void main(String[] args) {
		   com.taohai.amazon.DetailParser_amazon amazon = null;
	       try{
	    	   
	    	   amazon = new com.taohai.amazon.DetailParser_amazon("test");
	       }catch (Exception e) {
			// TODO: handle exception
		}
	       String product = amazon.makeJSONString();
	       System.out.println(product);
	}
}
