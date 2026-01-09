# ADR-0004: Caching Strategy — Business Details and Search-Aware Keys

- Status: Accepted
- Date: 2026-01-09
- Deciders: Project team
- Context: Nearby search and business detail reads are read-heavy and benefit from caching.

## Context

Two main read patterns exist:
1. **Business details**: `GET /v1/businesses/{id}` is frequently requested and is stable for short periods.
2. **Nearby search**: `GET /v1/search/nearby` depends on (lat, lon, radius) and can be expensive if it repeatedly hits DB.

Caching must not compromise correctness:
- results must remain within radius (post-filtering still required);
- invalidation must ensure updates are reflected within acceptable time.

## Decision

Implement a two-layer caching strategy using Redis:

### 1) Business details / summaries cache (primary)
- Cache key: `business:{id}` (details) and/or `business_summary:{id}`
- TTL: 5–30 minutes (configurable)
- Invalidation:
  - On business update/delete: delete cache keys for that id
  - Using outbox consumer (target) or synchronous invalidation (MVP)

### 2) Search-adjacent cache (optional, guarded)
Cache *intermediate* results rather than final response when possible:
- Cache candidate ID sets per geohash cell + precision:
  - Key: `geo:candidates:{geohash_prefix}`
  - Value: list/set of business ids (or Redis Set)
  - TTL: 10–60 minutes (configurable)
- Nearby search still performs final Haversine filtering on each request.

We do **not** cache final nearby responses by raw lat/lon due to high cardinality.
If needed later, consider caching by `(geohash_prefix, radius_bucket)` with short TTL and strict filtering.

## Rationale

- Business cache has high hit rate and low cardinality.
- Candidate-set caching accelerates the expensive part (candidate retrieval) without risking radius correctness.
- Avoiding response caching prevents cache explosion and poor hit ratios.

## Consequences

### Positive
- Lower DB load on read replicas.
- Faster p95 latency for nearby search and business details.
- Clear invalidation scope on business changes.

### Negative
- Requires careful invalidation to avoid long-lived stale business info.
- Candidate caching must be consistent with geo-index updates (eventual consistency still applies).

## Implementation Notes

- Prefer Redis for cache store; keep cache adapter behind an interface to allow disabling in profiles.
- Use cache stampede protection if necessary (e.g., `@Cacheable` with sync or custom locks).
- Monitor cache hit rate and eviction patterns.

## Follow-ups

- Add metrics: cache hit/miss, key cardinality, top hot keys.
- Add a feature flag to enable/disable candidate-set caching separately.
