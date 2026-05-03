# Final Implementation Plan for the Multi-Tenant POS Platform

## Summary
Build the product as a Laravel modular monolith for cloud control, shared business state, reporting, and integrations, with a separate Flutter POS app for store-device workflows, offline operation, and hardware control. The system is optimized around financial correctness, tenant isolation, multi-device concurrency, offline resilience, and controlled rollout of restaurant-first features without exceeding the PRD.

## Architecture Overview
Stack and topology:
- Cloud and web: `Laravel 12`, `PHP 8.4`, `PostgreSQL 16`, `Redis`, `Horizon`, `Inertia + Vue 3`
- Store devices: `Flutter` + local `SQLite` via `Drift`
- Regions: `AWS us-east-1` primary, `AWS us-west-2` warm standby, `Route 53` failover, immutable cross-region backups
- Reporting isolation: one dedicated PostgreSQL read replica for back office and Super Admin reporting; reporting queries never hit the write primary
- Queue isolation: separate Horizon supervisors and Redis namespaces/DBs for `payments`, `sync`, `delivery`, `reporting`, and `telemetry`

```text
[Super Admin Web] [Merchant Back Office Web]
                \ /
        [Laravel Modular Monolith]
 Tenancy | RBAC | Catalog | Orders | Payments
 Customers | Gift Cards | Memberships | Restaurant
 Salon/Workforce | Retail | Reporting | Audit
 Delivery Connectors | Billing Metering | Exception Queue | Sync API
                    |
     [Redis: payments | sync | delivery | reporting | telemetry]
                    |
 [PostgreSQL Primary] -> [Warm Standby] -> [Reporting Read Replica]

             HTTPS / Sync APIs
                    |
         [Flutter POS App per Device]
 UI + Local SQLite + Sync Queue + Hardware Adapters
 Printers | Drawer | Scanner | PAX Terminal
```

Core boundaries:
- Laravel owns all merchant-shared state, tenant isolation, sync authority, audit, reporting, stored value, and third-party integrations.
- Flutter owns checkout-time UX, local drafts, offline queue, printing, scanner, drawer, and terminal I/O.
- Cross-store balance-changing actions remain cloud-authoritative and online-only.

## Governance and Enforcement
Module-boundary enforcement:
- Each module exposes only `Contracts/` and `Application/` entry points; direct imports across modules outside those surfaces are forbidden.
- CI runs architecture tests with `Deptrac` or `Pest arch` rules to block cross-module imports, cross-module Eloquent model access, and direct cross-module DB joins.
- Shared code is limited to a `Platform` kernel, auth primitives, money/time utilities, and event contracts.

Required ADRs before implementation:
- Laravel modular monolith vs services
- PostgreSQL vs MySQL
- Flutter POS client choice
- Shared-schema tenancy with RLS
- Money representation and rounding rules
- Primary region and failover policy

Non-goals:
- No hidden feature parity with reference products
- No payroll filing, tax withholding, ACH, or compliance automation
- No offline card payments
- No offline gift-card or membership mutations
- No KDS scope beyond printer-oriented restaurant workflows in the PRD
- No data import adapters from other POS vendors in this PRD

## Tenant, Money, Time, and Idempotency Invariants
Tenancy model:
- Single PostgreSQL database and shared schema with `merchant_id` on every tenant-scoped row.
- Application scoping is mandatory on every query path.
- PostgreSQL Row-Level Security is enabled on tenant tables as defense in depth; only privileged maintenance/service roles may bypass it.

Money model:
- All money is stored and transmitted as integer minor units plus ISO currency code.
- Laravel uses a dedicated money value object such as `Brick\Money`; floats are forbidden.
- Conflict and reconciliation thresholds use `1 minor unit`, not decimal string comparison.
- Rounding rules are fixed per operation: tax rounds at line or jurisdiction rule, tips round at tender, split tenders prorate by minor units with remainder assigned deterministically to the last split, discounts prorate by line using minor units.

Time and business-day model:
- Every store has `timezone` and `business_day_cutoff`.
- Orders, register sessions, cashier reports, daily reports, payroll snapshots, and attendance records persist a derived `business_date`.
- UTC timestamps remain the canonical event time; business reporting uses `business_date`.
- Shift and appointment calculations use timezone-aware timestamps and DST-safe arithmetic.

