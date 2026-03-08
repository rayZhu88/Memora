package io.memora.api;

import java.util.ArrayList;
import java.util.List;

public final class MemoryAddPayload {
    private String content;
    private List<MemoryMessage> messages = new ArrayList<MemoryMessage>();

    public MemoryAddPayload() {
    }

    public MemoryAddPayload(String content, List<MemoryMessage> messages) {
        this.content = content;
        setMessages(messages);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<MemoryMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<MemoryMessage> messages) {
        this.messages = messages == null ? new ArrayList<MemoryMessage>() : new ArrayList<MemoryMessage>(messages);
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    public boolean hasMessages() {
        return messages != null && !messages.isEmpty();
    }
}
