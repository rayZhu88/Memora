package io.memora.vector;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public final class DashVectorStoreTest {
    @Test
    public void ensureCollectionAndUpsertUseDashVectorApiShape() {
        CapturingTransport transport = new CapturingTransport(
                new VectorHttpResponse(200, "{\"code\":0,\"message\":\"Success\",\"request_id\":\"req-1\"}"),
                new VectorHttpResponse(200, "{\"code\":0,\"message\":\"Success\",\"request_id\":\"req-2\"}"));

        DashVectorStore store = new DashVectorStore(
                new DashVectorStoreConfig("https://dashvector.example", "dash-key", 10_000, 10_000),
                transport);

        store.ensureCollection(new VectorCollectionSpec("memora", 3, VectorDistance.DOT, null));

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("scopeId", "memora");
        store.upsert("memora", null, Collections.singletonList(new VectorRecord(
                "point-1",
                Arrays.asList(Double.valueOf(0.1), Double.valueOf(0.2), Double.valueOf(0.3)),
                payload)));

        Assert.assertEquals("POST", transport.firstMethod);
        Assert.assertEquals("https://dashvector.example/v1/collections", transport.firstEndpoint);
        Assert.assertEquals("dash-key", transport.firstHeaders.get("dashvector-auth-token"));
        Assert.assertTrue(transport.firstPayload.contains("\"name\":\"memora\""));
        Assert.assertTrue(transport.firstPayload.contains("\"metric\":\"DotProduct\""));
        Assert.assertEquals("https://dashvector.example/v1/collections/memora/docs/upsert", transport.secondEndpoint);
        Assert.assertTrue(transport.secondPayload.contains("\"docs\""));
        Assert.assertTrue(transport.secondPayload.contains("\"fields\":{\"scopeId\":\"memora\"}"));
    }

    @Test
    public void searchParsesOutputAndBuildsFilterExpression() {
        CapturingTransport transport = new CapturingTransport(new VectorHttpResponse(
                200,
                "{\"code\":0,\"message\":\"Success\",\"output\":[{\"id\":\"point-1\",\"score\":0.9,\"fields\":{\"scopeId\":\"memora\",\"age\":20}}]}"));

        DashVectorStore store = new DashVectorStore(
                new DashVectorStoreConfig("https://dashvector.example", "dash-key", 10_000, 10_000),
                transport);

        List<VectorSearchHit> hits = store.search(new VectorSearchRequest(
                "memora",
                Arrays.asList(Double.valueOf(0.1), Double.valueOf(0.2)),
                5,
                null,
                new LinkedHashMap<String, Object>() {{
                    put("scopeId", "memora");
                    put("age", Integer.valueOf(20));
                }}));

        Assert.assertEquals(1, hits.size());
        Assert.assertEquals("point-1", hits.get(0).getId());
        Assert.assertEquals(0.9d, hits.get(0).getScore(), 0.0001d);
        Assert.assertEquals("memora", hits.get(0).getPayload().get("scopeId"));
        Assert.assertEquals("https://dashvector.example/v1/collections/memora/query", transport.firstEndpoint);
        Assert.assertTrue(transport.firstPayload.contains("\"topk\":5"));
        Assert.assertTrue(transport.firstPayload.contains("\"filter\":\"scopeId = \\\"memora\\\" and age = 20\""));
    }

    private static final class CapturingTransport implements VectorHttpTransport {
        private final VectorHttpResponse[] responses;
        private int index;
        private String firstMethod;
        private String firstEndpoint;
        private Map<String, String> firstHeaders;
        private String firstPayload;
        private String secondEndpoint;
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
                secondEndpoint = endpoint;
                secondPayload = payload;
            }
            VectorHttpResponse response = responses[Math.min(index, responses.length - 1)];
            index++;
            return response;
        }
    }
}