Idempotency:
- All mutating POS HTTP endpoints require an `Idempotency-Key`.
- Server stores the original request hash and response for `72 hours`.
- Replay with the same key and same payload returns the cached original response.
- Replay with the same key and different payload returns `422 Idempotency Conflict`.
- Sync endpoints also keep the existing unique `(device_id, local_event_id)` guarantee.

## Core Modules
- Platform Core: merchants, stores, devices, store mode, store settings, Super Admin controls, billing metering, and feature flags.
- Identity & RBAC: users, store-scoped roles, action permissions, break-glass access, and archived-record access controls.
- Catalog & Pricing: products, services, categories, variants, modifiers, add-ons, combos/packages, tax rules, discounts, promotions, membership pricing.
- Customer & Stored Value: customer profiles, member profiles, gift cards, memberships, cross-store ledgers, order history, notes, and spend holds.
- Order & Register Core: carts, orders, receipts, cash count, payouts, register open/close, cashier reporting, and daily summaries.
- Payments: cash, card, split tenders, tips, refunds, voids, in-doubt transaction recovery, reconciliation, and PAX semi-integration.
- Offline Sync: bootstrap, config pull, delta pull, queued event push, reconnect recovery, and conflict handling.
- Restaurant: dine-in, pickup, delivery, walk-in, table layout/status, merge/split/transfer, sold-out toggle, print routing, kitchen/label printing, and online-order operational controls.
- Salon & Workforce: service catalog, appointments, walk-ins, staff-service assignment, commissions, shifts, attendance, wage setup, payroll calculations, and labor analytics.
- Retail: SKU, barcode, variants, stock balances, receiving, transfers, adjustments, returns, promotions, and inventory reporting.
- Reporting & Audit: unified reporting, materialized summaries, immutable audit trails, and exception dashboards.
- Delivery Integrations: canonical menu model, channel adapter layer, aggregator-first rollout, then Uber Eats and DoorDash direct adapters.
- Exception Queue: manual review workflow for sync/payment/inventory/appointment/register conflicts with SLA, ownership, and audit trail.

## Public Interfaces
- `PaymentProvider`: `createIntent`, `authorize`, `capture`, `void`, `refund`, `reconcile`, `inquire`, `reverse`, `getTerminalCapabilities`
- `TerminalAdapter`: `connect`, `startPayment`, `cancelPayment`, `printReceipt`, `printLabel`, `openDrawer`, `scanBarcode`
- `StoredValueLedger`: `issue`, `topUp`, `redeem`, `reverse`, `balance`, `createHold`, `captureHold`, `releaseHold`
- `OnlineOrderingChannelAdapter`: `publishMenu`, `setStoreAvailability`, `setItemAvailability`, `receiveOrder`, `confirmOrder`, `updateStatus`, `cancelOrder`
- `PayrollCalculator`: `snapshotApprovedHours`, `calculateGross`, `calculateCommission`, `generatePayrollReport`
- Sync APIs: bootstrap, config pull, batched event push, delta pull by cursor, reconciliation status
- Admin APIs: merchant/store/device setup, catalog/pricing/tax, staff, reports, print routing, delivery channel config
- POS APIs: orders, payments, refunds, tables, appointments, inventory actions, stored value, and register actions

## Data Model
Core entities:
- Org/auth: `merchant`, `store`, `device`, `device_profile`, `store_mode`, `user`, `role`, `permission`, `role_permission`, `store_setting`
- Catalog: `category`, `catalog_item`, `service_item`, `variant`, `modifier_group`, `modifier_option`, `combo_package`, `price_rule`, `tax_rule`, `discount_rule`, `promotion`
- Customer value: `customer`, `member_account`, `membership_plan`, `gift_card`, `gift_card_ledger_entry`, `membership_ledger_entry`, `spend_hold`
- Orders/register: `order`, `order_line`, `receipt`, `payment`, `payment_split`, `refund`, `void_record`, `register_session`, `cash_movement`
- Restaurant: `floor_plan`, `dining_table`, `table_assignment`, `printer_config`, `printer_health`, `print_route`, `online_order_setting`
- Workforce: `staff_profile`, `staff_service_rule`, `appointment`, `shift`, `attendance_record`, `commission_rule`, `wage_rule`, `payroll_snapshot`
- Retail: `inventory_balance`, `inventory_adjustment`, `inventory_transfer`, `receiving_record`, `barcode_record`
- Ops/config: `sync_event`, `audit_log`, `archive_access_log`, `exception_case`, `exception_resolution`, `device_status_event`, `usage_record`, `delivery_channel_config`, `external_order_link`, `feature_flag`, `feature_flag_override`, `edit_lease`, `outbox_job`, `idempotency_record`

