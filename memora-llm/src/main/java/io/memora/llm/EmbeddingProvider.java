package io.memora.llm;

public enum EmbeddingProvider {
    ZHIPU("https://open.bigmodel.cn/api/paas/v4", "/embeddings");

    private final String defaultBaseUrl;
    private final String embeddingsPath;

    EmbeddingProvider(String defaultBaseUrl, String embeddingsPath) {
        this.defaultBaseUrl = defaultBaseUrl;
        this.embeddingsPath = embeddingsPath;
    }

    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }

    public String getEmbeddingsPath() {
        return embeddingsPath;
    }
}
