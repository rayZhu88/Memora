package io.memora.api;

public final class ClientContext {
    private String source;
    private String scopeId;
    private String sessionId;

    public ClientContext() {
    }

    public ClientContext(String source, String scopeId, String sessionId) {
        this.source = source;
        this.scopeId = scopeId;
        this.sessionId = sessionId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
