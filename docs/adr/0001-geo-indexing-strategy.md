# ADR-0001: Geo Indexing Strategy — Geohash + Neighbors

- Status: Accepted
- Date: 2026-01-09
- Deciders: Project team
- Context: Nearby search requires efficient candidate retrieval for a (lat, lon, radius) query.

## Context

We need a geo-indexing approach to support read-heavy nearby search at scale. Options considered:
1. Full table scan with distance computation (O(N)) — too slow at scale.
2. Fixed grid index — workable but tuning grid size is non-trivial across different radii.
3. Geohash prefix search — simple and widely used; supports range narrowing via prefix length.
4. Quadtree — good spatial properties but higher implementation complexity and persistence concerns.
5. S2 geometry — strong; higher complexity and library dependency.

Key constraints:
- Must support radius up to 20 km (configurable cap).
- Must handle boundary issues where nearby points fall into adjacent cells.
- Must remain simple enough for an MVP, with a clear path to evolution.

## Decision

Use **Geohash prefix indexing** with **neighbor cell expansion** and **final exact distance filtering**:
- Compute geohash of query point at precision derived from radius.
- Compute the 8 neighboring geohashes (plus the current cell).
- Fetch candidate business IDs by geohash prefixes for these cells.
- Perform exact Haversine distance computation to filter candidates within radius.
- Sort by distance and apply a result limit (e.g., 50–200, configurable).

Geo-index persistence:
- MVP: `geo_index` table with `(business_id, geohash, lat, lon)` and an index on `geohash`.
- Optional acceleration: Redis structures keyed by geohash prefix to store candidate IDs.

## Rationale

- Implementation is straightforward and well-understood.
- Prefix length provides a controllable tradeoff between recall and candidate set size.
- Neighbor expansion addresses boundary issues without complex geometry.
- Exact distance filtering ensures correctness regardless of cell approximation.

## Consequences

### Positive
- Simple and robust MVP path.
- Works with SQL (prefix search) and Redis.
- Easy to tune precision mapping table.

### Negative
- Candidate retrieval may include false positives, requiring distance filtering.
- Prefix queries can become expensive if precision is too coarse or cells too dense.
- Hotspots possible in dense urban regions; requires tuning and/or sharding at high scale.

## Follow-ups

- Add a configurable `radius -> geohash precision` mapping table.
- Add monitoring on candidate set size and query latency.
- Consider S2 or PostGIS for advanced geospatial features if needed.
