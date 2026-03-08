package io.memora.llm;

import java.util.Map;

interface HttpTransport {
    HttpResponse post(
            String endpoint,
            Map<String, String> headers,
            String payload,
            int connectTimeoutMs,
            int readTimeoutMs);
}