Indexes and constraints:
- Composite indexes on `(merchant_id, store_id, created_at)` for orders, payments, appointments, inventory movements, audit logs, and sync events
- Unique `(device_id, local_event_id)` for sync idempotency
- Unique merchant-level gift card code and membership identifier
- Unique store-level `sku` and `barcode`
- Time indexes on `(store_id, start_time)` and `(staff_id, start_time)` for shifts and appointments
- PostgreSQL exclusion constraints on confirmed appointments to prevent overlapping staff bookings where store rules disallow overlap

## Conflict Resolution and Exception Handling
Conflict matrix:
| Entity | Authority | Winning Rule | Manual Review Trigger |
|---|---|---|---|
| Orders | `edit_lease_id`, accepted payment events, server `status_seq` | Lease holder wins structural edits; first server-accepted tender wins money; first valid final transition wins `paid` or `voided` | Non-lease edit, paid order mutated, tenders exceed due, total mismatch `> 1 minor unit` |
| Inventory | `inventory_ledger_seq`, `count_session_id`, `count_closed_at` | Ledger movements always append; absolute overwrite allowed only through locked count session; first closed count wins | Negative stock, duplicate transfer/receiving doc, conflicting counts, variance `> max(5 units, 5%)` |
| Appointments | `slot_claim_id`, `staff_id`, `start_at`, `end_at`, server `status_seq` | Confirmed staff-backed appointments require online slot claim; offline entries remain local drafts only and never become authoritative | Deposit attached to rejected slot, checked-in customer on stale slot, required staff double-booked |
| Register Sessions | `drawer_id`, `session_version`, `session_owner_device_id`, `cash_total_seq` | First committed open wins drawer ownership; only owner closes unless manager override transfers ownership online | Concurrent open, non-owner close, close variance `> max($20, 1%)`, offline cash movement after close |

Exception Queue workflow:
- Every manual-review trigger opens an `exception_case` with severity, module, tenant, store, record references, and suggested action.
- Resolver roles are `Store Manager` for store-local issues, `Merchant Owner` for financial/store-value issues, and `Super Admin Support` for platform-level incidents.
- Resolution SLA is `24 hours` for store operations and `4 hours` for payment/stored-value exceptions.
- Every resolution is audit-logged with actor, action, before/after state, and reason.

## Multi-Device Concurrency and Edit Leases
Cloud-authoritative shared state:
- Table assignments/status, order headers, payments, receipts, gift-card balances, membership mutations, register sessions, inventory ledger, appointments, permissions, device status, and store configuration

Device-local state:
- Unsaved cart drafts, UI state, printer spool, hardware session state, cached catalog/pricing/tax/permissions, local sync queue, and transient notes/search buffers

Race prevention:
- Table assignments use cloud leases with `30s` TTL and `10s` heartbeat
- Gift-card spend holds are created only at tender start, not order start; the hold TTL is `120s` and renews every `60s` while the tender screen is active
- Register sessions are one-open-session-per-drawer and use compare-and-swap on `session_version`
- Cloud totals are recomputed from accepted ledger events; devices never push authoritative totals

