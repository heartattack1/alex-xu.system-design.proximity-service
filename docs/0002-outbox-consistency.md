# ADR-0002: Consistency Between Business Data and Geo Index — Outbox Pattern

- Status: Accepted
- Date: 2026-01-09
- Deciders: Project team
- Context: Updates to business location/details must reflect in geo-index and caches with acceptable freshness.

## Context

The system maintains:
- **Business data** (source of truth) in the primary relational database.
- **Geo index** used by nearby search to retrieve candidates.
- **Caches** (Redis) for business summaries and/or geo index acceleration.

When a business is created/updated/deleted, we must update geo-index and invalidate caches.
Direct synchronous updates can couple write latency and availability to secondary systems.

## Decision

Adopt the **Outbox Pattern** for propagating business changes:
- Business Service writes business row(s) and an outbox event **in the same DB transaction**.
- A publisher reads outbox events and publishes to a message broker (or a reliable queue).
- A consumer updates geo-index and cache entries asynchronously.
- Nearby Search tolerates eventual consistency between business source of truth and geo-index.

MVP fallback:
- For early stages (no broker), implement synchronous geo_index update post-commit, with clear interfaces to replace by outbox later.

## Rationale

- Guarantees event publication without dual-write inconsistency.
- Keeps write path fast and resilient.
- Supports scaling and future microservice separation.
- Aligns with eventual consistency requirements for LBS.

## Consequences

### Positive
- Reliable propagation of changes.
- Reduced coupling between services and data stores.
- Better observability with event streams.

### Negative
- Requires message broker and consumer infrastructure.
- Introduces replication lag / eventual consistency.
- Requires idempotency and deduplication handling in consumer.

## Implementation Notes

- Outbox table fields: `id`, `aggregate_id`, `event_type`, `payload_json`, `created_at`, `published_at`, `status`.
- Publisher should be at-least-once; consumer must be idempotent (e.g., upsert geo_index).
- Cache invalidation strategy: delete-by-key on update; avoid partial stale reads.

## Follow-ups

- Add retry + DLQ for failed events.
- Add metrics: outbox backlog, publish latency, consumer lag.
- Add integration tests for end-to-end propagation.
