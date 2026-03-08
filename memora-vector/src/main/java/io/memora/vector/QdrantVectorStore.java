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

public final class QdrantVectorStore implements VectorStore {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final QdrantVectorStoreConfig config;
    private final VectorHttpTransport transport;

    public QdrantVectorStore(QdrantVectorStoreConfig config) {
        this(config, new UrlConnectionVectorHttpTransport());
    }

    QdrantVectorStore(QdrantVectorStoreConfig config, VectorHttpTransport transport) {
        this.config = Objects.requireNonNull(config, "config");
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    public void ensureCollection(VectorCollectionSpec spec) {
        validateCollectionSpec(spec);

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("vectors", buildCollectionVectors(spec));
        execute("PUT", joinUrl(config.getBaseUrl(), "/collections/" + spec.getName()), writeJson(payload));
    }

    public void upsert(String collectionName, String vectorName, List<VectorRecord> records) {
        if (collectionName == null || collectionName.isEmpty()) {
            throw new IllegalArgumentException("collectionName is required");
        }
        if (records == null || records.isEmpty()) {
            return;
        }

        List<Map<String, Object>> points = new ArrayList<Map<String, Object>>();
        for (VectorRecord record : records) {
            Map<String, Object> point = new LinkedHashMap<String, Object>();
            point.put("id", record.getId());
            point.put("vector", buildRecordVector(vectorName, record.getVector()));
            if (!record.getPayload().isEmpty()) {
                point.put("payload", record.getPayload());
            }
            points.add(point);
        }

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("points", points);
        execute("PUT", joinUrl(config.getBaseUrl(), "/collections/" + collectionName + "/points"), writeJson(payload));
    }

    public List<VectorSearchHit> search(VectorSearchRequest request) {
        validateSearchRequest(request);

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("query", request.getVector());
        payload.put("limit", Integer.valueOf(request.getLimit()));
        payload.put("with_payload", Boolean.TRUE);
        if (request.getVectorName() != null && !request.getVectorName().isEmpty()) {
            payload.put("using", request.getVectorName());
        }
        Map<String, Object> filter = buildFilter(request.getFilterEquals());
        if (!filter.isEmpty()) {
            payload.put("filter", filter);
        }

        String response = execute(
                "POST",
                joinUrl(config.getBaseUrl(), "/collections/" + request.getCollectionName() + "/points/query"),
                writeJson(payload));
        return parseSearchHits(response);
    }

    private List<VectorSearchHit> parseSearchHits(String responseBody) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(responseBody);
            JsonNode result = root.path("result");
            JsonNode points = result.isArray() ? result : result.path("points");
            if (!points.isArray()) {
                return Collections.emptyList();
            }

            List<VectorSearchHit> hits = new ArrayList<VectorSearchHit>();
            for (JsonNode point : points) {
                Map<String, Object> payload = point.has("payload")
                        ? OBJECT_MAPPER.convertValue(point.get("payload"), new TypeReference<Map<String, Object>>() {
                        })
                        : Collections.<String, Object>emptyMap();
                JsonNode idNode = point.path("id");
                String id = idNode.isTextual() ? idNode.asText() : idNode.toString();
                hits.add(new VectorSearchHit(id, point.path("score").asDouble(), payload));
            }
            return hits;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse Qdrant response", exception);
        }
    }

    private String execute(String method, String endpoint, String body) {
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("Content-Type", "application/json");
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            headers.put("api-key", config.getApiKey());
        }

        VectorHttpResponse response = transport.execute(
                method,
                endpoint,
                headers,
                body,
                config.getConnectTimeoutMs(),
                config.getReadTimeoutMs());
        if (response.getStatusCode() >= 400) {
            throw new IllegalStateException(
                    "Qdrant request failed with status " + response.getStatusCode() + ": " + response.getBody());
        }
        return response.getBody();
    }

    private static Map<String, Object> buildCollectionVectors(VectorCollectionSpec spec) {
        if (spec.getVectorName() == null || spec.getVectorName().isEmpty()) {
            Map<String, Object> vectors = new LinkedHashMap<String, Object>();
            vectors.put("size", Integer.valueOf(spec.getDimensions()));
            vectors.put("distance", spec.getDistance().getWireValue());
            return vectors;
        }

        Map<String, Object> vectorConfig = new LinkedHashMap<String, Object>();
        vectorConfig.put("size", Integer.valueOf(spec.getDimensions()));
        vectorConfig.put("distance", spec.getDistance().getWireValue());

        Map<String, Object> namedVectors = new LinkedHashMap<String, Object>();
        namedVectors.put(spec.getVectorName(), vectorConfig);
        return namedVectors;
    }

    private static Object buildRecordVector(String vectorName, List<Double> values) {
        if (vectorName == null || vectorName.isEmpty()) {
            return values;
        }
        Map<String, Object> namedVector = new LinkedHashMap<String, Object>();
        namedVector.put(vectorName, values);
        return namedVector;
    }

    private static Map<String, Object> buildFilter(Map<String, Object> filterEquals) {
        if (filterEquals == null || filterEquals.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Map<String, Object>> must = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Object> entry : filterEquals.entrySet()) {
            Map<String, Object> clause = new LinkedHashMap<String, Object>();
            clause.put("key", entry.getKey());

            Map<String, Object> match = new LinkedHashMap<String, Object>();
            match.put("value", entry.getValue());
            clause.put("match", match);
            must.add(clause);
        }

        Map<String, Object> filter = new LinkedHashMap<String, Object>();
        filter.put("must", must);
        return filter;
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
            throw new IllegalStateException("Failed to encode Qdrant payload", exception);
        }
    }

}
