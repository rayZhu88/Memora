package io.memora.llm;

public interface EmbeddingClient {
    EmbeddingResponse embed(EmbeddingRequest request);
}
