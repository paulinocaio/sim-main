package io.sim.Project;

import com.google.gson.Gson;

public class JsonUtils {

    private static final Gson gson = new Gson();

    // Converte um objeto para JSON
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    // Converte JSON de volta para um objeto da classe especificada
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
}
