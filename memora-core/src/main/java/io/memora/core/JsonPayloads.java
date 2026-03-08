package io.memora.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class JsonPayloads {
    private JsonPayloads() {
    }

    static String emptyObject() {
        return "{}";
    }

    static String object(String key, String value) {
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put(key, value);
        return object(values);
    }

    static String object(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return emptyObject();
        }

        StringBuilder builder = new StringBuilder();
        builder.append('{');

        boolean first = true;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            builder.append('"')
                    .append(escape(entry.getKey()))
                    .append("\":\"")
                    .append(escape(entry.getValue()))
                    .append('"');
            first = false;
        }

        builder.append('}');
        return builder.toString();
    }

    static Map<String, String> singleton(String key, String value) {
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put(key, value);
        return Collections.unmodifiableMap(values);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (current <= 0x1F) {
                        escaped.append(String.format("\\u%04x", Integer.valueOf(current)));
                    } else {
                        escaped.append(current);
                    }
                    break;
            }
        }
        return escaped.toString();
    }
}
