package io.memora.vector;

import java.util.List;

public interface VectorStore {
    void ensureCollection(VectorCollectionSpec spec);

    void upsert(String collectionName, String vectorName, List<VectorRecord> records);

    List<VectorSearchHit> search(VectorSearchRequest request);
}
