package io.memora.llm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EmbeddingResponse {
    private final String model;
    private final EmbeddingProvider provider;
    private final List<EmbeddingVector> vectors;
    private final LlmUsage usage;
    private final String rawBody;

    public EmbeddingResponse(
            String model,
            EmbeddingProvider provider,
            List<EmbeddingVector> vectors,
            LlmUsage usage,
            String rawBody) {
        this.model = model;
        this.provider = provider;
        this.vectors = immutableCopy(vectors);
        this.usage = usage;
        this.rawBody = rawBody;
    }

    public String getModel() {
        return model;
    }

    public EmbeddingProvider getProvider() {
        return provider;
    }

    public List<EmbeddingVector> getVectors() {
        return vectors;
    }

    public LlmUsage getUsage() {
        return usage;
    }

    public String getRawBody() {
        return rawBody;
    }

    private static List<EmbeddingVector> immutableCopy(List<EmbeddingVector> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<EmbeddingVector>(source));
    }
}
