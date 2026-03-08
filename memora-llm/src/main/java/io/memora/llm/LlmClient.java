package io.memora.llm;

public interface LlmClient {
    ChatResponse chat(ChatRequest request);
}

