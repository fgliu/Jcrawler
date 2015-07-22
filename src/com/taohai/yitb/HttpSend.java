package com.taohai.yitb;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Http发送类
 * @author nxl
 *
 */
public class HttpSend {

	/**
	 * 发送post请求
	 * @param url 地址
	 * @param key 参数名
	 * @param value 参数值
	 * @return
	 */
	public static String post(String url, String key, String value){
		String response = null;
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(url);
		// 参数
		method.addParameter(key, value);
		HttpClientParams httpClientParams = client.getParams();
		httpClientParams.setSoTimeout(30000);
		try {
			client.executeMethod(method);
			// 实际返回值
			response = method.getResponseBodyAsString();
		} catch (IOException e) {
			e.printStackTrace();
			return response;
		}
		return response;
	}
	
	/**
	 * 发送post请求
	 * @param url 地址
	 * @param params 参数名和参数值的键值对
	 * @return
	 */
	public static String post(String url, Map<String,String> params){
		String response = null;
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(url);
		// 包装参数
		int size = params.size();
		NameValuePair[] parameters = new NameValuePair[size];
		int index = 0;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			NameValuePair pair = new NameValuePair(entry.getKey(), entry.getValue());
			parameters[index] = pair;
			index++;
		}
		method.addParameters(parameters);
		HttpClientParams httpClientParams = client.getParams();
		httpClientParams.setContentCharset("UTF-8");
		httpClientParams.setSoTimeout(30000);
		try {
			client.executeMethod(method);
			// 实际返回值
			response = method.getResponseBodyAsString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}
	
}
