# PRD Alignment Audit

Updated: 2026-05-13

This note compares the current repository against `POS-PRD (1).pdf` and `implementaion-plan.md`.

## Aligned In The Repo

- Multi-tenant platform foundation is in place: merchants, stores, devices, tenant-scoped models, request context, and PostgreSQL RLS catch-up coverage.
- POS API versioning now uses `/api/pos/v{major}` and enforces supported major/app-version rules.
- Phase 1 and 2 POS scope is covered: catalog, pricing, tax, customers, orders, register sessions, split tenders, refunds, voids, gift cards, memberships, audit logs, sync recovery, and receipt handling.
- Restaurant setup and operations are covered through tables, table leases, print routing, business-day reporting, and POS table actions.
- Salon/workforce scope is covered through staff profiles, service items, appointment lifecycle, slot claims, shifts, attendance, wage/commission rules, payroll snapshots, and labor analytics.
- Delivery scope is covered through channel configs, menu publish hooks, external order ingest, status updates, cancellation, and store/item availability controls.
- Retail scope is covered through barcode/SKU records, inventory lookup, receive, transfer, adjust, return, promotions, and stock movement reporting.
- Flutter has Phase 3/4 operator surfaces wired to the gateway for appointments, shifts, labor, delivery, and retail inventory operations.
- Back-office admin workspaces exist for the main API-backed operations: catalog, customers, stored value, restaurant, workforce, delivery, retail, reports, exceptions, feature flags, privacy, and archive reads.
- Privacy and readiness controls now include customer exports, customer PII tombstones, merchant exports, archive access logs, retention pruning, DLQ incident monitoring, reporting-replica query routing, canary script, and DR checklist.

## Still External Or Operational

- PAX/POSLink production certification cannot be completed inside the repo alone. It still needs terminal boarding values, live acquirer credentials, certification scripts/results, and estate mapping from the processor/merchant environment.
- Production infrastructure is represented by configuration and scripts, but the real AWS/Redis/Horizon/backup/observability rollout must be applied and verified in the target environment.
- Local PostgreSQL verification is still blocked until this machine's active PHP runtime has `pdo_pgsql` enabled.

## Product Polish Still Worth Doing

- The admin area is functional, but a full client-ready back office should still add searchable grids, detail/edit drawers, pagination, saved filters, and role-aware hiding of actions.
- More Playwright flows should be added for real operator journeys once stable seed data and production-like auth fixtures are available.
