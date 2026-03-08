package io.memora.llm;

import java.util.Collections;

public final class EmbeddingClients {
    private EmbeddingClients() {
    }

    public static EmbeddingClient create(EmbeddingClientConfig config) {
        return new HttpEmbeddingClient(config);
    }

    public static EmbeddingClient forZhipu(String apiKey, String defaultModel) {
        return create(new EmbeddingClientConfig(
                EmbeddingProvider.ZHIPU,
                apiKey,
                defaultModel,
                null,
                10_000,
                60_000,
                Collections.<String, String>emptyMap()));
    }
}
