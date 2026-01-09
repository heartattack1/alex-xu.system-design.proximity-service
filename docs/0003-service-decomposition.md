# ADR-0003: Service Decomposition — Modular Monolith First, Split Later

- Status: Accepted
- Date: 2026-01-09
- Deciders: Project team
- Context: The target architecture includes LBS and Business Service. For a generated project, we need a pragmatic implementation plan.

## Context

The system logically separates:
- LBS (Nearby Search): read-heavy, stateless.
- Business Service: CRUD + write-heavy relative to LBS.
- Supporting components: geo-index, cache, outbox.

A strict microservices approach from day one increases operational complexity (deployment, observability, networking, message broker).
For an MVP and code generation, we prefer a simpler execution model while preserving boundaries.

## Decision

Start as a **modular monolith** with clear package/module boundaries:
- `adapter` (REST), `application` (use cases), `domain` (entities), `infrastructure` (persistence, cache, messaging).
- Separate logical modules: `lbs` and `business` under `application` and `domain` namespaces.
- Keep interfaces (ports) between modules to make future extraction straightforward.

When scale/ownership requires, split into two deployables:
- `lbs-service` and `business-service`, communicating via HTTP and events.

## Rationale

- Faster delivery and simpler local development.
- Preserves clean boundaries required for future microservices.
- Supports gradual introduction of outbox and async processing.
- Enables code generation of a cohesive Spring Boot project without multi-repo complexity.

## Consequences

### Positive
- Lower operational overhead for MVP.
- Clear evolution path to microservices.
- Shared domain model and utilities initially.

### Negative
- Risk of accidental coupling if boundaries not enforced.
- Needs discipline in package/module access rules.
- Some performance characteristics differ from true microservices (acceptable for MVP).

## Follow-ups

- Add ArchUnit rules to enforce module boundaries (optional).
- Add separate configuration profiles to emulate split deployment.
- Add contract tests if/when services are extracted.
