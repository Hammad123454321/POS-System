# ADR 0001: Laravel Modular Monolith

Status: accepted

## Decision

Use a Laravel 12 modular monolith for cloud-side product logic, with feature modules exposing only `Contracts/` and `Application/` entry points.

## Rationale

- The PRD requires shared financial correctness, tenant isolation, reporting, and multi-device coordination.
- These concerns benefit from one transactional boundary over PostgreSQL in the early phases.
- The approved architecture already separates device responsibilities into the Flutter POS client.

## Consequences

- Cross-module access is enforced by architecture tests.
- Modules may share only the platform kernel, event contracts, and money/time primitives.
- Queue isolation and integration boundaries remain internal implementation seams until a service split is justified by load or organizational need.

