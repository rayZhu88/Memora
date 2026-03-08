package io.memora.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class InMemoryEntryStore implements MemoryEntryStore {
    private final List<MemoryEntry> entries = Collections.synchronizedList(new ArrayList<MemoryEntry>());

    public void saveAll(List<MemoryEntry> newEntries) {
        if (newEntries == null || newEntries.isEmpty()) {
            return;
        }
        entries.addAll(new ArrayList<MemoryEntry>(newEntries));
    }

    public List<MemoryEntry> search(String query, String scopeId, String sessionId, int limit) {
        if (query == null || query.trim().isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }

        String normalizedQuery = query.trim().toLowerCase();
        List<MemoryEntry> snapshot;
        synchronized (entries) {
            snapshot = new ArrayList<MemoryEntry>(entries);
        }

        List<MemoryEntry> matches = new ArrayList<MemoryEntry>();
        for (MemoryEntry entry : snapshot) {
            if (!matchesScope(entry, scopeId)) {
                continue;
            }
            if (!matchesQuery(entry, normalizedQuery)) {
                continue;
            }
            matches.add(entry);
        }

        Collections.sort(matches, new Comparator<MemoryEntry>() {
            public int compare(MemoryEntry left, MemoryEntry right) {
                boolean leftSessionMatch = matchesSession(left, sessionId);
                boolean rightSessionMatch = matchesSession(right, sessionId);
                if (leftSessionMatch != rightSessionMatch) {
                    return leftSessionMatch ? -1 : 1;
                }
                return Long.compare(right.getCreatedAt(), left.getCreatedAt());
            }
        });

        if (matches.size() <= limit) {
            return matches;
        }
        return new ArrayList<MemoryEntry>(matches.subList(0, limit));
    }

    private static boolean matchesScope(MemoryEntry entry, String scopeId) {
        if (scopeId == null) {
            return true;
        }
        return scopeId.equals(entry.getScopeId());
    }

    private static boolean matchesQuery(MemoryEntry entry, String normalizedQuery) {
        String content = entry.getContent();
        if (content == null) {
            return false;
        }
        return content.toLowerCase().contains(normalizedQuery);
    }

    private static boolean matchesSession(MemoryEntry entry, String sessionId) {
        return sessionId != null && sessionId.equals(entry.getSessionId());
    }
}
