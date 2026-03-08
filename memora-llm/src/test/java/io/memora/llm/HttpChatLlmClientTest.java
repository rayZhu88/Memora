package io.memora.llm;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public final class HttpChatLlmClientTest {
    @Test
    public void chatClientParsesResponseOnJdk17() {
        CapturingTransport transport = new CapturingTransport(
                new HttpResponse(
                        200,
                        "{\"id\":\"chat-1\",\"model\":\"glm-4-plus\",\"choices\":[{\"message\":{\"content\":\"hello\"}}],\"usage\":{\"prompt_tokens\":10,\"completion_tokens\":2,\"total_tokens\":12}}"));

        LlmClient client = new HttpChatLlmClient(
                new LlmClientConfig(
                        LlmProvider.ZHIPU,
                        "test-key",
                        "glm-4-plus",
                        "https://example.test",
                        10_000,
                        10_000,
                        Collections.<String, String>emptyMap()),
                transport);

        ChatResponse response = client.chat(new ChatRequest(
                null,
                Arrays.asList(new ChatMessage(ChatRole.USER, "hi")),
                null,
                null,
                null));

        Assert.assertEquals("chat-1", response.getId());
        Assert.assertEquals("hello", response.getContent());
        Assert.assertEquals(Integer.valueOf(12), response.getUsage().getTotalTokens());
        Assert.assertEquals("https://example.test/chat/completions", transport.endpoint);
        Assert.assertEquals("Bearer test-key", transport.headers.get("Authorization"));
        Assert.assertTrue(transport.payload.contains("\"messages\""));
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
