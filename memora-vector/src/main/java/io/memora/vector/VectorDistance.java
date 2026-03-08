package io.memora.vector;

public enum VectorDistance {
    COSINE("Cosine"),
    DOT("Dot"),
    EUCLID("Euclid");

    private final String wireValue;

    VectorDistance(String wireValue) {
        this.wireValue = wireValue;
    }

    public String getWireValue() {
        return wireValue;
    }
}
