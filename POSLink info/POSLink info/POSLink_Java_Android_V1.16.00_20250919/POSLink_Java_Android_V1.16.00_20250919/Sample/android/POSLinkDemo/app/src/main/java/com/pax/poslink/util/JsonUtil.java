package com.pax.poslink.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class JsonUtil {
    private static final String TAG = "JsonUtil";

    public static <T> T gsonParseJson(String json, Class<T> classOfT) {
        Gson gson = new Gson();
        T t = null;
        try {
            t = gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "", e);
        }
        return t;
    }

    public static <T> T gsonParseJson(String json, Type typeOfT) {
        Gson gson = new Gson();
        T t = null;
        try {
            t = gson.fromJson(json, typeOfT);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "", e);
        }
        return t;
    }

    static class MyExclusionStrategy implements ExclusionStrategy {

        private Object object;

        public MyExclusionStrategy(Object object) {
            this.object = object;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            try {
                Field field = object.getClass().getField(f.getName());
                field.setAccessible(true);
                Object o = field.get(object);
                if (o instanceof String) {
                    String value = (String)o;
                    if (TextUtils.isEmpty(value)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }

    public static String gsonToJson(Object obj) {
//        Gson gson = new Gson();
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .addSerializationExclusionStrategy(new MyExclusionStrategy(obj))
                .create();

        String json = "";
        try {
            json = gson.toJson(obj);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return json;
    }

    public static JSONObject fromJson(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    public static <T> String generalTypeToJson(Object src, Type typeOfSrc, Class<T> classOfT) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(classOfT, new GeneralTypeAdapter<T>())
                .create();
        String json = "";
        try {
            json = gson.toJson(src, typeOfSrc);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return json;
    }

    public static <D, T> D jsonToGeneralType(String json, Type typeOfTarget, Class<T> classOfT) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(classOfT, new GeneralTypeAdapter<T>())
                .create();
        D d = null;
        try {
            d = gson.fromJson(json, typeOfTarget);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "", e);
        }
        return d;
    }

    private static class GeneralTypeAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

        private static final String CLASS_NAME = "class_name";
        private static final String CLASS_DATA = "class_data";

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String className = jsonObject.get(CLASS_NAME).getAsString();
            JsonElement element = jsonObject.get(CLASS_DATA);
            try {
                return context.deserialize(element, Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Unknown class: " + className, e);
            }
        }

        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.add(CLASS_NAME, new JsonPrimitive(src.getClass().getCanonicalName()));
            result.add(CLASS_DATA, context.serialize(src, src.getClass()));
            return result;
        }
    }
}
