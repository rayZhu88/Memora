package io.memora.api;

public final class MemoryAddRequest {
    private String type;
    private MemoryAddPayload payload;
    private ClientContext context;

    public MemoryAddRequest() {
    }

    public MemoryAddRequest(String type, MemoryAddPayload payload, ClientContext context) {
        this.type = type;
        this.payload = payload;
        this.context = context;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MemoryAddPayload getPayload() {
        return payload;
    }

    public void setPayload(MemoryAddPayload payload) {
        this.payload = payload;
    }

    public ClientContext getContext() {
        return context;
    }

    public void setContext(ClientContext context) {
        this.context = context;
    }
}
