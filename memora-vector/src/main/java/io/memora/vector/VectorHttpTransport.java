package io.memora.vector;

import java.util.Map;

interface VectorHttpTransport {
    VectorHttpResponse execute(
            String method,
            String endpoint,
            Map<String, String> headers,
            String payload,
            int connectTimeoutMs,
            int readTimeoutMs);
}
