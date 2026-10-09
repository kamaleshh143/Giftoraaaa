package com.giftora.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;

/**
 * Shared Gson instance and JSON helpers for API responses.
 * java.time types are emitted as ISO-8601 strings so the API stays
 * reflection-module-friendly on modern JDKs.
 */
public final class JsonUtil {

    private static final TypeAdapter<Instant> INSTANT_ADAPTER = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public Instant read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return Instant.parse(in.nextString());
        }
    };

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .registerTypeAdapter(Instant.class, INSTANT_ADAPTER)
            .create();

    private JsonUtil() {
    }

    public static Gson gson() {
        return GSON;
    }

    public static String toJson(Object value) {
        return GSON.toJson(value);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }
}
