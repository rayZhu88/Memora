package io.memora.transport.http;

import io.memora.core.InMemoryEntryStore;
import io.memora.core.Memora;
import io.memora.core.MemoraRuntime;
import io.memora.core.MemoryEntryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoraHttpConfiguration {
    @Bean
    public MemoryEntryStore memoryEntryStore() {
        return new InMemoryEntryStore();
    }

    @Bean
    public Memora memora(MemoryEntryStore store) {
        return new MemoraRuntime(store);
    }
}
