package io.memora.vector;

public final class VectorCollectionSpec {
    private final String name;
    private final int dimensions;
    private final VectorDistance distance;
    private final String vectorName;

    public VectorCollectionSpec(String name, int dimensions, VectorDistance distance, String vectorName) {
        this.name = name;
        this.dimensions = dimensions;
        this.distance = distance;
        this.vectorName = vectorName;
    }

    public String getName() {
        return name;
    }

    public int getDimensions() {
        return dimensions;
    }

    public VectorDistance getDistance() {
        return distance;
    }

    public String getVectorName() {
        return vectorName;
    }
}
