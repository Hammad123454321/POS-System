# Implementation To-Do

Updated: 2026-05-13

This list tracks remaining implementation work identified during the PRD and implementation-plan audit. It intentionally excludes test execution and test-fix tasks.

## Completed In The Latest Alignment Pass

- Added merchant-level privacy export for tenant-scoped operational records, with owner/admin authorization and archived-access logging.
- Expanded archived-record read logging from receipts to audit logs and payroll snapshots.
- Expanded retention pruning to cover audit logs, payroll snapshots, receipts, archive access logs, sync events, and device status events.
- Added back-office actions for merchant exports and archived audit/payroll reads.
- Re-verified Laravel route registration, PHP syntax, Vue type-checking, and production frontend build.

## Flutter Phase 3/4 Support

- Add POS gateway methods for workforce appointments, slot claims, appointment check-in, appointment completion, staff shift open/close, and labor analytics.
- Add POS gateway methods for delivery external-order listing, ingest/confirm/status/cancel actions, store availability, and item availability.
- Add POS gateway methods for retail inventory lookup, receiving, transfers, adjustments, returns, and promotion surfaces.
- Extend local cached bootstrap/config parsing for workforce, delivery, retail, and promotion payloads.
- Add Flutter screens or sections for appointment calendar/work queue, staff shift controls, labor summaries, delivery order handling, and retail inventory operations.
- Add offline and auth-hold behavior for Phase 3/4 actions, including clear blocking for online-only actions.
- Update app copy and navigation so the POS client no longer presents itself as a Phase 2-only surface.

## PostgreSQL RLS Expansion

- Inventory every tenant-scoped table and confirm required `merchant_id` and, where applicable, `store_id` columns.
- Add RLS policies for catalog, orders, payments, stored value, customers, restaurant, workforce, retail, delivery, reporting, audit, exception queue, and sync tables.
- Ensure application request context reliably sets the PostgreSQL tenant setting before tenant-scoped queries run.
- Define and document bypass behavior for maintenance/service roles.
- Review migrations for tenant-scoped unique indexes and foreign keys that could bypass intended tenant boundaries.

## API Versioning

- Derive the requested major version from `/api/pos/v{major}` instead of hard-coding major `1`.
- Support the current major and immediately previous major as configurable values.
- Enforce app-version compatibility rules for supported majors.
- Return `426 Upgrade Required` after sunset while leaving bootstrap/version-check routes available for the required grace window.
- Include `min_supported_major`, `min_supported_app_version`, `sunset_at`, and `upgrade_required` consistently in bootstrap/version responses.
- Document emergency security revocation behavior and the shorter revocation window.

## PAX/POSLink Production Path

- Finalize store-to-terminal mapping and terminal boarding values.
- Wire production POSLink SDK calls end-to-end for certified PAX sale flow.
- Confirm tender metadata capture: masked PAN, auth code, terminal id, application label, AID, TVR, TSI, entry mode, and transaction reference.
- Complete void/refund/inquiry/reversal mapping against the selected Fiserv/First Data acquiring path.
- Confirm in-doubt recovery behavior blocks duplicate card retries until inquiry or reversal completes.
- Produce certification artifacts and operational runbook for terminal provisioning and troubleshooting.

## Documentation Alignment

- Decide the authoritative product status: backend Phase 4 complete, Flutter Phase 2 only, or another precise scope.
- Update `README.md`, `docs/progress-status.txt`, `docs/phase4-completion-checklist.txt`, and `docs/on-hold-items.txt` so they agree.
- Split backend-complete, Flutter-complete, integration-complete, and production-ready status labels.
- Add a known-gaps section for items intentionally deferred from the PRD.

## Operational Infrastructure

- Implement reporting replica routing so back-office and Super Admin reports do not hit the write primary.
- Configure separate Redis namespaces or databases and Horizon supervisors for payments, sync, delivery, reporting, and telemetry.
- Add DLQ handling rules, oldest-job monitoring, and automatic critical incident creation for stale DLQ jobs.
- Define canary release automation with staged rollout, rollback thresholds, and health checks.
- Implement and document DR/failover flow for warm standby promotion, Route 53 cutover, worker restart ordering, and cloud outage behavior.
- Configure immutable cross-region backups and retention policies.
- Retention pruning now covers audit logs, sync events, receipts, payroll snapshots, archive access logs, and device status events.
- Add observability metrics, structured log fields, alert thresholds, and dashboards for payments, sync, stored value, delivery, retail, and queues.

## Security And Readiness

- Complete tenant isolation enforcement beyond RLS, including query scopes and cross-tenant authorization review.
- Archive access logging now covers audit logs, receipts, payroll snapshots, customer exports, and merchant exports.
- Merchant data export workflow is implemented for tenant-scoped operational records.
- Customer deletion/tombstone workflow preserves financial and audit records.
- Complete STRIDE threat model follow-through for device theft, rogue cashier, compromised back-office user, payment replay, webhook spoofing, token theft, and tenant leakage.
- Finish device revocation, auth-hold, quarantine review, and manager re-auth flows across backend and Flutter.
- Confirm local SQLite key rotation behavior for scheduled rotation, revocation, OS keystore invalidation, attestation failure, and ownership transfer.

## Admin And Back-Office UI

- Admin workspaces now exist for catalog, customers, stored value, restaurant setup, workforce, delivery, retail, reports, exceptions, feature flags, privacy, and archive access.
- Each workspace has plain-language guidance and form-backed actions for the most important API flows, including privacy export, PII tombstone, archived receipt access, delivery availability, staff setup, reports, retail promotions, and exception resolution.
- Still to finish for a full production back office: searchable data grids, edit/detail drawers, pagination, saved filters, role-aware permission hiding, and richer dashboard charts for exception queue, sync health, delivery health, payment health, and stock movement trends.
