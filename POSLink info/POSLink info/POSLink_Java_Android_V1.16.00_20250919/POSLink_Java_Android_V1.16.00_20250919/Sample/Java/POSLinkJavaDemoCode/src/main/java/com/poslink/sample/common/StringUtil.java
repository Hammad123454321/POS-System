package com.poslink.sample.common;

/**
 * Created by Leon.F on 2018/3/9.
 */

public class StringUtil {

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static int parseInt(String str) {
      try {
          return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
      }
    }
}
