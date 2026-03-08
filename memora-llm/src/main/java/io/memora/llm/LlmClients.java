package io.memora.llm;

import java.util.Collections;

public final class LlmClients {
    private LlmClients() {
    }

    public static LlmClient create(LlmClientConfig config) {
        return new HttpChatLlmClient(config);
    }

    public static LlmClient forKimi(String apiKey, String defaultModel) {
        return create(new LlmClientConfig(
                LlmProvider.KIMI,
                apiKey,
                defaultModel,
                null,
                10_000,
                60_000,
                Collections.<String, String>emptyMap()));
    }

    public static LlmClient forZhipu(String apiKey, String defaultModel) {
        return create(new LlmClientConfig(
                LlmProvider.ZHIPU,
                apiKey,
                defaultModel,
                null,
                10_000,
                60_000,
                Collections.<String, String>emptyMap()));
    }

    public static LlmClient forMiniMax(String apiKey, String defaultModel) {
        return create(new LlmClientConfig(
                LlmProvider.MINIMAX,
                apiKey,
                defaultModel,
                null,
                10_000,
                60_000,
                Collections.<String, String>emptyMap()));
    }
}

