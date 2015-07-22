package com.taohai;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.htmlparser.util.ParserException;
import org.json.JSONArray;
import org.json.JSONObject;

public class Crawler6PmServelet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		JSONObject errObj = new JSONObject();
		String url = null;
		boolean err = false;
		PrintWriter out = null;
		Object obj = request.getParameter("url");
		if (null != obj) {
			url = obj.toString();
		}
		Object update = request.getParameter("update");
		int i = 0;
		if (null != update) {
			i = 1;
		}
		com.taohai.pm6.DetailParser_6pm dp = null;
		try {
			out = response.getWriter();
			if (i > 0) {
				dp = new com.taohai.pm6.DetailParser_6pm(url, i);
			} else {
				dp = new com.taohai.pm6.DetailParser_6pm(url);
			}
			if (i > 0) {
				JSONArray js = dp.updatePrice();
				out.println(js.toString());
			} else {
				JSONObject jo = dp.makeJSON();
				out.println(jo.toString());
			}
		}catch (Exception e) {
			errObj = this.makeErrorJson(e, url);
			err = true;
		}
		finally{
			if (err) {
				out.println(errObj.toString());
			}
		}

	}
	

	public JSONObject makeErrorJson(Exception e, String url) {
		JSONObject errObj = new JSONObject();
		errObj.put("status", 1);
		errObj.put("error", e.getMessage());
		errObj.put("source_url", url);
		errObj.put("time", System.currentTimeMillis());
		return errObj;
	}


}
