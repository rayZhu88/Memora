package io.memora.adapter.spi;

import io.memora.api.ClientContext;

public final class AdapterContext {
    private final String source;
    private final String scopeId;
    private final String sessionId;

    public AdapterContext(String source, String scopeId, String sessionId) {
        this.source = source;
        this.scopeId = scopeId;
        this.sessionId = sessionId;
    }

    public ClientContext toClientContext() {
        return new ClientContext(source, scopeId, sessionId);
    }
}
