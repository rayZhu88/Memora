package io.memora.llm;

import com.fasterxml.jackson.databind.JsonNode;
import io.memora.llm.internal.JsonSupport;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HttpChatLlmClient implements LlmClient {
    private final LlmClientConfig config;
    private final HttpTransport transport;

    public HttpChatLlmClient(LlmClientConfig config) {
        this(config, new UrlConnectionHttpTransport());
    }

    HttpChatLlmClient(LlmClientConfig config, HttpTransport transport) {
        this.config = Objects.requireNonNull(config, "config");
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    public ChatResponse chat(ChatRequest request) {
        validateRequest(request);

        String endpoint = joinUrl(config.getBaseUrl(), config.getProvider().getChatCompletionsPath());
        String payload = buildPayload(request);

        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + config.getApiKey());
        headers.putAll(config.getExtraHeaders());

        HttpResponse response = transport.post(
                endpoint,
                headers,
                payload,
                config.getConnectTimeoutMs(),
                config.getReadTimeoutMs());

        if (response.getStatusCode() >= 400) {
            throw new LlmException(
                    "LLM request failed with status " + response.getStatusCode() + ": " + response.getBody());
        }

        return parseResponse(response.getBody());
    }

    /*
     * Streaming is intentionally not implemented in v1.
     * The sync chat path remains the default for Memora internal workflows.
    */

    private ChatResponse parseResponse(String responseBody) {
        JsonNode root = JsonSupport.parseTree(responseBody);
        JsonNode firstChoice = root.path("choices").isArray() && root.path("choices").size() > 0
                ? root.path("choices").get(0)
                : null;
        JsonNode message = firstChoice == null ? null : firstChoice.path("message");

        String content = message == null || message.isMissingNode() ? null : message.path("content").asText(null);
        String id = root.path("id").asText(null);
        String model = root.path("model").asText(null);

        JsonNode usage = root.path("usage");
        LlmUsage tokenUsage = usage.isMissingNode()
                ? new LlmUsage(null, null, null)
                : new LlmUsage(
                        usage.path("prompt_tokens").isNumber() ? Integer.valueOf(usage.path("prompt_tokens").asInt()) : null,
                        usage.path("completion_tokens").isNumber()
                                ? Integer.valueOf(usage.path("completion_tokens").asInt())
                                : null,
                        usage.path("total_tokens").isNumber() ? Integer.valueOf(usage.path("total_tokens").asInt()) : null);

        return new ChatResponse(id, config.getProvider(), model, content, tokenUsage, responseBody);
    }

    private String buildPayload(ChatRequest request) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("model", resolveModel(request));
        payload.put("stream", Boolean.FALSE);

        List<Map<String, Object>> messages = new ArrayList<Map<String, Object>>();
        for (ChatMessage message : request.getMessages()) {
            Map<String, Object> messageBody = new LinkedHashMap<String, Object>();
            messageBody.put("role", message.getRole().getWireValue());
            messageBody.put("content", message.getContent());
            messages.add(messageBody);
        }
        payload.put("messages", messages);

        if (request.getTemperature() != null) {
            payload.put("temperature", request.getTemperature());
        }
        if (request.getTopP() != null) {
            payload.put("top_p", request.getTopP());
        }
        if (request.getMaxTokens() != null) {
            payload.put("max_tokens", request.getMaxTokens());
        }

        return JsonSupport.writeJson(payload);
    }

    private String resolveModel(ChatRequest request) {
        if (request.getModel() != null && !request.getModel().isEmpty()) {
            return request.getModel();
        }
        if (config.getDefaultModel() != null && !config.getDefaultModel().isEmpty()) {
            return config.getDefaultModel();
        }
        throw new LlmException("No model was provided in request or client config");
    }

    private static void validateRequest(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("chat request is required");
        }
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new IllegalArgumentException("chat request requires at least one message");
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
}
