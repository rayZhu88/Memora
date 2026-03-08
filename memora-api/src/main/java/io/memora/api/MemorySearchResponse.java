package io.memora.api;

import java.util.ArrayList;
import java.util.List;

public final class MemorySearchResponse {
    private String requestId;
    private String memoryContext;
    private List<MemoryItemView> items = new ArrayList<MemoryItemView>();

    public MemorySearchResponse() {
    }

    public MemorySearchResponse(String requestId, String memoryContext, List<MemoryItemView> items) {
        this.requestId = requestId;
        this.memoryContext = memoryContext;
        setItems(items);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMemoryContext() {
        return memoryContext;
    }

    public void setMemoryContext(String memoryContext) {
        this.memoryContext = memoryContext;
    }

    public List<MemoryItemView> getItems() {
        return items;
    }

    public void setItems(List<MemoryItemView> items) {
        this.items = items == null ? new ArrayList<MemoryItemView>() : new ArrayList<MemoryItemView>(items);
    }
}
