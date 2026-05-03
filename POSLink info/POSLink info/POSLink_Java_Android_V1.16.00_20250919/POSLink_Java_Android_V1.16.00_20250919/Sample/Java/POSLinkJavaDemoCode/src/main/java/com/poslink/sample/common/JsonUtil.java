package com.poslink.sample.common;

import com.google.gson.Gson;

/**
 * Created by Leon.F on 2018/4/9.
 */

public class JsonUtil {
    public static <T> T fromJson(String json, Class<T> clzz) {
        Gson gson = new Gson();
        return gson.fromJson(json, clzz);
    }

    public static String toJson(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }
}
