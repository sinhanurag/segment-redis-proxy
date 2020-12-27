package com.segment.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import spark.ResponseTransformer;

public class JsonUtil {
    public static String stringToJson(String string) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        JsonParser jp = new JsonParser();
        return gson.toJson(jp.parse(string));
    }
    public static String objectToJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }
}
