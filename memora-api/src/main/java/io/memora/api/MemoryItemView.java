package io.memora.api;

public final class MemoryItemView {
    private String id;
    private String type;
    private String scopeId;
    private String content;
    private Double score;

    public MemoryItemView() {
    }

    public MemoryItemView(String id, String type, String scopeId, String content, Double score) {
        this.id = id;
        this.type = type;
        this.scopeId = scopeId;
        this.content = content;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
