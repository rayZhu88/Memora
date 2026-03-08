package io.memora.llm;

public interface ChatStreamHandler {
    void onStart();

    void onDelta(String delta);

    void onComplete(ChatResponse response);

    void onError(Throwable error);
}

