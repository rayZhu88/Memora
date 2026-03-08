package io.memora.core;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public final class InMemoryEntryStoreTest {
    @Test
    public void searchFiltersByScopeAndOrdersSessionHitsFirst() {
        InMemoryEntryStore store = new InMemoryEntryStore();
        store.saveAll(Arrays.asList(
                new MemoryEntry("1", "note", "memora", "s1", "codex", "search contract", "{}", 100L),
                new MemoryEntry("2", "note", "memora", "s2", "codex", "search contract", "{}", 200L),
                new MemoryEntry("3", "note", "other", "s1", "codex", "search contract", "{}", 300L)));

        List<MemoryEntry> results = store.search("search", "memora", "s1", 10);

        Assert.assertEquals(2, results.size());
        Assert.assertEquals("1", results.get(0).getId());
        Assert.assertEquals("2", results.get(1).getId());
    }

    @Test
    public void searchIsCaseInsensitiveAndRespectsLimit() {
        InMemoryEntryStore store = new InMemoryEntryStore();
        store.saveAll(Arrays.asList(
                new MemoryEntry("1", "note", "memora", null, "codex", "Memora Search API", "{}", 100L),
                new MemoryEntry("2", "note", "memora", null, "codex", "search ranking", "{}", 200L)));

        List<MemoryEntry> results = store.search("search", "memora", null, 1);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals("2", results.get(0).getId());
    }
}
