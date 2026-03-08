package io.memora.vector;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class VectorSearchHit {
    private final String id;
    private final double score;
    private final Map<String, Object> payload;

    public VectorSearchHit(String id, double score, Map<String, Object> payload) {
        this.id = id;
        this.score = score;
        this.payload = payload == null
                ? Collections.<String, Object>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<String, Object>(payload));
    }

    public String getId() {
        return id;
    }

    public double getScore() {
        return score;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
