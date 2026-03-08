package io.memora.core;

import io.memora.api.ClientContext;
import io.memora.api.MemoryAddPayload;
import io.memora.api.MemoryAddRequest;
import io.memora.api.MemoryAddResponse;
import io.memora.api.MemoryMessage;
import io.memora.api.MemorySearchRequest;
import io.memora.api.MemorySearchResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public final class MemoraRuntimeTest {
    @Test
    public void searchReturnsItemsAndContext() {
        RecordingStore store = new RecordingStore();
        store.searchResults = Collections.singletonList(new MemoryEntry(
                "mem-1",
                "note",
                "memora",
                "s1",
                "codex",
                "v1 should stay small",
                "{}",
                1L));

        MemoraRuntime memora = new MemoraRuntime(store);
        MemorySearchResponse response = memora.search(new MemorySearchRequest(
                "how should v1 look",
                new ClientContext("codex-desktop", "memora", "s1"),
                5));

        Assert.assertEquals("how should v1 look", store.lastQuery);
        Assert.assertEquals("memora", store.lastScopeId);
        Assert.assertEquals("s1", store.lastSessionId);
        Assert.assertEquals(1, response.getItems().size());
        Assert.assertTrue(response.getMemoryContext().contains("Memora search scope: memora."));
        Assert.assertTrue(response.getMemoryContext().contains("Relevant memories: v1 should stay small"));
    }

    @Test
    public void addConversationCreatesOneEntryPerMessage() {
        RecordingStore store = new RecordingStore();
        MemoraRuntime memora = new MemoraRuntime(store);

        MemoryAddResponse response = memora.add(new MemoryAddRequest(
                "conversation",
                new MemoryAddPayload(null, Arrays.asList(
                        new MemoryMessage("user", "first"),
                        new MemoryMessage("assistant\nrole", "second"))),
                new ClientContext("codex-desktop", "memora", "s1")));

        Assert.assertTrue(response.isAccepted());
        Assert.assertEquals(2, store.savedEntries.size());
        Assert.assertEquals("conversation.message", store.savedEntries.get(0).getType());
        Assert.assertEquals("first", store.savedEntries.get(0).getContent());
        Assert.assertEquals("{\"role\":\"assistant\\nrole\"}", store.savedEntries.get(1).getPayloadJson());
    }

    @Test
    public void jsonPayloadEncoderEscapesControlCharacters() {
        Assert.assertEquals(
                "{\"role\":\"assistant\\\\\\\"\\n\\t\"}",
                JsonPayloads.object("role", "assistant\\\"\n\t"));
        Assert.assertEquals("{}", JsonPayloads.emptyObject());
    }

    private static final class RecordingStore implements MemoryEntryStore {
        private final List<MemoryEntry> savedEntries = new ArrayList<MemoryEntry>();
        private List<MemoryEntry> searchResults = Collections.emptyList();
        private String lastQuery;
        private String lastScopeId;
        private String lastSessionId;
        private int lastLimit;

        public void saveAll(List<MemoryEntry> entries) {
            savedEntries.addAll(entries);
        }

        public List<MemoryEntry> search(String query, String scopeId, String sessionId, int limit) {
            lastQuery = query;
            lastScopeId = scopeId;
            lastSessionId = sessionId;
            lastLimit = limit;
            return searchResults;
        }
    }
}
