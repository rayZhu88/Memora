package io.memora.llm;

public final class StreamingSupport {
    private StreamingSupport() {
    }

    public static boolean supportsStreaming(LlmClient client) {
        return client instanceof StreamingLlmClient;
    }

    public static StreamingLlmClient requireStreaming(LlmClient client) {
        if (client instanceof StreamingLlmClient) {
            return (StreamingLlmClient) client;
        }
        throw new LlmException("This LLM client does not support streaming");
    }
}

