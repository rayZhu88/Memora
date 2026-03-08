package io.memora.llm;

public interface StreamingLlmClient extends LlmClient {
    void stream(ChatRequest request, ChatStreamHandler handler);
}

