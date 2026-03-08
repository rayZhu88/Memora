package io.memora.core;

import java.util.List;

public interface MemoryEntryStore {
    void saveAll(List<MemoryEntry> entries);

    List<MemoryEntry> search(String query, String scopeId, String sessionId, int limit);
}
