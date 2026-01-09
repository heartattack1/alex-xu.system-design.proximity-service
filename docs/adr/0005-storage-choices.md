# ADR-0005: Storage Choices — PostgreSQL + Optional Redis, PostGIS as Future Option

- Status: Accepted
- Date: 2026-01-09
- Deciders: Project team
- Context: The system needs a reliable source of truth and an efficient geo-index for prefix queries.

## Context

We need storage for:
- Business data (OLTP, transactional)
- Geo index (prefix search on geohash, candidate retrieval)
- Cache (business details, optional candidate sets)
- Event propagation (optional: outbox + broker)

Options considered:
1. PostgreSQL (plain) with `geo_index` table and btree index on `geohash`
2. PostgreSQL with PostGIS and native spatial indexes
3. MySQL with similar schema (prefix search)
4. Redis GEO for proximity search (fast, but diverges from geohash-prefix design)
5. Elasticsearch / OpenSearch geo queries (overkill for MVP)

## Decision

### MVP / Baseline
- Use **PostgreSQL** as the primary database.
- Persist geo-index as a relational table `geo_index(business_id, geohash, lat, lon)` with an index on `geohash`.
- Use **Redis** as an optional cache store (business details and candidate sets).

### Future option (if requirements expand)
- Consider **PostGIS** for:
  - complex polygons, geofencing, advanced geo queries
  - improved spatial query performance beyond prefix matching

We explicitly avoid Elasticsearch for MVP due to operational complexity and because the query patterns are simple.

## Rationale

- PostgreSQL provides reliable transactions and mature tooling (migrations, Testcontainers).
- Prefix queries on `geohash` are easy to implement and test.
- Redis is a standard fit for caching and can be introduced gradually.
- PostGIS is a controlled evolution path if geospatial needs grow.

## Consequences

### Positive
- Simple local environment with Docker Compose.
- Deterministic behavior suitable for code generation.
- Clear upgrade path to PostGIS if necessary.

### Negative
- Prefix search may be less efficient than true spatial indexes at very large scale.
- Requires careful tuning of geohash precision mapping to control candidate set sizes.

## Implementation Notes

- Create an index on `geo_index(geohash)`; evaluate prefix query patterns.
- Keep storage choices behind configuration profiles (`local`, `test`, `prod`).
- Implement repositories to allow future swap to PostGIS without changing use cases.

## Follow-ups

- Add load tests to validate candidate set sizes in dense regions.
- If adopting PostGIS, add ADR for migration strategy and query changes.
