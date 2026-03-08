package io.memora.api;

public final class MemoryAddResponse {
    private String requestId;
    private boolean accepted;
    private boolean queued;

    public MemoryAddResponse() {
    }

    public MemoryAddResponse(String requestId, boolean accepted, boolean queued) {
        this.requestId = requestId;
        this.accepted = accepted;
        this.queued = queued;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isQueued() {
        return queued;
    }

    public void setQueued(boolean queued) {
        this.queued = queued;
    }
}
