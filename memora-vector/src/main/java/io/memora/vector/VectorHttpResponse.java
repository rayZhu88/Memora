package io.memora.vector;

final class VectorHttpResponse {
    private final int statusCode;
    private final String body;

    VectorHttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    int getStatusCode() {
        return statusCode;
    }

    String getBody() {
        return body;
    }
}
