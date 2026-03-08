package io.memora.llm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EmbeddingVector {
    private final int index;
    private final List<Double> values;

    public EmbeddingVector(int index, List<Double> values) {
        this.index = index;
        this.values = immutableCopy(values);
    }

    public int getIndex() {
        return index;
    }

    public List<Double> getValues() {
        return values;
    }

    private static List<Double> immutableCopy(List<Double> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<Double>(source));
    }
}
