package io.memora.llm.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.memora.llm.LlmException;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public final class JsonSupport {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonSupport() {
    }

    public static String quote(String value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new LlmException("Failed to encode JSON string", exception);
        }
    }

    public static Map<String, Object> parseObject(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception exception) {
            throw new LlmException("Failed to parse JSON response", exception);
        }
    }

    public static JsonNode parseTree(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception exception) {
            throw new LlmException("Failed to parse JSON response", exception);
        }
    }

    public static String writeJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new LlmException("Failed to encode JSON payload", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> readList(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return Collections.emptyList();
    }

    public static Map<String, Object> readObject(Map<String, Object> source, String key) {
        return asObject(source.get(key));
    }

    public static String readString(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? null : String.valueOf(value);
    }

    public static Integer readInteger(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }
        return null;
    }
}
