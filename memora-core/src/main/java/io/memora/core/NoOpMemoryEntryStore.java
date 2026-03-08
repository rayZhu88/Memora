package io.memora.core;

import java.util.Collections;
import java.util.List;

public final class NoOpMemoryEntryStore implements MemoryEntryStore {
    public void saveAll(List<MemoryEntry> entries) {
        // Default bootstrap store. Real persistence is provided by storage modules.
    }

    public List<MemoryEntry> search(String query, String scopeId, String sessionId, int limit) {
        return Collections.emptyList();
    }
}
