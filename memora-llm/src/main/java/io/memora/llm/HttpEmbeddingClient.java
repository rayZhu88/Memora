package io.memora.llm;

import com.fasterxml.jackson.databind.JsonNode;
import io.memora.llm.internal.JsonSupport;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HttpEmbeddingClient implements EmbeddingClient {
    private final EmbeddingClientConfig config;
    private final HttpTransport transport;

    public HttpEmbeddingClient(EmbeddingClientConfig config) {
        this(config, new UrlConnectionHttpTransport());
    }

    HttpEmbeddingClient(EmbeddingClientConfig config, HttpTransport transport) {
        this.config = Objects.requireNonNull(config, "config");
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    public EmbeddingResponse embed(EmbeddingRequest request) {
        validateRequest(request);

        String endpoint = joinUrl(config.getBaseUrl(), config.getProvider().getEmbeddingsPath());
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
                    "Embedding request failed with status " + response.getStatusCode() + ": " + response.getBody());
        }

        return parseResponse(response.getBody());
    }

    private EmbeddingResponse parseResponse(String responseBody) {
        JsonNode root = JsonSupport.parseTree(responseBody);
        List<EmbeddingVector> vectors = new ArrayList<EmbeddingVector>();
        JsonNode data = root.path("data");
        if (data.isArray()) {
            for (JsonNode item : data) {
                List<Double> values = new ArrayList<Double>();
                JsonNode embedding = item.path("embedding");
                if (embedding.isArray()) {
                    for (JsonNode value : embedding) {
                        values.add(Double.valueOf(value.asDouble()));
                    }
                }
                vectors.add(new EmbeddingVector(item.path("index").asInt(vectors.size()), values));
            }
        }

        JsonNode usage = root.path("usage");
        LlmUsage tokenUsage = usage.isMissingNode()
                ? new LlmUsage(null, null, null)
                : new LlmUsage(
                        usage.path("prompt_tokens").isNumber() ? Integer.valueOf(usage.path("prompt_tokens").asInt()) : null,
                        usage.path("completion_tokens").isNumber()
                                ? Integer.valueOf(usage.path("completion_tokens").asInt())
                                : null,
                        usage.path("total_tokens").isNumber() ? Integer.valueOf(usage.path("total_tokens").asInt()) : null);

        return new EmbeddingResponse(
                root.path("model").asText(resolveModel(null)),
                config.getProvider(),
                vectors,
                tokenUsage,
                responseBody);
    }

    private String buildPayload(EmbeddingRequest request) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("model", resolveModel(request.getModel()));
        payload.put("input", request.getInputs());
        if (request.getDimensions() != null) {
            payload.put("dimensions", request.getDimensions());
        }
        if (request.getUser() != null && !request.getUser().isEmpty()) {
            payload.put("user_id", request.getUser());
        }
        return JsonSupport.writeJson(payload);
    }

    private String resolveModel(String requestModel) {
        if (requestModel != null && !requestModel.isEmpty()) {
            return requestModel;
        }
        if (config.getDefaultModel() != null && !config.getDefaultModel().isEmpty()) {
            return config.getDefaultModel();
        }
        throw new LlmException("No embedding model was provided in request or client config");
    }

    private static void validateRequest(EmbeddingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("embedding request is required");
        }
        if (request.getInputs() == null || request.getInputs().isEmpty()) {
            throw new IllegalArgumentException("embedding request requires at least one input");
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
