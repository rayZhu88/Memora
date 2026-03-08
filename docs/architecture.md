# Memora Architecture

Memora v1 is intentionally small. The external interface is a controller contract with only two operations:

- `search`
- `add`

## Northbound Contract

### `search`

Purpose:

- return memory hits
- return an assembled memory context that can be injected before a prompt is sent

Request shape:

- `query`
- `context.source`
- `context.scopeId`
- `context.sessionId`
- `topK`

### `add`

Purpose:

- ingest conversation messages
- ingest explicit notes

Request shape:

- `type`
- `payload.content`
- `payload.messages`
- `context.source`
- `context.scopeId`
- `context.sessionId`

## Layering

### `memora-api`

Public controller contract and northbound DTOs.

### `memora-adapter-spi`

Edge adapters build `search` and `add` requests and inject returned memory context into client prompts.

### `memora-core`

Internal application facade named `Memora`. It owns the implementation behind the public contract and remains plain Java.

### `memora-llm`

Provider-agnostic chat abstraction. V1 keeps a single `LlmClient` interface and hides provider differences behind config:

- `KIMI`
- `ZHIPU`
- `MINIMAX`

Interaction model:

- synchronous chat is the default
- a separate streaming SPI exists, but streaming is not the primary path for internal memory workflows

### `memora-storage-sqlite`

SQLite-backed repository implementation for v1 local-first storage.

### `memora-transport-http`

Spring MVC transport only. It exposes `/api/v1/memory/search` and `/api/v1/memory/add` without pushing Spring into the core.

## V1 Scope

What is intentionally included:

- search
- add
- scope-aware context
- session-aware context
- pure Java core plus separate Spring HTTP shell

What is intentionally deferred:

- profile-specific APIs
- feedback-specific APIs
- admin APIs
- fetch or retrieve as public contracts
- complex filter DSL
