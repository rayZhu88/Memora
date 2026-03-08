package io.memora.llm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ChatRequest {
    private final String model;
    private final List<ChatMessage> messages;
    private final Double temperature;
    private final Double topP;
    private final Integer maxTokens;

    public ChatRequest(String model, List<ChatMessage> messages, Double temperature, Double topP, Integer maxTokens) {
        this.model = model;
        this.messages = immutableCopy(Objects.requireNonNull(messages, "messages"));
        this.temperature = temperature;
        this.topP = topP;
        this.maxTokens = maxTokens;
    }

    public String getModel() {
        return model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        if (source.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<T>(source));
    }
}

