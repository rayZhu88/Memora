package io.memora.transport.http;

import io.memora.api.MemoryAddRequest;
import io.memora.api.MemoryAddResponse;
import io.memora.api.MemoryController;
import io.memora.api.MemorySearchRequest;
import io.memora.api.MemorySearchResponse;
import io.memora.core.Memora;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/memory")
public class MemoryHttpController implements MemoryController {
    private final Memora memora;

    public MemoryHttpController(Memora memora) {
        this.memora = memora;
    }

    @PostMapping("/search")
    public MemorySearchResponse search(@RequestBody MemorySearchRequest request) {
        return memora.search(request);
    }

    @PostMapping("/add")
    public MemoryAddResponse add(@RequestBody MemoryAddRequest request) {
        return memora.add(request);
    }
}

