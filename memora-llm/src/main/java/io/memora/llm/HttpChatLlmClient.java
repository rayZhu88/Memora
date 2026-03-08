package io.memora.llm;

import io.memora.llm.internal.JsonSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class HttpChatLlmClient implements LlmClient {
    private final LlmClientConfig config;

    public HttpChatLlmClient(LlmClientConfig config) {
        this.config = config;
    }

    public ChatResponse chat(ChatRequest request) {
        validateRequest(request);

        String endpoint = joinUrl(config.getBaseUrl(), config.getProvider().getChatCompletionsPath());
        String payload = buildPayload(request);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(config.getConnectTimeoutMs());
            connection.setReadTimeout(config.getReadTimeoutMs());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            for (Map.Entry<String, String> header : config.getExtraHeaders().entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }

            byte[] body = payload.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(body.length);
            OutputStream outputStream = connection.getOutputStream();
            try {
                outputStream.write(body);
                outputStream.flush();
            } finally {
                outputStream.close();
            }

            int status = connection.getResponseCode();
            String responseBody = status >= 400
                    ? readBody(connection.getErrorStream())
                    : readBody(connection.getInputStream());

            if (status >= 400) {
                throw new LlmException("LLM request failed with status " + status + ": " + responseBody);
            }

            return parseResponse(responseBody);
        } catch (IOException exception) {
            throw new LlmException("Failed to call LLM provider " + config.getProvider(), exception);
        }
    }

    /*
     * Streaming is intentionally not implemented in v1.
     * The sync chat path remains the default for Memora internal workflows.
     */

    private ChatResponse parseResponse(String responseBody) {
        Map<String, Object> root = JsonSupport.parseObject(responseBody);
        List<Object> choices = JsonSupport.readList(root, "choices");
        Map<String, Object> firstChoice = choices.isEmpty() ? null : JsonSupport.asObject(choices.get(0));
        Map<String, Object> message = firstChoice == null ? null : JsonSupport.readObject(firstChoice, "message");

        String content = message == null ? null : JsonSupport.readString(message, "content");
        String id = JsonSupport.readString(root, "id");
        String model = JsonSupport.readString(root, "model");

        Map<String, Object> usage = JsonSupport.readObject(root, "usage");
        LlmUsage tokenUsage = usage == null
                ? new LlmUsage(null, null, null)
                : new LlmUsage(
                        JsonSupport.readInteger(usage, "prompt_tokens"),
                        JsonSupport.readInteger(usage, "completion_tokens"),
                        JsonSupport.readInteger(usage, "total_tokens"));

        return new ChatResponse(id, config.getProvider(), model, content, tokenUsage, responseBody);
    }

    private String buildPayload(ChatRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        builder.append("\"model\":").append(JsonSupport.quote(resolveModel(request))).append(',');
        builder.append("\"stream\":false,");
        builder.append("\"messages\":[");

        List<ChatMessage> messages = request.getMessages();
        for (int index = 0; index < messages.size(); index++) {
            ChatMessage message = messages.get(index);
            if (index > 0) {
                builder.append(',');
            }
            builder.append('{')
                    .append("\"role\":").append(JsonSupport.quote(message.getRole().getWireValue())).append(',')
                    .append("\"content\":").append(JsonSupport.quote(message.getContent()))
                    .append('}');
        }
        builder.append(']');

        if (request.getTemperature() != null) {
            builder.append(",\"temperature\":").append(request.getTemperature());
        }
        if (request.getTopP() != null) {
            builder.append(",\"top_p\":").append(request.getTopP());
        }
        if (request.getMaxTokens() != null) {
            builder.append(",\"max_tokens\":").append(request.getMaxTokens());
        }

        builder.append('}');
        return builder.toString();
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

    private static String readBody(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        try {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } finally {
            reader.close();
        }
    }
}