Lease crash recovery:
- `order_edit` TTL `45s`, heartbeat `15s`
- `table_assignment` TTL `30s`, heartbeat `10s`
- `slot_claim` TTL `60s`, heartbeat `20s`
- `drawer_session_control` TTL `90s`, heartbeat `30s`
- `count_session` TTL `120s`, heartbeat `30s`
- Reaper runs every `10s` and expires a lease when `last_heartbeat_at + ttl < now()` or the owning device is disabled or revoked
- On expiry the server clears lease ownership, increments `lease_version`, stamps `lease_expired_at`, and leaves business data unchanged
- Any write or heartbeat after expiry returns `409 LEASE_EXPIRED` with `lease_version`, `lease_expired_at`, and `current_holder_device_id`
- Flutter shows a blocking modal/banner, preserves partial drafts for orders, appointments, and count worksheets, and discards lease-only actions such as table claims and drawer closes
- Reacquisition uses full-jitter retry at `1s`, `2s`, `4s`, `8s`, then every `10s` up to `45s`; after that the user must retry explicitly

## Payments, Stored Value, Printing, Delivery, and Payroll Policies
Payments:
- Launch with PAX semi-integration behind the `PaymentProvider` boundary and one certified acquiring path first
- `void` is allowed only before capture, `refund` only after capture, and offline card payments are disabled
- In-doubt card transactions require provider `inquire` or `reverse` before any retry; the POS blocks duplicate retry until the inquiry flow completes and logs a manager-reviewed recovery action
- Restaurant tip-adjust is enabled only if the first certified processor path supports it; otherwise card tips are collected before authorization
- Stored receipt metadata includes masked PAN, auth code, terminal id, application label, AID, TVR, TSI, entry mode, and transaction reference

Stored value:
- Gift-card issue, top-up, redemption, and membership mutation are online-only and ledger-backed
- Spend holds are PostgreSQL-authoritative; Redis may accelerate lookups but is not the system of record
- Membership pricing may be cached offline, but membership renewals, balance changes, and entitlement mutations are blocked offline

Printing:
- Every print route supports primary and secondary printers
- Flutter persists the local print spool for `7 days` and retries automatically on printer recovery
- Reprints require permission and are audit-logged with actor, reason, device, and timestamp
- Printer health is reported to cloud telemetry every `30s` while online

Delivery:
- The platform owns a canonical menu model and provider adapters transform it to channel-specific schemas
- Menu sync, order ingestion, status updates, sold-out state, hours, pause windows, and prep-time settings are all driven from the canonical model
- Provider payout reconciliation is explicitly out of scope for this PRD

Payroll:
- Payroll remains calculations and reports only
- Scope includes approved hours, configured wage rules, configured commissions, and payroll-ready summaries
- Statutory overtime, meal/rest premiums, tip-credit rules, filing, and disbursement are out of scope and must be handled by an external payroll/compliance system

## API Versioning, Device Auth, Encryption, and Enrollment
Versioning:
- URI major versioning: `/api/pos/v{major}/...`
- Required headers: `X-POS-App-Version`, `X-Device-Protocol-Version`, `X-Platform`
- Server supports the current major and immediately previous major; writes are allowed for clients up to two app minor versions behind within a supported major
- Bootstrap returns `min_supported_major`, `min_supported_app_version`, `sunset_at`, and `upgrade_required`
- Deprecation notice is `90 days`; emergency security revocation may shorten to `14 days`
- After sunset, write endpoints return `426 Upgrade Required`; bootstrap/version-check remain available for `14 days`

Device auth and enrollment:
- Store Admin or Merchant Owner creates a single-use enrollment QR/code valid `15 minutes`, bound to one `store_id` and one `device_profile`
- Flutter generates a hardware-backed keypair and exchanges enrollment code, public key, device fingerprint, and platform attestation for `device_id`, access token, and rotating refresh token
- Access token TTL is `15 minutes`; refresh token TTL is `30 days` absolute and rotates on every refresh
- Silent refresh runs when less than `5 minutes` remain, on app resume, and before sync
- Refresh-token reuse detection revokes the entire device token family and opens a security exception case
- Forced re-auth occurs on device disable, store transfer, merchant suspension, refresh-token reuse, key mismatch, unsupported app version after sunset, or `30 days` without online check-in
- If revoked while offline, queued events move to `auth_hold`, remain encrypted, and are not discarded; on reconnect the device locks, requires manager re-auth, and only uploads held events after approval and quarantine review

