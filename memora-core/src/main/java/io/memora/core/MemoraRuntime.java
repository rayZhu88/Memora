package io.memora.core;

import io.memora.api.ClientContext;
import io.memora.api.MemoryAddPayload;
import io.memora.api.MemoryAddRequest;
import io.memora.api.MemoryAddResponse;
import io.memora.api.MemoryItemView;
import io.memora.api.MemoryMessage;
import io.memora.api.MemorySearchRequest;
import io.memora.api.MemorySearchResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class MemoraRuntime implements Memora {
    private static final String DEFAULT_SCOPE_ID = "default";
    private static final String CONVERSATION_TYPE = "conversation";
    private static final String NOTE_TYPE = "note";
    private static final int DEFAULT_TOP_K = 8;
    private static final int MAX_TOP_K = 20;

    private final MemoryEntryStore store;

    public MemoraRuntime(MemoryEntryStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public MemorySearchResponse search(MemorySearchRequest request) {
        validateSearchRequest(request);

        ClientContext context = request.getContext();
        String scopeId = resolveScopeId(context);
        String sessionId = context == null ? null : context.getSessionId();
        int limit = normalizeTopK(request.getTopK());
        List<MemoryEntry> entries = store.search(request.getQuery(), scopeId, sessionId, limit);
        List<MemoryItemView> items = toMemoryItems(entries);

        return new MemorySearchResponse(
                UUID.randomUUID().toString(),
                buildMemoryContext(request.getQuery(), scopeId, sessionId, items),
                items);
    }

    public MemoryAddResponse add(MemoryAddRequest request) {
        validateAddRequest(request);

        List<MemoryEntry> entries = toEntries(request);
        store.saveAll(entries);

        return new MemoryAddResponse(UUID.randomUUID().toString(), true, false);
    }

    private static List<MemoryItemView> toMemoryItems(List<MemoryEntry> entries) {
        List<MemoryItemView> items = new ArrayList<MemoryItemView>();
        for (MemoryEntry entry : entries) {
            items.add(new MemoryItemView(
                    entry.getId(),
                    entry.getType(),
                    entry.getScopeId(),
                    entry.getContent(),
                    null));
        }
        return items;
    }

    private static String buildMemoryContext(
            String query,
            String scopeId,
            String sessionId,
            List<MemoryItemView> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("Memora search scope: ")
                .append(scopeId)
                .append(". Query: ")
                .append(query)
                .append('.');

        if (sessionId != null && !sessionId.isEmpty()) {
            builder.append(" Session: ").append(sessionId).append('.');
        }

        if (!items.isEmpty()) {
            builder.append(" Relevant memories:");
            int count = Math.min(items.size(), 3);
            for (int i = 0; i < count; i++) {
                builder.append(' ').append(items.get(i).getContent());
            }
        }

        return builder.toString();
    }

    private List<MemoryEntry> toEntries(MemoryAddRequest request) {
        List<MemoryEntry> entries = new ArrayList<MemoryEntry>();
        ClientContext context = request.getContext();
        String scopeId = resolveScopeId(context);
        String sessionId = context == null ? null : context.getSessionId();
        String source = context == null ? null : context.getSource();
        long createdAt = System.currentTimeMillis();

        if (isConversationType(request.getType())) {
            for (MemoryMessage message : request.getPayload().getMessages()) {
                entries.add(new MemoryEntry(
                        UUID.randomUUID().toString(),
                        "conversation.message",
                        scopeId,
                        sessionId,
                        source,
                        message.getContent(),
                        JsonPayloads.object("role", message.getRole()),
                        createdAt));
            }
            return entries;
        }

        entries.add(new MemoryEntry(
                UUID.randomUUID().toString(),
                NOTE_TYPE,
                scopeId,
                sessionId,
                source,
                request.getPayload().getContent(),
                JsonPayloads.emptyObject(),
                createdAt));
        return entries;
    }

    private static String resolveScopeId(ClientContext context) {
        if (context != null && context.getScopeId() != null && !context.getScopeId().isEmpty()) {
            return context.getScopeId();
        }
        return DEFAULT_SCOPE_ID;
    }

    private static int normalizeTopK(Integer topK) {
        if (topK == null || topK.intValue() <= 0) {
            return DEFAULT_TOP_K;
        }
        return Math.min(topK.intValue(), MAX_TOP_K);
    }

    private static void validateSearchRequest(MemorySearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("search request is required");
        }
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("query is required");
        }
    }

    private static void validateAddRequest(MemoryAddRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("add request is required");
        }
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("type is required");
        }

        MemoryAddPayload payload = request.getPayload();
        if (payload == null) {
            throw new IllegalArgumentException("payload is required");
        }

        if (isConversationType(request.getType()) && !payload.hasMessages()) {
            throw new IllegalArgumentException("conversation payload requires messages");
        }
        if (isNoteType(request.getType()) && !payload.hasContent()) {
            throw new IllegalArgumentException("note payload requires content");
        }
        if (!isConversationType(request.getType()) && !isNoteType(request.getType())) {
            throw new IllegalArgumentException("type must be conversation or note");
        }
    }

    private static boolean isConversationType(String type) {
        return CONVERSATION_TYPE.equalsIgnoreCase(type);
    }

    private static boolean isNoteType(String type) {
        return NOTE_TYPE.equalsIgnoreCase(type);
    }
}
