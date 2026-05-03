# ADR 0002: Shared-Schema Tenancy With RLS

Status: accepted

## Decision

Use one PostgreSQL database with a shared schema, `merchant_id` on every tenant-scoped record, application-layer scoping on every query path, and PostgreSQL Row-Level Security as defense in depth.

## Rationale

- The approved plan explicitly chooses shared-schema tenancy.
- This model keeps cross-store and cross-module workflows transactional.
- RLS reduces the blast radius of a missed application filter.

## Consequences

- Tenant-scoped tables enable and force RLS in PostgreSQL environments.
- POS requests set the active merchant context on the database session before business queries run.
- Super-admin and maintenance access must use privileged roles that intentionally bypass RLS.

