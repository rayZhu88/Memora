package io.memora.vector;

import java.util.Objects;

public final class DashVectorStoreConfig {
    private final String endpoint;
    private final String apiKey;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public DashVectorStoreConfig(String endpoint, String apiKey, int connectTimeoutMs, int readTimeoutMs) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
        this.connectTimeoutMs = connectTimeoutMs <= 0 ? 10_000 : connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs <= 0 ? 60_000 : readTimeoutMs;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getApiKey() {
        return apiKey;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }
}
