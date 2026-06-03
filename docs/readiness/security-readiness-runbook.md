# Security Readiness Runbook

Updated: 2026-05-06

This runbook captures production security controls that require both code paths and deployment operations.

## Tenant Isolation

- Every tenant-scoped table must carry `merchant_id`.
- PostgreSQL RLS is enforced for all tables with `merchant_id` through the catch-up RLS migration.
- POS request middleware must set `app.current_merchant_id`, `app.current_store_id`, and `app.current_device_id` before protected queries run.
- Application queries must still apply explicit merchant and store filters; RLS is defense in depth, not the primary authorization layer.

## Archive Access

- Reads of archived audit logs, receipts, payroll snapshots, and export artifacts must write an `archive_access_log` record.
- Archive access logs must be replicated to immutable object storage and retained for `7 years`.
- Archived financial records are never soft-deleted.

## Privacy Workflows

- Merchant operational export must produce tenant-scoped data only.
- Customer deletion must tombstone PII while preserving financial, receipt, and audit records required by retention policy.
- Export and deletion workflows must be fully audit-logged with actor, tenant, timestamp, scope, and reason.

## Device Revocation And Quarantine

- Device disable, merchant suspension, refresh-token reuse, key mismatch, unsupported app version after sunset, and stale online check-in must force re-auth.
- Flutter keeps queued events encrypted and moves them to auth-hold/quarantine instead of discarding them.
- Manager approval is required before quarantined events return to the pending sync queue.
- Local encryption key rotation is required after revocation, re-enrollment, OS keystore invalidation, secure-attestation failure, or ownership transfer.

## Threat Model Follow-Through

- Device theft: force revocation, auth-hold, local key rotation, and server-side token-family invalidation.
- Rogue cashier: enforce role-gated refund, void, reprint, close variance, and exception-resolution actions.
- Compromised back-office user: require least-privilege roles, audit every privileged action, and support break-glass review.
- Payment replay: require idempotency keys, provider transaction inquiry, and no duplicate card retry during in-doubt recovery.
- Webhook spoofing: verify provider signatures and deduplicate provider event references.
- Token theft: rotate refresh tokens on every refresh and revoke token family on reuse.
- Tenant leakage: combine explicit query scoping, RLS, architecture tests, and audit review.
