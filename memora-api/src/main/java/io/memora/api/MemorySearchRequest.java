package io.memora.api;

public final class MemorySearchRequest {
    private String query;
    private ClientContext context;
    private Integer topK;

    public MemorySearchRequest() {
    }

    public MemorySearchRequest(String query, ClientContext context, Integer topK) {
        this.query = query;
        this.context = context;
        this.topK = topK;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ClientContext getContext() {
        return context;
    }

    public void setContext(ClientContext context) {
        this.context = context;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
