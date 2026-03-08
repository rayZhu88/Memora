package io.memora.api;

public final class MemoryMessage {
    private String role;
    private String content;

    public MemoryMessage() {
    }

    public MemoryMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
