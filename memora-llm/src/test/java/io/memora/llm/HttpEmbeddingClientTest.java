package io.memora.llm;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public final class HttpEmbeddingClientTest {
    @Test
    public void zhipuEmbeddingClientParsesVectors() {
        CapturingTransport transport = new CapturingTransport(new HttpResponse(
                200,
                "{\"model\":\"embedding-3\",\"data\":[{\"index\":0,\"embedding\":[0.1,0.2,0.3]}],\"usage\":{\"prompt_tokens\":12,\"total_tokens\":12}}"));

        EmbeddingClient client = new HttpEmbeddingClient(
                new EmbeddingClientConfig(
                        EmbeddingProvider.ZHIPU,
                        "test-key",
                        "embedding-3",
                        "https://example.test",
                        10_000,
                        10_000,
                        Collections.<String, String>emptyMap()),
                transport);

        EmbeddingResponse response = client.embed(new EmbeddingRequest(
                null,
                Arrays.asList("memora"),
                Integer.valueOf(1024),
                "u1"));

        Assert.assertEquals("embedding-3", response.getModel());
        Assert.assertEquals(1, response.getVectors().size());
        Assert.assertEquals(3, response.getVectors().get(0).getValues().size());
        Assert.assertEquals("https://example.test/embeddings", transport.endpoint);
        Assert.assertEquals("Bearer test-key", transport.headers.get("Authorization"));
        Assert.assertTrue(transport.payload.contains("\"model\":\"embedding-3\""));
        Assert.assertTrue(transport.payload.contains("\"dimensions\":1024"));
        Assert.assertTrue(transport.payload.contains("\"user_id\":\"u1\""));
    }

    private static final class CapturingTransport implements HttpTransport {
        private final HttpResponse response;
        private String endpoint;
        private Map<String, String> headers;
        private String payload;

        private CapturingTransport(HttpResponse response) {
            this.response = response;
        }

        public HttpResponse post(
                String endpoint,
                Map<String, String> headers,
                String payload,
                int connectTimeoutMs,
                int readTimeoutMs) {
            this.endpoint = endpoint;
            this.headers = headers;
            this.payload = payload;
            return response;
        }
    }
}
