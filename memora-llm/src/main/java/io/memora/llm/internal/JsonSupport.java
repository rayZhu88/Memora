package io.memora.llm.internal;

import io.memora.llm.LlmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public final class JsonSupport {
    private JsonSupport() {
    }

    public static String quote(String value) {
        if (value == null) {
            return "null";
        }

        StringBuilder builder = new StringBuilder();
        builder.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (character < 0x20) {
                        builder.append(String.format("\\u%04x", (int) character));
                    } else {
                        builder.append(character);
                    }
            }
        }
        builder.append('"');
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        if (engine == null) {
            throw new LlmException("Nashorn JavaScript engine is not available for JSON parsing");
        }

        try {
            Object result = engine.eval("Java.asJSONCompatible(" + json + ")");
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            }
            throw new LlmException("Expected JSON object response");
        } catch (ScriptException exception) {
            throw new LlmException("Failed to parse JSON response", exception);
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

