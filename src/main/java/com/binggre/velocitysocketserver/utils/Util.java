package com.binggre.velocitysocketserver.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Util {

    public static String toJson(Object object) {
        Gson gson = (new GsonBuilder()).disableHtmlEscaping().create();
        return gson.toJson(object);
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        Gson gson = (new GsonBuilder()).create();
        return gson.fromJson(json, clazz);
    }
}