package io.memora.llm;

final class HttpResponse {
    private final int statusCode;
    private final String body;

    HttpResponse(int statusCode, String body) {
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
