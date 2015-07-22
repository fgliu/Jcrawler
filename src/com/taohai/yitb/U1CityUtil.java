package com.taohai.yitb;
/**
 * Description: 又一城接口仓库端
 * User: linson
 * QQ: 69100737
 * Date: 2014-11-18
 * Time: 10:17
 * Company:中青弘丰IT部
 */
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *   U1城 开源宝\网渠宝接口
 *   U1CityUtil.invoke()
 *   invoke(String pUrl,String pMethod, String pUser, String pSession, String pFormat,Map<String,String> data )
 *   例子查看: main()
 */
public class U1CityUtil
{

    public final static String METHOD_GET="GET";
    public final static String METHOD_PUT="PUT";
    public final static String METHOD_DELETE="DELETE";
    public final static String METHOD_POST="POST";
    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

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
            result.put("result","Failed,Pls Check your url to verify it right") ;
            return result;
        }
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
            result= U1CityUtil.encodeByMD5(origin, "gb2312");
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

    public static   HashMap<String,String> invoke(String pUrl,String pMethod, String pUser, String pSession, String pFormat,Map<String,String> data ){
        StringBuilder pStr=new StringBuilder();
        pStr.append("user=").append(U1CityUtil.encodeURL(pUser,"gb2312"));
        pStr.append("&").append("method=").append(U1CityUtil.encodeURL(pMethod,"gb2312"));
        if(!isBlank(pFormat))
            pStr.append("&").append("format=").append(U1CityUtil.encodeURL(pFormat,"gb2312"));
        String str=pMethod+pSession;
        for(String key:data.keySet()){
            str=str+key+data.get(key);
            pStr.append("&").append(key).append("=").append(U1CityUtil.encodeURL(data.get(key),"gb2312"));
        }

        String token=strMd5(strAsc(str));
        pStr.append("&").append("token=").append(U1CityUtil.encodeURL(token,"gb2312"));
        return rest(pUrl,pStr.toString(),U1CityUtil.METHOD_POST);
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

    public static void main(String[] args)
 {
		HashMap<String, String> data = new HashMap<String, String>();
//		IOpenAPI.GetProClass  查询商品类别
//		IOpenAPI.GetProducts  查询商品信息
//		IOpenAPI.GetProductSkuInfo  查询商品库存信息
//		IOpenAPI.AddOrder  添加订单
//		IOpenAPI.CancelOrderState  取消订单
//		IOpenAPI.GetOrder  查询订单
//		IOpenAPI.GetOrderDeliver  查询订单发货
//		IOpenAPI.GetExchangeOrderProduct  查询售后信息
//		IOpenAPI.AddExchangeOrderProduct  添加售后信息
//		IOpenAPI.CancelExchangeOrderProduct  取消售后信息
//		IOpenAPI.ExchangeOrderExpress  售后快递信息
		
//		http://old.yitb.com/api.rest
//		http://ht.yitb.com/api.rest
		
//		测试的渠道商的账号:TestFxs
//		 密码:test123456
//		Key:TestKey123456789
//		Secert:TestSecert123456789
//		测试商品的货号:YDC-GE1603

		
		String key ="TestKey123456789";
		String secret="TestSecert123456789";
		data.put("appKey", "TestKey123456789");
		HashMap<String, String> restJson = invoke(
				"http://old.yitb.com/api.rest", "IOpenAPI.GetProducts",
				key, secret, "json", data);
		
//		String key ="80a170ee5b96e77a";
//		String secret="ff43703d80a170ee5b96e77ac586d854";
//		data.put("appKey", "80a170ee5b96e77a");
//		HashMap<String, String> restJson = invoke(
//				"http://ht.yitb.com/api.rest", "IOpenAPI.GetProducts",
//				key, secret, "json", data);
		
		String jsonStr = restJson.get("result");
		JSONObject jsonObj;
		List<ProductBean> list = new ArrayList<ProductBean>();
		try {
			jsonObj = new JSONObject(jsonStr);
//			JSONObject proObj = jsonObj.getJSONObject("Result");
			JSONArray proObjs = jsonObj.getJSONArray("Result");
			for (int i = 0; i < proObjs.length(); i++) {
				ProductBean product = new ProductBean();
				JSONObject proObj =  proObjs.getJSONObject(i);
				product.setProId(proObj.getString("ProId"));
				list.add(product);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // 得到指定json key对象的value对象
       
//        for (int i = 0; i < list.size(); i++) {
//			System.out.println(list.get(i).getProId());
//		}
		System.out.println(restJson);
	}
}