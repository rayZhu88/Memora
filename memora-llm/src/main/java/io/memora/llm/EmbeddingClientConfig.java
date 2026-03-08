package io.memora.llm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class EmbeddingClientConfig {
    private final EmbeddingProvider provider;
    private final String apiKey;
    private final String defaultModel;
    private final String baseUrl;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final Map<String, String> extraHeaders;

    public EmbeddingClientConfig(
            EmbeddingProvider provider,
            String apiKey,
            String defaultModel,
            String baseUrl,
            int connectTimeoutMs,
            int readTimeoutMs,
            Map<String, String> extraHeaders) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
        this.defaultModel = defaultModel;
        this.baseUrl = baseUrl == null || baseUrl.isEmpty() ? provider.getDefaultBaseUrl() : baseUrl;
        this.connectTimeoutMs = connectTimeoutMs <= 0 ? 10_000 : connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs <= 0 ? 60_000 : readTimeoutMs;
        this.extraHeaders = immutableCopy(extraHeaders);
    }

    public EmbeddingProvider getProvider() {
        return provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public Map<String, String> getExtraHeaders() {
        return extraHeaders;
    }

    private static Map<String, String> immutableCopy(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(source));
    }
}
