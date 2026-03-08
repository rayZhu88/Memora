package io.memora.llm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class EmbeddingRequest {
    private final String model;
    private final List<String> inputs;
    private final Integer dimensions;
    private final String user;

    public EmbeddingRequest(String model, List<String> inputs, Integer dimensions, String user) {
        this.model = model;
        this.inputs = immutableCopy(Objects.requireNonNull(inputs, "inputs"));
        this.dimensions = dimensions;
        this.user = user;
    }

    public String getModel() {
        return model;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public Integer getDimensions() {
        return dimensions;
    }

    public String getUser() {
        return user;
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        if (source.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
