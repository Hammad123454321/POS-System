package com.pax.poslink.util;

import android.text.TextUtils;

import org.dom4j.util.StringUtils;

import java.util.Locale;

/**
 * Created by Leon.F on 2018/5/15.
 */
public class StringUtil {

    public static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static boolean isEmpty(String msg) {
        return msg == null || msg.length() == 0;
    }

    public static boolean isTrue(String field) {
        return !TextUtils.isEmpty(field) && parseInt(field) > 0;
    }

    public static String formatExtData(String extData) {
        return extData.replaceAll("><", ">\n<");
    }

    public static String convert2Hex(String orgBuf) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < orgBuf.length(); i++) {
            char ret = orgBuf.charAt(i);
            if (ret >= 0x20 && ret <= 0x7f) {
                buf.append(orgBuf.charAt(i));
            } else {
                buf.append("[");
                buf.append(Integer.toHexString((orgBuf.charAt(i) >> 4 & 0x0f)));
                buf.append(Integer.toHexString((orgBuf.charAt(i) & 0x0f)));
                buf.append("]");
            }
        }

        return buf.toString();
    }

    public static String bcdToBinaryStr(byte[] b) {
        StringBuffer result = new StringBuffer();
        int len = 8;
        for (int i = 0; i < b.length; i++) {
            StringBuilder str = new StringBuilder(Long.toString(b[i] & 0xff, 2));
            while (str.length() < len) {
                str.insert(0, "0");
            }
            result.append(str);
        }
        return result.reverse().toString();
    }

    public static byte[] reverse(byte[] arr) {
        for (int i = 0; i < arr.length / 2; i++) {
            byte temp = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = arr[i];
            arr[i] = temp;
        }
        // 返回反转后的结果
        return arr;
    }


    public static String bcdToStr(byte[] b) throws IllegalArgumentException {
        if (b == null) {
            throw new IllegalArgumentException("bcdToStr input arg is null");
        }

        char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }

        return sb.toString();
    }

    /**
     * 把16进制字符串转换成字节数组
     *
     * @param hexString
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    public static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    public static String showFloatValueString(String number) {
        // Float.parseFloat("999999.99") = 1.0E8
        Double num = StringUtil.parseDouble(number) / 100.0;
        return String.format(Locale.ENGLISH, "%.2f", num);
    }

    public static double parseDouble(String number) {
        if (isNumber(number)) {
            return Double.parseDouble(number);
        }

        return 0f;
    }

    public static boolean isNumber(String str) {
        try {
            Double.parseDouble(str);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
    public static String getescapeCharacter(String str){
        return str.replaceAll("&","&amp;").
                replaceAll("<","&lt;").
                replaceAll(">","&gt;").
                replaceAll("\"","&quot;").
                replaceAll("\'","&apos;");
    }


}
