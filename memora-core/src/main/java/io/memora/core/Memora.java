package io.memora.core;

import io.memora.api.MemoryAddRequest;
import io.memora.api.MemoryAddResponse;
import io.memora.api.MemorySearchRequest;
import io.memora.api.MemorySearchResponse;

public interface Memora {
    MemorySearchResponse search(MemorySearchRequest request);

    MemoryAddResponse add(MemoryAddRequest request);
}

