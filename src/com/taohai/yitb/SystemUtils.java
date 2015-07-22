package com.taohai.yitb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;


/**
 * 系统工具类。
 *
 */
public abstract class SystemUtils {
    private SystemUtils() {
    }
    private final static String DEFAULT_CHARSET = "gb2312";

    private static String getStringFromException(Throwable e) {
        String result = "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);
        e.printStackTrace(ps);
        try {
            result = bos.toString(DEFAULT_CHARSET);
        } catch (IOException ioe) {
        }
        return result;
    }



    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toLowerCase());
        }
        return sign.toString();
    }



    public static String signRequest(String appSecret, String method,Map<String, String> postDate) throws IOException {
        String str = method + appSecret;
        for(String key: postDate.keySet())
        {
            str += key;
            str += postDate.get(key);
        }
        String strAsc = Asc(str.replace(" ", "").toLowerCase());
        String token = Encrypt_MD5(strAsc);

        return token;
    }

    private static String Asc(String source)
    {
        char[] arr = source.toCharArray();
        Arrays.sort(arr);
        String strAsc = "";
        for(char item : arr)
        {
            strAsc += item;
        }
        return strAsc;
    }
    public static String Encrypt_MD5(String AppKey) throws IOException {
        return byte2hex(encryptMD5(AppKey));
    }

    public static byte[] encryptMD5(String data) throws IOException {
        byte[] bytes = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            bytes = md.digest(data.getBytes(DEFAULT_CHARSET));
        } catch (GeneralSecurityException gse) {
            String msg = getStringFromException(gse);
            throw new IOException(msg);
        }
        return bytes;
    }
}
