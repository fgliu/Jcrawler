package com.taohai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taohai.yitb.HttpSend;

public class UOneCityServelet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4731567501626860164L;
	
    public final static String METHOD_POST="POST";
    
    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=utf-8");
		String addOrder = request.getParameter("AddOrder");
		String getOrderDeliver = request.getParameter("GetOrderDeliver");
		String detail = request.getParameter("detail");
		PrintWriter out = null;
		HashMap<String, String> data = new HashMap<String, String>();
		try {
			JSONObject json = null;
			if (null != addOrder) {
				json = (JSONObject) new JSONParser().parse(addOrder);
			} else if (null != getOrderDeliver) {
				json = (JSONObject) new JSONParser().parse(getOrderDeliver);
			}else if(null != detail ){
				json = (JSONObject) new JSONParser().parse(detail);
			}
			if(null != json){
				data = new ObjectMapper().readValue(json.toJSONString(),
						HashMap.class);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		out = response.getWriter();
		String key = "key";
		String secret = "secret";
		String url = "url";
		data.put("appKey",key);
		String cmd = "IOpenAPI.GetProducts";
		if (null != addOrder) {
			cmd = "IOpenAPI.AddOrder";
		} else if (null != getOrderDeliver) {
			cmd = "IOpenAPI.GetOrderDeliver";
		}
		HashMap<String, String> restJson = invoke(url, cmd, key, secret,
				"json", data);
		String jsonStr = restJson.get("result");
		out.println(jsonStr);
		System.out.println(jsonStr);
    
	}
	
	
	  public static HashMap<String,String> rest(String serviceUrl,String parameter,String restMethod){
	        try {
	            URL url= new URL(serviceUrl);
	            HttpURLConnection con = (HttpURLConnection)url.openConnection();
	            con.setRequestMethod(restMethod);
	            con.setConnectTimeout(300000);
	            con.addRequestProperty("Content-type","application/x-www-form-urlencoded;charset=UTF-8");
	            con.setDoOutput(true);
	            OutputStream os = con.getOutputStream();
	            os.write(parameter.getBytes("UTF-8"));
	            os.close();

	            HashMap<String,String> result=new HashMap<String,String>();
	            result.put("code",String.valueOf(con.getResponseCode()));
	            result.put("msg",con.getResponseMessage());

	            //读取返回信息
	            InputStream inputStream = con.getInputStream();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
	            String  strMessage;
	            StringBuffer buffer=new StringBuffer();
	            while ((strMessage = reader.readLine()) != null) {
	                buffer.append(strMessage);
	            }
	            result.put("result",buffer.toString());
	            return result;
	        } catch ( Exception e ) {
	            HashMap<String,String> result=new HashMap<String,String>();
	            result.put("code","0");
	            result.put("msg",e.getMessage());
	            result.put("result",e.getMessage()) ;
	            return result;
	        }
	    }
	  
	  public static   HashMap<String,String> invoke(String pUrl,String pMethod, String pUser, String pSession, String pFormat,Map<String,String> data ){
	        StringBuilder pStr=new StringBuilder();
	        pStr.append("user=").append(UOneCityServelet.encodeURL(pUser,"utf-8"));
	        pStr.append("&").append("method=").append(UOneCityServelet.encodeURL(pMethod,"utf-8"));
	        if(!isBlank(pFormat))
	            pStr.append("&").append("format=").append(UOneCityServelet.encodeURL(pFormat,"utf-8"));
	        String str=pMethod+pSession;
	        for(String key:data.keySet()){
	            str=str+key+data.get(key);
	            pStr.append("&").append(key).append("=").append(UOneCityServelet.encodeURL(data.get(key),"utf-8"));
	        }

	        String token=strMd5(strAsc(str));
	        pStr.append("&").append("token=").append(UOneCityServelet.encodeURL(token,"utf-8"));
	        return rest(pUrl,pStr.toString(),UOneCityServelet.METHOD_POST);
	        
	    }

	  //字符串按字母升序排序
	    public static String strAsc(String origin){
	        StringBuffer result=new StringBuffer();
	        //去掉空格、转化成小写、升序
	        char [] originChars=origin.replace(" ","").toLowerCase().toCharArray();
	        Arrays.sort(originChars);
	        for(char s: originChars){
	            result.append(s);
	        }
	        return result.toString();
	    }
	    

	    //md5加密
	    public static String strMd5(String origin){
	        String result="";
	        try{
	            result= UOneCityServelet.encodeByMD5(origin, "gb2312");
	        }catch (Exception e){
	            e.printStackTrace();
	        }
	        return result;
	    }
		  /**
	     * encode By MD5
	     *
	     * @param str
	     * @param encode
	     * @return String
	     */
	    public static String encodeByMD5(String str,String encode) {
	        if (str == null) {
	            return null;
	        }
	        try {
	            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
	            messageDigest.update(str.getBytes(encode));
	            return getFormattedText(messageDigest.digest());
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }

	    }
	    
		private static String getFormattedText(byte[] bytes) {
			int len = bytes.length;
			StringBuilder buf = new StringBuilder(len * 2);
			// 把密文转换成十六进制的字符串形式
			for (int j = 0; j < len; j++) {
				buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
				buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
			}
			return buf.toString();
		}
		 /**
	     * 对url进行编码
	     */
	    public static String encodeURL(String url,String charset) {
	        try {
	            return URLEncoder.encode(url, charset).replace("+","%20");
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

		public static boolean isBlank(String str) {
	        int strLen;
	        if(str != null && (strLen = str.length()) != 0) {
	            for(int i = 0; i < strLen; ++i) {
	                if(!Character.isWhitespace(str.charAt(i))) {
	                    return false;
	                }
	            }

	            return true;
	        } else {
	            return true;
	        }
	    }

}
