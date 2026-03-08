package io.memora.llm;

public enum LlmProvider {
    KIMI("https://api.moonshot.cn/v1", "/chat/completions"),
    ZHIPU("https://open.bigmodel.cn/api/paas/v4", "/chat/completions"),
    MINIMAX("https://api.minimax.io/v1", "/chat/completions");

    private final String defaultBaseUrl;
    private final String chatCompletionsPath;

    LlmProvider(String defaultBaseUrl, String chatCompletionsPath) {
        this.defaultBaseUrl = defaultBaseUrl;
        this.chatCompletionsPath = chatCompletionsPath;
    }

    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }

    public String getChatCompletionsPath() {
        return chatCompletionsPath;
    }
}

