package io.memora.api;

public interface MemoryController {
    MemorySearchResponse search(MemorySearchRequest request);

    MemoryAddResponse add(MemoryAddRequest request);
}

