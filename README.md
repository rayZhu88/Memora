# Memora

Memora is a Java-first memory framework for AI clients and agent runtimes.

The current repository is aligned to a conservative v1 northbound contract and a split runtime architecture:

- `search`: read memory and return both hits and injectable memory context
- `add`: write new memory from conversation turns or explicit notes
- pure Java core services
- Spring MVC only in the outer transport module

## Modules

- `memora-api`: northbound controller contract and DTOs
- `memora-adapter-spi`: adapter contracts for browser, CLI, desktop, and IDE integrations
- `memora-core`: internal `Memora` facade and pure Java application services
- `memora-llm`: provider-agnostic chat and embedding abstractions
- `memora-vector`: vector storage abstraction with Qdrant integration
- `memora-storage-sqlite`: SQLite-backed repository implementation, currently in the optional `integration-modules` Maven profile
- `memora-transport-http`: Spring Boot HTTP entrypoint, currently in the optional `integration-modules` Maven profile

The default HTTP bootstrap now uses an in-memory `MemoryEntryStore` so `add` and `search` work without extra storage setup.

## V1 API Shape

```text
POST /api/v1/memory/search
POST /api/v1/memory/add
```

Shared request context:

- `source`
- `scopeId`
- `sessionId`

## LLM Abstraction

`memora-llm` now provides:

- `LlmClient` for chat completions
- `EmbeddingClient` for text vectorization

Embedding support in v1:

- `ZHIPU` embedding models via `/embeddings`

V1 interaction model:

- sync `chat()` is the default path
- streaming is an optional future capability, not the primary integration mode

V1 provider profiles:

- `KIMI`
- `ZHIPU`
- `MINIMAX`

## Vector Support

`memora-vector` provides:

- `VectorStore`
- `QdrantVectorStore`

This keeps vector search optional and separate from the main memory store. `Memora` can continue using local `SQLite/FTS` for v1 while Qdrant is added as a semantic retrieval backend.

## Local Build

Memora now targets JDK 17. Make sure `java -version` and `mvn -version` both resolve to a Java 17 runtime before building.

```bash
mvn test
```

Optional integration modules:

```bash
mvn test -Pintegration-modules
```

## Next Steps

- finish SQLite-backed search ranking and write behavior
- add transport tests for the HTTP module
- wire `EmbeddingClient` and `VectorStore` into a hybrid retrieval pipeline
- add browser, CLI, and desktop adapters on top of the HTTP API
- introduce fact extraction and profile projection after the v1 API stabilizes
