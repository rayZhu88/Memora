package io.memora.core;

public final class MemoryEntry {
    private final String id;
    private final String type;
    private final String scopeId;
    private final String sessionId;
    private final String source;
    private final String content;
    private final String payloadJson;
    private final long createdAt;

    public MemoryEntry(
            String id,
            String type,
            String scopeId,
            String sessionId,
            String source,
            String content,
            String payloadJson,
            long createdAt) {
        this.id = id;
        this.type = type;
        this.scopeId = scopeId;
        this.sessionId = sessionId;
        this.source = source;
        this.content = content;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getScopeId() {
        return scopeId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSource() {
        return source;
    }

    public String getContent() {
        return content;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
