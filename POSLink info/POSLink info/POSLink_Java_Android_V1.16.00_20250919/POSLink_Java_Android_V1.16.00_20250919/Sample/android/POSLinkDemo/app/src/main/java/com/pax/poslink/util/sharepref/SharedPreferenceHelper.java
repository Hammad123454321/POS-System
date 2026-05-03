package com.pax.poslink.util.sharepref;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * sharedPreference helper
 * The class provider value to save and get
 * Created by Frank.W on 2017/3/23.
 */

public class SharedPreferenceHelper {
    private final static String SP_CLIENT = "SharedPreferenceHelper";
    private static SharedPreferences.Editor mEditor = null;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferenceHelper spHelper = null;

    public static void instance(Context context) {
        if (spHelper == null) {
            spHelper = new SharedPreferenceHelper(context);
        }
    }

    private SharedPreferenceHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(SP_CLIENT, 0);
        mEditor = sharedPreferences.edit();
    }

    public SharedPreferenceHelper(Context context, String key) {
        sharedPreferences = context.getSharedPreferences(key, 0);
        mEditor = sharedPreferences.edit();
    }

    /**
     * According to value's type, Put value into SharedPreference.
     *
     * @param key   key
     * @param value value
     */
    private static void put(String key, Object value) {
        if (value instanceof String) {
            mEditor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            mEditor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            mEditor.putLong(key, (Long) value);
        } else if (value instanceof Boolean) {
            mEditor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            mEditor.putFloat(key, (Float) value);
        } else {
            mEditor.putString(key, value.toString());
        }
        mEditor.commit();
    }

    /**
     * According to value's type, Get value from SharedPreference.
     *
     * @param key          key
     * @param defaultValue default value
     * @return value
     */
    private static <T extends Object> T getValue(String key, T defaultValue) {
        if (defaultValue instanceof String) {
            return (T) sharedPreferences.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return (T) Integer.valueOf(sharedPreferences.getInt(key, (Integer) defaultValue));
        } else if (defaultValue instanceof Long) {
            return (T) Long.valueOf(sharedPreferences.getLong(key, (Long) defaultValue));
        } else if (defaultValue instanceof Boolean) {
            return (T) Boolean.valueOf(sharedPreferences.getBoolean(key, (Boolean) defaultValue));
        } else if (defaultValue instanceof Float) {
            return (T) Float.valueOf(sharedPreferences.getFloat(key, (Float) defaultValue));
        } else {
            return (T) sharedPreferences.getString(key, (String) defaultValue);
        }
    }

    public static void save(String key, Object value) {
        if (!TextUtils.isEmpty(key) && value != null) {
            put(key, value);
        }
    }

    public static <T> T get(String key, T defaultValue) {
        return getValue(key, defaultValue);
    }

    public static void saveInt(String key, int value) {
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    public static int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }
}
