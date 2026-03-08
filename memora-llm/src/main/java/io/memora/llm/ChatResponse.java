package io.memora.llm;

public final class ChatResponse {
    private final String id;
    private final LlmProvider provider;
    private final String model;
    private final String content;
    private final LlmUsage usage;
    private final String rawBody;

    public ChatResponse(String id, LlmProvider provider, String model, String content, LlmUsage usage, String rawBody) {
        this.id = id;
        this.provider = provider;
        this.model = model;
        this.content = content;
        this.usage = usage;
        this.rawBody = rawBody;
    }

    public String getId() {
        return id;
    }

    public LlmProvider getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public String getContent() {
        return content;
    }

    public LlmUsage getUsage() {
        return usage;
    }

    public String getRawBody() {
        return rawBody;
    }
}