Flutter local encryption:
- The local SQLite encryption key rotates every `90 days`
- Unscheduled rotation is triggered by device auth revocation/re-enrollment, OS keystore invalidation, secure-attestation failure, or device ownership transfer
- Rotation uses an envelope-encrypted data key and SQLCipher in-place rekey with sync paused and local writes blocked
- If rotation is interrupted, the old key remains authoritative until rekey commit succeeds
- After successful rotation queued offline events remain on disk and are transparently re-encrypted before sync resumes

## Redis, Rate Limits, Feature Flags, and Deployment
Redis failure mode:
- Queue workers and Horizon `block`: workers pause intake, synchronous request handling continues, and critical async side effects are persisted in PostgreSQL `outbox_job` rows until Redis returns
- Session cache `degrade`: web sessions fall back to database-backed sessions
- Hot POS data cache `degrade`: Laravel falls back to PostgreSQL reads, while Flutter continues using its device-local cache
- Spend holds `fail closed`: gift-card redemption and top-up are blocked until the PostgreSQL-backed hold path succeeds and Redis acceleration returns

Rate limiting:
- POS APIs: `120 requests/minute` per device and `3000 requests/minute` per merchant
- Sync APIs: separate bucket with `30 requests/minute` per device and `600 requests/minute` per merchant
- Sync retries use the sync bucket and do not consume POS API quota
- Limit breaches return `429 Too Many Requests` with `Retry-After`, `X-RateLimit-Limit`, `X-RateLimit-Remaining`, and `X-RateLimit-Reset`

Feature flags:
- Flags are merchant-scoped by default with optional store-level overrides; production per-device flags are not used
- Flags are stored in PostgreSQL `feature_flag` and `feature_flag_override`
- Flutter receives the merged effective flag set in bootstrap payloads and refreshes them on config pull
- Super Admin can toggle all flags; Merchant Owner can toggle only allowlisted self-service rollout flags

Deployment:
- Laravel deploys use canary releases
- Canary split is `5% for 10 minutes`, then `25% for 15 minutes`, then `100%`
- Rollback is automatic if canary `5xx` rate exceeds `1%` for `5 consecutive minutes`
- Flutter devices on the previous supported API major continue to work during the canary window because both supported majors remain writable

## Observability, Retention, Privacy, and DR
Observability:
- Every module emits request count, p50/p95/p99 latency, 4xx/5xx rate, queue depth, oldest queue age, retry count, and DLQ count
- Sync emits device sync lag, push success, pull success, conflict rate, and `auth_hold` queue count
- Payments emit auth/capture/refund success, terminal timeout rate, in-doubt count, and reconciliation mismatch count
- Stored value emits ledger append latency, spend-hold success, insufficient-funds rejects, double-spend rejects, and ledger imbalance count
- Mandatory log fields: `timestamp`, `level`, `service`, `env`, `trace_id`, `span_id`, `request_id`, `tenant_id`, `store_id`, `device_id`, `user_id`, `module`, `route`, `api_version`, `app_version`, `register_session_id`, `outcome`, `error_code`, `latency_ms`
- On-call pages if payment success drops below `97%` for `5 minutes` with at least `50` attempts, terminal timeouts exceed `5%` for `10 minutes`, sync failures exceed `10%` for `10 minutes` across `100` devices, oldest sync lag exceeds `15 minutes` across `50` devices, sync DLQ exceeds `50`, or any stored-value ledger imbalance appears
- Any job sitting in DLQ for more than `15 minutes` automatically opens a critical incident ticket

Retention and archived access:
- Audit logs: `7 years`, no soft delete, hard delete only by retention, accessible to Super Admin, Merchant Owner, Auditor
- Sync events: `180 days`, no soft delete, hard delete only by retention
- Receipts: `7 years`, no soft delete, hard delete only by retention
- Payroll snapshots: `7 years`, no soft delete, hard delete only by retention
- Device status events: `365 days`, no soft delete, hard delete only by retention
- Access to archived audit logs, receipts, and payroll snapshots is itself audit-logged in PostgreSQL `archive_access_log`, replicated to immutable object storage, and retained for `7 years`

