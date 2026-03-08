package io.memora.vector;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public final class QdrantVectorStoreTest {
    @Test
    public void ensureCollectionAndUpsertSendExpectedPayloads() {
        CapturingTransport transport = new CapturingTransport(
                new VectorHttpResponse(200, "{\"status\":\"ok\",\"result\":true}"),
                new VectorHttpResponse(200, "{\"status\":\"ok\",\"result\":{\"operation_id\":1}}"));

        QdrantVectorStore store = new QdrantVectorStore(
                new QdrantVectorStoreConfig("https://qdrant.example", "qdrant-key", 10_000, 10_000),
                transport);

        store.ensureCollection(new VectorCollectionSpec("memora", 3, VectorDistance.COSINE, "memory"));

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("scopeId", "memora");
        store.upsert("memora", "memory", Collections.singletonList(new VectorRecord(
                "point-1",
                Arrays.asList(Double.valueOf(0.1), Double.valueOf(0.2), Double.valueOf(0.3)),
                payload)));

        Assert.assertEquals("PUT", transport.firstMethod);
        Assert.assertEquals("qdrant-key", transport.firstHeaders.get("api-key"));
        Assert.assertTrue(transport.firstPayload.contains("\"vectors\":{\"memory\":{\"size\":3,\"distance\":\"Cosine\"}}"));
        Assert.assertTrue(transport.secondPayload.contains("\"id\":\"point-1\""));
        Assert.assertTrue(transport.secondPayload.contains("\"vector\":{\"memory\":[0.1,0.2,0.3]}"));
        Assert.assertTrue(transport.secondPayload.contains("\"scopeId\":\"memora\""));
    }

    @Test
    public void searchParsesQdrantHitsAndBuildsFilters() {
        CapturingTransport transport = new CapturingTransport(new VectorHttpResponse(
                200,
                "{\"status\":\"ok\",\"result\":{\"points\":[{\"id\":\"point-1\",\"score\":0.88,\"payload\":{\"scopeId\":\"memora\"}}]}}"));

        QdrantVectorStore store = new QdrantVectorStore(
                new QdrantVectorStoreConfig("https://qdrant.example", null, 10_000, 10_000),
                transport);

        List<VectorSearchHit> hits = store.search(new VectorSearchRequest(
                "memora",
                Arrays.asList(Double.valueOf(0.1), Double.valueOf(0.2)),
                5,
                null,
                Collections.<String, Object>singletonMap("scopeId", "memora")));

        Assert.assertEquals(1, hits.size());
        Assert.assertEquals("point-1", hits.get(0).getId());
        Assert.assertEquals(0.88d, hits.get(0).getScore(), 0.0001d);
        Assert.assertEquals("memora", hits.get(0).getPayload().get("scopeId"));
        Assert.assertEquals("POST", transport.firstMethod);
        Assert.assertEquals("https://qdrant.example/collections/memora/points/query", transport.firstEndpoint);
        Assert.assertTrue(transport.firstPayload.contains("\"query\":[0.1,0.2]"));
        Assert.assertTrue(transport.firstPayload.contains("\"match\":{\"value\":\"memora\"}"));
    }

    private static final class CapturingTransport implements VectorHttpTransport {
        private final VectorHttpResponse[] responses;
        private int index;
        private String firstMethod;
        private String firstEndpoint;
        private Map<String, String> firstHeaders;
        private String firstPayload;
        private String secondPayload;

        private CapturingTransport(VectorHttpResponse... responses) {
            this.responses = responses;
        }

        public VectorHttpResponse execute(
                String method,
                String endpoint,
                Map<String, String> headers,
                String payload,
                int connectTimeoutMs,
                int readTimeoutMs) {
            if (index == 0) {
                firstMethod = method;
                firstEndpoint = endpoint;
                firstHeaders = headers;
                firstPayload = payload;
            } else if (index == 1) {
                secondPayload = payload;
            }
            VectorHttpResponse response = responses[Math.min(index, responses.length - 1)];
            index++;
            return response;
        }
    }
}
