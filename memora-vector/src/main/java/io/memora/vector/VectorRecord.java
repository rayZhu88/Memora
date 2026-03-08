package io.memora.vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VectorRecord {
    private final String id;
    private final List<Double> vector;
    private final Map<String, Object> payload;

    public VectorRecord(String id, List<Double> vector, Map<String, Object> payload) {
        this.id = id;
        this.vector = vector == null
                ? Collections.<Double>emptyList()
                : Collections.unmodifiableList(new ArrayList<Double>(vector));
        this.payload = payload == null
                ? Collections.<String, Object>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<String, Object>(payload));
    }

    public String getId() {
        return id;
    }

    public List<Double> getVector() {
        return vector;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