Privacy and export:
- Merchants can request a full operational export of their own tenant data through a back-office export workflow
- Customer deletion requests tombstone PII while preserving financial and audit records required by retention rules
- Deletion/export actions are fully audit-logged

DR and failover:
- PostgreSQL targets are `RPO 5 minutes` and `RTO 60 minutes`
- Deployment topology is `us-east-1` primary with `us-west-2` warm standby using a continuously streaming PostgreSQL replica and a pre-provisioned Laravel stack ready to promote
- Cross-region immutable backups remain in a separate backup account with nightly full snapshots and continuous WAL archiving
- Failover is triggered by both automation and manual ops approval after primary health checks, DB write probes, and app reachability all fail for `120 seconds`
- DNS cutover uses `Route 53` failover alias records with a `30-second` TTL and converges in roughly `60–120 seconds`
- During cutover the old primary serves `503 Retry-After: 30` if still reachable, the standby remains read-only until promotion, and workers resume in this order: payments, stored value, sync, reporting
- Flutter retries bootstrap and sync with full-jitter backoff from `2s` to `30s` for `5 minutes` and then stays in offline mode with the queue held on disk
- During full cloud outage Flutter enters `Cloud Outage Mode`, continues local order capture, kitchen/receipt printing, and cash sales on already-open register sessions, and blocks card payments, stored-value mutations, new drawer open/close, inventory counts/transfers, delivery actions, and appointment confirmations
- The approved `60-minute` RTO remains the target; DR drills must measure both technical recovery time and manual-approval latency separately

## Reporting and Read Model Strategy
- Reporting and Super Admin dashboards read from the reporting replica or materialized summaries only
- Materialized reporting views refresh concurrently every `5 minutes`
- Reporting staleness SLO is `15 minutes`
- High-volume reports are pre-aggregated by `merchant_id`, `store_id`, and `business_date`

## Implementation Phases
- Phase 1a inside Phase 1: tenancy, stores, devices, RBAC, Flutter POS shell, sync primitives, catalog/pricing/tax, order/receipt core, cash-only register operations, and receipt printing
- Phase 1b inside Phase 1: multi-device correctness, customer/member basics, discounts, restaurant table basics, print routing, exception queue, and business-day reporting
- Phase 2: PAX payment boundary and first acquiring path, cash/card/split/tips, refund/void controls, gift cards, memberships, cross-store online validation, label printing, audit logs, advanced sync recovery, and billing metering
- Phase 3: salon services, appointments, staff-service assignment, commissions, staff profiles, shifts, attendance, wage setup, payroll calculations/reports, and labor analytics
- Phase 4: delivery adapter framework, aggregator connector first, Uber Eats and DoorDash direct adapters on the same contract, canonical menu sync, order ingestion, status updates, device monitoring, exception handling, and full retail module

## Capacity, Testing, and Security Readiness
Initial engineering sizing assumptions:
- Up to `10` POS devices per store
- Up to `200` open restaurant orders/tabs per store at peak
- Up to `50` payment attempts per minute per busy store
- Up to `10,000` sync events per device per day
- Up to `1,000` active stores per region before the next capacity review

Required tests:
- Tenant isolation and RLS enforcement tests
- Property-based tests for money math, discount proration, tax, tip, and split-tender invariants
- Offline order capture, reconnect sync, duplicate replay prevention, conflict recovery, and lease expiry recovery
- Payment sandbox contract tests in CI for auth, capture, refund, void, inquiry, and reversal
- Chaos and fault-injection tests for sync partitions, retries, duplicate delivery, reordered events, and clock skew
- Load tests for Friday-evening restaurant peak and multi-store sync fan-in
- Long-running soak tests for leases, token rotation, DLQ behavior, and printer spool recovery
- Zero-downtime migration rehearsals on hot tables and canary rollback drills
- Multi-region failover drills and reporting replica failover validation

Security readiness:
- Complete STRIDE threat model before Phase 1 starts, covering device theft, rogue cashier, compromised back office user, payment replay, webhook spoofing, token theft, and tenant-data leakage
- Performance budgets before Phase 1 starts for app cold start, add-to-cart, tender, and print flows
