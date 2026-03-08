package io.memora.adapter.spi;

import io.memora.api.MemoryAddPayload;
import io.memora.api.MemoryAddRequest;
import io.memora.api.MemorySearchRequest;
import io.memora.api.MemorySearchResponse;

public interface ClientAdapter {
    String id();

    MemorySearchRequest buildSearchRequest(String query, AdapterContext context, Integer topK);

    String injectMemoryContext(String userInput, MemorySearchResponse response);

    MemoryAddRequest buildAddRequest(String type, MemoryAddPayload payload, AdapterContext context);
}

