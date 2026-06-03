# Production Operations Runbook

Updated: 2026-05-06

This runbook records the production controls required by the implementation plan. Items here are implemented as repository configuration where possible and must be bound to real cloud resources during deployment.

## Reporting Isolation

- Configure a dedicated read-only PostgreSQL reporting connection with `REPORTING_DB_*` environment variables.
- Route back-office and Super Admin reporting queries through the reporting connection or materialized summaries only.
- Keep write-path POS APIs on the primary database connection.
- Alert if reporting lag exceeds `15 minutes`.

## Queue Isolation

- Use separate Redis databases/namespaces for `payments`, `sync`, `delivery`, `reporting`, and `telemetry`.
- Run the dedicated Horizon supervisors already declared in `config/horizon.php`.
- Keep payment workers highest priority during failover recovery, followed by stored value, sync, and reporting.
- Treat queue connection failure as degraded mode and persist critical async side effects to PostgreSQL outbox rows.

## DLQ Handling

- Failed jobs are persisted through Laravel failed-job storage.
- Any DLQ job older than `15 minutes` must create or update a critical incident ticket.
- Payment and stored-value DLQ items require manager-visible exception cases before any retry that could duplicate financial effects.

## Canary Rollout

- Stage production deploys at `5% for 10 minutes`, `25% for 15 minutes`, then `100%`.
- Roll back automatically if canary `5xx` rate exceeds `1%` for `5 consecutive minutes`.
- Confirm both current and previous supported POS API majors stay writable during rollout.

## Disaster Recovery

- Primary region: `us-east-1`.
- Warm standby region: `us-west-2`.
- PostgreSQL targets: `RPO 5 minutes`, `RTO 60 minutes`.
- Promote standby only after primary health checks, DB write probes, and app reachability fail for `120 seconds` and manual ops approval is recorded.
- Cut DNS through Route 53 failover alias records with `30-second` TTL.
- Resume workers in this order: payments, stored value, sync, reporting.

## Backups And Retention

- Use nightly immutable full snapshots and continuous WAL archiving in a separate backup account.
- Retain audit logs, receipts, payroll snapshots, and archive access logs for `7 years`.
- Retain sync events for `180 days`.
- Retain device status events for `365 days`.

## Observability Alerts

- Page when payment success drops below `97%` for `5 minutes` with at least `50` attempts.
- Page when terminal timeouts exceed `5%` for `10 minutes`.
- Page when sync failures exceed `10%` for `10 minutes` across at least `100` devices.
- Page when oldest sync lag exceeds `15 minutes` across at least `50` devices.
- Page when sync DLQ exceeds `50`.
- Page immediately on any stored-value ledger imbalance.
