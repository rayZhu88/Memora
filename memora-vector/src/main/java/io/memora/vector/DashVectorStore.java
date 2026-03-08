package io.memora.vector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DashVectorStore implements VectorStore {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DashVectorStoreConfig config;
    private final VectorHttpTransport transport;

    public DashVectorStore(DashVectorStoreConfig config) {
        this(config, new UrlConnectionVectorHttpTransport());
    }

    DashVectorStore(DashVectorStoreConfig config, VectorHttpTransport transport) {
        this.config = Objects.requireNonNull(config, "config");
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    public void ensureCollection(VectorCollectionSpec spec) {
        validateCollectionSpec(spec);

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("name", spec.getName());
        payload.put("dimension", Integer.valueOf(spec.getDimensions()));
        payload.put("metric", toDashMetric(spec.getDistance()));

        execute(
                "POST",
                joinUrl(config.getEndpoint(), "/v1/collections"),
                writeJson(payload));
    }

    public void upsert(String collectionName, String vectorName, List<VectorRecord> records) {
        if (collectionName == null || collectionName.isEmpty()) {
            throw new IllegalArgumentException("collectionName is required");
        }
        if (records == null || records.isEmpty()) {
            return;
        }
        if (vectorName != null && !vectorName.isEmpty()) {
            throw new IllegalArgumentException("DashVectorStore v1 does not support named vectors");
        }

        List<Map<String, Object>> docs = new ArrayList<Map<String, Object>>();
        for (VectorRecord record : records) {
            Map<String, Object> doc = new LinkedHashMap<String, Object>();
            doc.put("id", record.getId());
            doc.put("vector", record.getVector());
            if (!record.getPayload().isEmpty()) {
                doc.put("fields", record.getPayload());
            }
            docs.add(doc);
        }

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("docs", docs);
        execute(
                "POST",
                joinUrl(config.getEndpoint(), "/v1/collections/" + collectionName + "/docs/upsert"),
                writeJson(payload));
    }

    public List<VectorSearchHit> search(VectorSearchRequest request) {
        validateSearchRequest(request);
        if (request.getVectorName() != null && !request.getVectorName().isEmpty()) {
            throw new IllegalArgumentException("DashVectorStore v1 does not support named vectors");
        }

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("vector", request.getVector());
        payload.put("topk", Integer.valueOf(request.getLimit()));
        String filter = toDashFilter(request.getFilterEquals());
        if (!filter.isEmpty()) {
            payload.put("filter", filter);
        }

        String response = execute(
                "POST",
                joinUrl(config.getEndpoint(), "/v1/collections/" + request.getCollectionName() + "/query"),
                writeJson(payload));
        return parseHits(response);
    }

    private List<VectorSearchHit> parseHits(String responseBody) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(responseBody);
            JsonNode output = root.path("output");
            if (!output.isArray()) {
                return Collections.emptyList();
            }

            List<VectorSearchHit> hits = new ArrayList<VectorSearchHit>();
            for (JsonNode item : output) {
                Map<String, Object> fields = item.has("fields")
                        ? OBJECT_MAPPER.convertValue(item.get("fields"), new TypeReference<Map<String, Object>>() {
                        })
                        : Collections.<String, Object>emptyMap();
                hits.add(new VectorSearchHit(
                        item.path("id").asText(),
                        item.path("score").asDouble(),
                        fields));
            }
            return hits;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse DashVector response", exception);
        }
    }

    private String execute(String method, String endpoint, String body) {
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("dashvector-auth-token", config.getApiKey());

        VectorHttpResponse response = transport.execute(
                method,
                endpoint,
                headers,
                body,
                config.getConnectTimeoutMs(),
                config.getReadTimeoutMs());
        if (response.getStatusCode() >= 400) {
            throw new IllegalStateException(
                    "DashVector request failed with status " + response.getStatusCode() + ": " + response.getBody());
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(response.getBody());
            int code = root.path("code").asInt(0);
            if (code != 0) {
                throw new IllegalStateException(
                        "DashVector request failed with code " + code + ": " + root.path("message").asText());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse DashVector response", exception);
        }

        return response.getBody();
    }

    private static void validateCollectionSpec(VectorCollectionSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("collection spec is required");
        }
        if (spec.getName() == null || spec.getName().isEmpty()) {
            throw new IllegalArgumentException("collection name is required");
        }
        if (spec.getDimensions() <= 0) {
            throw new IllegalArgumentException("collection dimensions must be positive");
        }
        if (spec.getDistance() == null) {
            throw new IllegalArgumentException("vector distance is required");
        }
    }

    private static void validateSearchRequest(VectorSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("search request is required");
        }
        if (request.getCollectionName() == null || request.getCollectionName().isEmpty()) {
            throw new IllegalArgumentException("collectionName is required");
        }
        if (request.getVector() == null || request.getVector().isEmpty()) {
            throw new IllegalArgumentException("query vector is required");
        }
        if (request.getLimit() <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
    }

    private static String toDashMetric(VectorDistance distance) {
        switch (distance) {
            case COSINE:
                return "Cosine";
            case DOT:
                return "DotProduct";
            case EUCLID:
                return "Euclidean";
            default:
                throw new IllegalArgumentException("Unsupported distance: " + distance);
        }
    }

    private static String toDashFilter(Map<String, Object> filterEquals) {
        if (filterEquals == null || filterEquals.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : filterEquals.entrySet()) {
            if (!first) {
                builder.append(" and ");
            }
            builder.append(entry.getKey()).append(" = ").append(formatFilterValue(entry.getValue()));
            first = false;
        }
        return builder.toString();
    }

    private static String formatFilterValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return "\"" + String.valueOf(value).replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String joinUrl(String baseUrl, String path) {
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    private static String writeJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to encode DashVector payload", exception);
        }
    }
}
