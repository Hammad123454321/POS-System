package com.poslink.sample.common;

/**
 * Created by Leon.F on 2018/3/21.
 */

public class LogUtil {

    public static final boolean DEBUG = false;

    public static void v(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }
}
