# Valkey-to-Kotlin Migration Plan

## Overview
This document outlines a practical roadmap to port the core of Valkey from C to Kotlin. 
The approach focuses on a thin, working slice first (network → parse → dispatch → data structures → reply), 
then iterative expansion to cover more data types and features.

## Goals
- Build a functional, idiomatic Kotlin implementation that preserves Valkey semantics.
- Prioritize correctness, predictable latency, and incremental rehash behavior.
- Reach a useful MVP quickly, then expand command coverage and features.

## Guiding Principles
- Favor incremental, test-driven porting of self-contained modules.
- Keep data-path hot spots allocation-free or allocation-light (primitive arrays, inline classes).
- Preserve incremental rehash and SCAN cursor semantics.
- Keep a minimal single-threaded event loop initially; shard and parallelize later.

## Phase Breakdown and Initial File Targets

### Phase 0: Foundations (primitives/utilities)
- String abstraction and helpers (SDS analog) — Kotlin String/ByteArray wrappers.
- Hashing and checksums — 64-bit keyed hash (e.g., SipHash), CRC if needed.
- String matching — glob-style pattern matcher for MATCH.
- Utility helpers — parsing integers, time helpers (LRU/LFU clock later).

Deliverables:
- ByteArray-backed string utils
- SipHash/XXH hashing with global seed
- Glob matcher

### Phase 1: Core In-Memory Engine
- Hashtable/dictionary with incremental rehash and SCAN behavior.
- Optional (defer if needed): compact encodings (listpack/intset), quicklist for lists.

Deliverables:
- Two-table hashtable with:
    - insert/find/delete
    - rehashIdx and incremental rehash steps
    - scan(cursor, count, pattern, onlyKeys) supporting masks and rehash boundaries
- Reverse-bit cursor nextCursor(mask) behavior

### Phase 2: Server Runtime (minimal)
- Event loop (Kotlin NIO/coroutines), non-blocking sockets.
- Networking: client state, input/output buffers.
- RESP protocol parser/writer.
- Command registry and dispatch.

Deliverables:
- Minimal server skeleton
- RESP support for arrays, bulk strings, errors, integers, simple strings
- Command registration and invocation

### Phase 3: Database Layer and Keyspace
- Database abstraction (keyspace, multiple DBs as needed).
- Expiration subsystem (store expiries; lazy expire first, active cycle later).
- Key lookup helpers, type checking, shared replies.

Deliverables:
- Keyspace dict + expiry dict
- Lazy expiration on access
- Helpers for consistent type checks and error replies

### Phase 4: Core Commands (breadth-first)
- Strings: PING, ECHO, SET, GET, DEL, EXISTS, INCR/DECR.
- Hash: HSET, HGET, HDEL, HLEN, HGETALL, HSCAN (hashtable-backed first).
- Set: SADD, SREM, SISMEMBER, SCARD, SSCAN (hashtable-backed first).
- Zset: ZADD, ZREM, ZSCORE, ZCARD, ZRANGE, ZSCAN (skiplist + dict later).
- SCAN suite for keyspace.

Deliverables:
- Working cache with strings
- Hash/Set backed by hashtable encoding
- SCAN/HSCAN/SSCAN with COUNT and MATCH

### Phase 5: Persistence and Replication (later milestones)
- RDB snapshotting, AOF rewrite.
- Replication (PSYNC), backlog.
- Cluster (hash slots, redirections).
- Pub/Sub, transactions, scripting.

Deliverables:
- Start with RDB read/write, then AOF
- Replication protocol plumbing
- Cluster only after core stability

## Minimal First Milestone (MVP)
- Hashing seed + hashtable with incremental rehash + scan(cursor).
- RESP parser/writer and minimal server loop.
- Commands: PING, ECHO, SET, GET, DEL.
- Keyspace with lazy expiration.
- SCAN over keyspace.

Acceptance:
- Byte-for-byte RESP compatibility for the above commands.
- Stable latency during steady inserts with incremental rehash enabled.
- SCAN returns consistent cursor and bounded work per call.

## Next Milestone
- Hash and Set types with HSET/HGET/HSCAN and SADD/SISMEMBER/SSCAN.
- COUNT and MATCH behavior validated against reference behavior.
- Active expire cycle (time-bounded per event loop tick).

## Kotlin-Specific Design Notes
- Types:
    - size_t → ULong or UInt (be consistent).
    - uint64_t → ULong; int8_t/int16_t → Byte/Short.
- Data layout:
    - Use primitive arrays for buckets/metadata.
    - Replace pointers with handles/indices.
- Hashing:
    - Use a strong 64-bit hash (e.g., SipHash) with a seed; cache high bits in bucket metadata to minimize key comparisons.
- Rehashing:
    - Two tables; move N buckets per write (and optionally read).
    - Skip already-moved buckets when scanning.
- SCAN cursor:
    - Implement reverse-bit increment to match expected progression and low bias.
- Performance:
    - Inline value classes for small wrappers (Cursor, Hash64).
    - Avoid boxing of unsigned types on hot paths.
    - Pooled buffers for I/O.

## Testing Strategy
- Golden protocol tests:
    - Compare RESP requests/responses for MVP commands against a reference server.
- Property tests:
    - Hashtable invariants across insert/delete/rehash.
    - SCAN eventually visits entries; bounded iterations per call.
    - Concurrent modifications during scan don’t break invariants (single-threaded with interleaved operations).
- Microbenchmarks:
    - Hash computation, lookup/insert latency, rehash step overhead.
    - SCAN throughput at various load factors and chain depths.
- Fuzzing:
    - RESP parser fuzz inputs to ensure robustness.

## Risks and Mitigations
- Scan/rehash edge cases:
    - Mirror cursor/mask behavior exactly and build dedicated tests.
- GC pressure:
    - Prefer primitive arrays and object reuse on hot paths.
- I/O backpressure:
    - Use bounded output buffers and event-driven flushing.

## Execution Checklist
- Foundations
    - [ ] Hashing seed + SipHash/XXH3
    - [ ] Glob matcher
- Hashtable
    - [ ] Two-table structure with rehashIdx
    - [ ] Insert/find/delete
    - [ ] nextCursor and scan respecting masks and rehash state
- Server/Protocol
    - [ ] RESP parser/writer
    - [ ] Event loop + client I/O
    - [ ] Command registry
- MVP Commands
    - [ ] PING, ECHO, SET, GET, DEL
    - [ ] SCAN (keyspace)
- Expiry
    - [ ] Lazy expire; active cycle (later)
- Extended Commands
    - [ ] Hash: HSET/HGET/HDEL/HLEN/HGETALL/HSCAN
    - [ ] Set: SADD/SREM/SISMEMBER/SCARD/SSCAN

## Timeline (suggested)
- Week 1: Foundations + Hashtable core + unit tests.
- Week 2: RESP + server loop + MVP commands + SCAN.
- Week 3: Keyspace expiry + Hash type + HSCAN.
- Week 4: Set type + SSCAN + active expire; harden tests/benchmarks.

## Next Steps
- Confirm target feature scope for MVP.
- Decide on the hashing implementation and finalize unsigned type choices.
- Start with the hashtable module and RESP parser, then glue together a thin working server with PING/ECHO/SET/GET/DEL.