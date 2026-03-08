package io.memora.vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VectorSearchRequest {
    private final String collectionName;
    private final List<Double> vector;
    private final int limit;
    private final String vectorName;
    private final Map<String, Object> filterEquals;

    public VectorSearchRequest(
            String collectionName,
            List<Double> vector,
            int limit,
            String vectorName,
            Map<String, Object> filterEquals) {
        this.collectionName = collectionName;
        this.vector = vector == null
                ? Collections.<Double>emptyList()
                : Collections.unmodifiableList(new ArrayList<Double>(vector));
        this.limit = limit;
        this.vectorName = vectorName;
        this.filterEquals = filterEquals == null
                ? Collections.<String, Object>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<String, Object>(filterEquals));
    }

    public String getCollectionName() {
        return collectionName;
    }

    public List<Double> getVector() {
        return vector;
    }

    public int getLimit() {
        return limit;
    }

    public String getVectorName() {
        return vectorName;
    }

    public Map<String, Object> getFilterEquals() {
        return filterEquals;
    }
}
