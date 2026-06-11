# POS Platform — Gap-Closure Implementation Plan (all 22 gaps + extras)

## Context

A gap analysis against the PRD/implementation plan found 22 gaps: backend domain logic is ~85-90% complete, but the back office lacks platform operations (super admin, onboarding, RBAC, orders browsing), the Flutter app is an "operations console" rather than a real cashier UI, delivery/payment integrations stop at the transport boundary, and several correctness systems (order leases, sync arbitration, metering, reporting summaries) are unwired. Public registration is open on production — a security hole. This plan closes all 22 gaps plus 4 small competitor-informed extras, in **two parallel tracks** (Track A: backend/back-office; Track B: Flutter cashier UI), production-grade, following the codebase's existing conventions.

**Execution model:** Tracks A and B are independent and can be worked simultaneously. Within each track, order is mandatory (dependencies noted). Total ≈ 60 dev-days (A: ~38, B: ~22).

## Gap → work-item map (all 22 accounted for)

| # | Gap | Work item | Track |
|---|---|---|---|
| 1 | Super Admin portal | A-P1.1 | A |
| 2 | Merchant/store/device onboarding | A-P1.2 | A |
| 3 | RBAC management + user invites | A-P0.2 + A-P1.3 | A |
| 4 | Orders browser | A-P2.2 | A |
| 5 | Admin CRUD (catalog items/categories) | A-P2.3 | A |
| 6 | Real cashier checkout UI | B-4, B-5 | B |
| 7 | Visual table floor plan | B-6 | B |
| 8 | Appointment calendar | B-7 | B |
| 9 | Hardware adapters (printer/drawer/scanner) | B-8 | B |
| 10 | Live card payments | EXT-1 (client-owned checklist + eng support) | — |
| 11 | Order edit leases | A-P3.1 | A |
| 12 | Inbound delivery webhooks | A-P3.2 | A |
| 13 | Outbound delivery HTTP clients | A-P3.3 | A |
| 14 | Reporting summary tables | A-P4.2 | A |
| 15 | Billing metering events | A-P4.1 | A |
| 16 | Email receipts | A-P3.4 | A |
| 17 | Sync conflict arbitration | A-P4.3 | A |
| 18 | Web admin authorization | A-P0.2 | A |
| 19 | Tests green + CI gating | A-P0.1 | A |
| 20 | Production hardening (right-sized DR/observability) | EXT-2 | — |
| 21 | Lock down public registration | A-P0.3 | A |
| 22 | Device revocation / auth-hold UX | B-9 | B |
| + | Extras: tip prompt, receipt options, no-sale drawer, email-receipt UI | B-5/B-8 + A-P3.4 | both |

## Ground rules (both tracks)

- Laravel root: `apps/platform`. Flutter root: `apps/pos`. Modules: `app/Modules/{X}/{Contracts,Application,Domain,Interfaces}`.
- IDs are ULIDs everywhere except `users.id` (bigint). Money = integer minor units (`Brick`-style on backend, `MinorAmount` util in Flutter at `lib/src/core/support/minor_amount.dart`).
- **Every new merchant-scoped table** gets `merchant_id` + inline RLS policy block — copy the `applyMerchantPolicies` pattern from migration `2026_04_27_120000_create_phase_4_delivery_and_retail_tables.php`. Tables with legitimately-null `merchant_id` (`user_invitations`, `delivery_provider_events`) need bespoke `USING (merchant_id IS NULL OR merchant_id = current_setting('app.current_merchant_id', true))` policies.
- Backend style: action classes (constructor-injected), FormRequests, Pest feature tests using helpers in `tests/Pest.php` (`posHeaders()`, `buildPosOrderContext()`). Run `vendor/bin/pint` before every commit (CI gates on it).
- Flutter style: `ChangeNotifier` + constructor injection (NO new state-management/router packages). `PosHomeController` (`lib/src/features/home/pos_home_controller.dart`, 1777 lines) stays the single source of truth.
- Queues: reuse existing Horizon supervisors only — `payments`, `sync`, `delivery`, `reporting`, `telemetry` (config/horizon.php).
- Front-end admin: one Inertia page `resources/js/pages/AdminOperations.vue` with a `workspaces[]` array (line ~44); forms use plain `fetch()` + `X-CSRF-TOKEN` from meta tag; UI kit = reka-ui + Tailwind at `resources/js/components/ui/*`.

---

# TRACK A — Backend & Back Office

## Phase A-P0: Foundations (do first; everything else depends on it)

### A-P0.1 — Tests green + CI gating (Gap 19) — 1.5d

Root cause of parallel-test failures: `pgsql_reporting` connection (config/database.php:102) resolves its DB name at config-load, so paratest's per-worker `_test_{token}` suffix never reaches it.

1. In `phpunit.xml` add `<env name="POS_REPORTING_DB_CONNECTION" value="pgsql"/>` — `config('pos.reporting.connection')` (config/pos.php:~85) then resolves to the default `pgsql` connection, which parallel testing DOES remap. `ReportingConnection::name()` (app/Platform/Support/Reporting/ReportingConnection.php) returns it directly; no code change.
2. The route-binding bug (`{major}` param clobbering controller args) is already fixed in `EnsurePosApiHeaders` (forgetParameter). Re-run `php artisan test --parallel` and fix any residual flakes.
3. CI: in `.github/workflows/ci-cd.yml` remove `continue-on-error: true` from the test step and add `POS_REPORTING_DB_CONNECTION: pgsql` to its env. Keep tests serial in CI (shared service container).

Verify: 3 consecutive green `php artisan test --parallel` runs locally; CI green.

### A-P0.2 — Authorization layer (Gaps 3-core, 18) — 3.5d

RBAC schema already exists (migration `2026_04_20_140000`): `roles` (ulid, nullable merchant_id, name, scope), `permissions` (key unique), `permission_role`, `user_store_role` (user_id bigint, store_id, role_id, unique triple). Today role checks are inline DB queries (see `CreateEnrollmentCode::canManageDevices`). No Gates/Policies. `HandleInertiaRequests` shares only `auth.user`. **Critical hidden issue:** `/api/admin/v1` routes set NO tenant context (`set_config('app.current_merchant_id', …)` only happens in `ApplyPosRequestContext` for device routes).

1. **Models**: Add `app/Modules/Identity/Domain/Models/UserStoreRole.php` (pivot model). Extend `app/Models/User.php` with: `roleNamesForStore(string $storeId): Collection`, `hasRoleInStore(string|array $roles, string $storeId): bool`, `accessibleStoreIds(): Collection`, `accessibleMerchantIds(): Collection`, `distinctPermissionKeys(): Collection` — memoize per request in a `protected ?Collection $storeRoleMap`.
2. **Canonical roles/permissions**: `app/Modules/Identity/Application/ProvisionMerchantRoles.php` — seeds 4 roles per merchant (`Merchant Owner`, `Store Admin`, `Store Manager`, `Cashier`) with permission keys: `devices.manage`, `catalog.manage`, `orders.view`, `orders.refund`, `users.manage`, `reports.view`, `delivery.manage`, `billing.view`, `stores.manage`. Called from merchant creation (A-P1.2). Also a `RbacSeeder` for the global `permissions` rows.
3. **Policies**: `app/Modules/PlatformCore/Interfaces/Authorization/StorePolicy.php` (+ `MerchantPolicy`) with methods per permission key, all delegating to a shared `ResolvesStorePermissions` trait in `app/Platform/Support/Authorization/`. Register in `AppServiceProvider::boot()`: `Gate::policy(Store::class, StorePolicy::class)` + `Gate::before(fn (User $u) => $u->is_super_admin ? true : null)`.
4. **Middleware**:
   - `app/Platform/Http/Middleware/ApplyAdminRequestContext.php` — mirrors `ApplyPosRequestContext`: resolves `{store}`/`{merchant}` route param, 403 unless the user has a role there (or is super admin), binds `TenantContext`, runs `set_config('app.current_merchant_id', …)` on BOTH the default connection AND `ReportingConnection::name()`. Also rejects suspended merchants.
   - `app/Platform/Http/Middleware/EnsureStoreRole.php` — alias `store.role:{permission}` registered in `bootstrap/app.php` `$middleware->alias()`.
   - Wrap the entire `Route::prefix('admin/v1')` group in `routes/api.php` with `ApplyAdminRequestContext::class`. Replace inline checks (e.g. `canManageDevices`) with `Gate::authorize('manageDevices', $store)` in controllers.
5. **Inertia/UI**: extend `HandleInertiaRequests::share()` with `auth.isSuperAdmin`, `auth.permissions` (distinct keys). In `AdminOperations.vue` add `requiredPermission` to each workspace object and filter the rendered list. Convert the `/admin/{section}` closure route (routes/web.php:13) to a small controller that 403s users with zero roles (unless super admin).

Tests: `tests/Feature/Admin/AuthorizationTest.php` — roleless user 403 on every admin route (iterate route list); store-A admin 403 on store B; super admin passes; Inertia props contain permissions.

### A-P0.3 — Lock down registration (Gap 21) — 0.5d

`config/fortify.php` (~line 147): make registration conditional: `...(env('FORTIFY_REGISTRATION_ENABLED', false) ? [Features::registration()] : [])`. Default OFF; set `true` in `phpunit.xml` so existing registration tests keep running (they use `skipUnlessFortifyHas`). `routes/web.php` Welcome page already reads `Features::enabled()` so the UI follows. Production user creation = invitations (A-P1.3).

## Phase A-P1: Platform ops & onboarding

### A-P1.1 — Super Admin portal (Gap 1) — 3d

1. **Migration** `add_super_admin_and_invitations`: `users.is_super_admin` boolean default false; `user_invitations` table (ulid PK, nullable merchant_id/store_id/role_id FKs, email, token_hash unique, invited_by FK users, accepted_at/expires_at, timestamps) with the nullable-merchant RLS policy.
2. **Middleware** `EnsureSuperAdmin` (403 unless `is_super_admin`). New route groups: `routes/api.php` `Route::prefix('super-admin/v1')->middleware(['auth:sanctum', EnsureSuperAdmin::class])`; `routes/web.php` `Route::get('super-admin/{section?}', …)` rendering a new `SuperAdminOperations.vue`.
3. **Endpoints** (controllers in `app/Modules/PlatformCore/Interfaces/Http/Controllers/SuperAdmin/`): `GET merchants` (paginated, status/name filter — `merchants` table has no merchant_id column so RLS doesn't apply); `POST merchants/{merchant}/suspend|reinstate` (action sets `merchants.status`; POS access is already cut by `ApplyPosRequestContext`'s status check; `ApplyAdminRequestContext` adds the same); `GET/POST feature flags` global rows (reuse the action behind `StoreFeatureFlagController`, generalized to nullable store).
4. **UI**: `resources/js/pages/SuperAdminOperations.vue` — structural clone of AdminOperations.vue with 3 workspaces (Merchants, Global Flags, Invitations). Nav entry rendered only when `auth.isSuperAdmin`.
5. **Bootstrap**: artisan command `app/Console/Commands/GrantSuperAdminCommand.php` (`app:grant-super-admin {email}`).

Tests: 403 for non-super-admin on all routes; suspend → that merchant's device bootstrap 403; global flag visible in `pos/v1/config`.

### A-P1.2 — Onboarding APIs + UI (Gap 2) — 3d

Actions in `app/Modules/PlatformCore/Application/Onboarding/`:
- `CreateMerchant(name, currency)` → also calls `ProvisionMerchantRoles`. Route: `POST super-admin/v1/merchants`.
- `CreateStore(Merchant, name, code, mode, timezone, cutoff)` → `POST admin/v1/merchants/{merchant}/stores`, gated `Gate::authorize('manageStores', $merchant)`.
- `CreateDeviceProfile(name, type, capabilities)` → `device_profiles` is global (no merchant_id) → super-admin-only `POST super-admin/v1/device-profiles`; plus read-only `GET admin/v1/device-profiles` for the enrollment form.
- `GET admin/v1/stores/{store}/devices` list + `POST …/devices/{device}/deactivate` (`AdminDeviceController`).
- Enrollment-code minting endpoint already exists (`POST admin/v1/stores/{store}/device-enrollment-codes`) — surface in UI with device-profile select.
- UI: new `onboarding` workspace in AdminOperations.vue (permission-gated); merchant-create lives in SuperAdminOperations.vue.

Tests: merchant create provisions 4 roles + pivots; cross-merchant store create denied; code minting requires `devices.manage`.

### A-P1.3 — User & role management (Gap 3-UI) — 2.5d

Controllers in `app/Modules/Identity/Interfaces/Http/Controllers/`:
- `GET admin/v1/stores/{store}/users` (users with roles in store); `GET …/roles` (for selects).
- `POST admin/v1/stores/{store}/users/invitations` → `InviteUser` action: `user_invitations` row (token `Str::random(64)`, sha256 hash stored, 7-day expiry), queued `UserInvitationMail` (`app/Modules/Identity/Interfaces/Mail/`). Gate `users.manage`.
- `POST …/users/{user}/roles` + `DELETE …/roles/{role}` → `AssignUserStoreRole`/`RevokeUserStoreRole`; guard: cannot revoke own last `Merchant Owner` (lockout protection).
- Public acceptance: `GET /invitations/{token}` (Inertia page `resources/js/pages/auth/AcceptInvitation.vue`) + `POST /invitations/{token}/accept` (name+password) in `routes/web.php`, guest middleware, throttled. Creates user, attaches role, marks accepted, logs in.

Tests: invite→accept→login→role present; expired/used token 410; lockout guard; permission gate.

## Phase A-P2: Back-office CRUD

### A-P2.1 — DataTable component + fetch composable (enabler) — 1.5d

- `resources/js/components/ui/data-table/DataTable.vue`: props `columns {key,label,format?,align?}[]`, `rows`, `pagination {page,perPage,total}`, `loading`; emits `update:page`, `row-click`. Plain reka-ui/Tailwind table — no new npm deps.
- `resources/js/composables/useAdminFetch.ts`: extract the existing fetch+CSRF+query-string pattern from AdminOperations.vue (`submitAction`, lines ~666-723) into a reusable composable so new workspaces don't copy-paste.

### A-P2.2 — Orders browser (Gap 4) — 3d

Backend:
- `GET admin/v1/stores/{store}/orders` → `AdminOrdersQuery` in `app/Modules/Reporting/Application/Queries/` using `ReportingConnection::query(Order::class)`; filters: `status`, `business_date_from/to`, `device_id`, `q`; sort `created_at desc`; `paginate(min(perPage,100))`. ALWAYS add explicit `where('merchant_id', $store->merchant_id)->where('store_id', $store->id)` (belt) in addition to the reporting-connection set_config from `ApplyAdminRequestContext` (braces).
- `GET admin/v1/stores/{store}/orders/{order}` → order + `lines`, `payments` (with refunds/voids), `external_order_links`, receipt ref. Scope check: 404 unless `$order->store_id === $store->id` (shared `ScopedToStore` controller concern).
- API Resources: `AdminOrderResource`, `AdminOrderDetailResource` in `app/Modules/OrderRegister/Interfaces/Http/Resources/`. Gate `orders.view`. Controller `AdminOrderController` in OrderRegister module.

UI: `orders` workspace — filter bar (date range, status select), DataTable, detail in existing `Sheet` component on row click.

Tests: pagination meta; filters; cross-store id → 404; missing permission → 403.

### A-P2.3 — Catalog Item + Category CRUD (Gap 5) — 2d

Verified missing: no admin endpoints for catalog items or categories (only variants/modifiers/combos exist).
- `AdminCatalogItemController` (Catalog module): `GET index` (paginate, filters `category_id`, `is_active`, `q`), `POST store`, `PUT update`, `POST deactivate`. Actions `Create/Update/DeactivateCatalogItem` in `app/Modules/Catalog/Application/Items/`. NOTE: `catalog_items` is merchant-scoped (unique `(merchant_id, sku)`) — route under `admin/v1/stores/{store}/catalog/items` for consistency but write with `$store->merchant_id`; validate SKU per merchant. **Include `category_id`, `sku`, `barcode` in `pos/v1/config` items payload** (PosConfigController) — Track B depends on this (B-CROSS).
- `AdminCategoryController`: same verbs; `categories` ARE store-scoped.
- Gate `catalog.manage`. UI: upgrade `catalog` workspace to DataTable list + create/edit Dialog.

Tests: CRUD; duplicate SKU 422; deactivated item absent from `pos/v1/config`; cross-merchant isolation.

## Phase A-P3: Operational correctness

### A-P3.1 — Order edit leases (Gap 11) — 2d

Reuse existing `AcquireEditLease`/`HeartbeatEditLease`/`ReleaseEditLease` + `EditLeaseException` (app/Modules/PlatformCore/Application/Concurrency) verbatim with `resource_type='order_edit'`.
1. `config/pos.php` leases: `'order_edit_ttl_seconds' => 45, 'order_edit_heartbeat_seconds' => 15` (PRD values).
2. POS routes (inside the idempotency group, mirroring dining tables): `POST orders/{order}/edit-lease`, `…/edit-lease/heartbeat`, `…/edit-lease/release` → `OrderEditLeaseController` copying `DiningTableController`'s claim/heartbeat/release structure including the 409 JSON mapping (`error_code`, `lease_version`, `current_holder_device_id`, expiry).
3. Enforcement: `AssertOrderEditLease::handle(Device, Order)` in `app/Modules/OrderRegister/Application/Concurrency/` — throws 409 if a LIVE lease is held by a DIFFERENT device; **absent lease = allowed** (matches table semantics; single-device flow unaffected). Inject before mutation in `OrderTenderController`, `OrderCashCheckoutController`, payment refund/void controllers.
4. `TenderOrder`/`CashCheckoutOrder` release the lease best-effort on terminal state.

Tests: foreign live lease → 409; expired lease → second device claims; close releases.

### A-P3.2 — Inbound delivery webhooks (Gap 12) — 3d

Mirror the Fiserv pattern (`FiservTransNotifyWebhookController` + `ProcessFiservTransNotifyWebhook`: normalize → verify signature → persist provider event → idempotent match → exception case on failure).
1. **Migration** `create_delivery_provider_events`: ulid PK, nullable merchant_id/store_id, `channel_key`, `external_event_id` (unique with channel_key — idempotency anchor; fallback sha256 of raw body), `external_order_id` nullable, `event_type`, `signature_valid` bool, `payload` json, `processed_at`, nullable FK external_order_link_id. Bespoke nullable-merchant RLS. Same migration: add indexed `external_store_id` column to `delivery_channel_configs` (backfill from its `mapping` json) and set `'credentials' => 'encrypted:array'` cast on the DeliveryChannelConfig model.
2. **Route** (top-level, no auth, beside fiserv): `POST webhooks/delivery/{channel}` with `whereIn('channel', ['uber_eats','door_dash'])` → `DeliveryWebhookController`.
3. **Controller**: read raw body + headers; resolve store via `delivery_channel_configs` lookup on `channel_key` + `external_store_id` from payload; verify HMAC via new contract method `OnlineOrderingChannelAdapter::verifyWebhookSignature(array $config, string $rawBody, array $headers): bool` (UberEats: `X-Uber-Signature` = HMAC-SHA256(body, client_secret); DoorDash: `X-DoorDash-Signature`; **confirm exact header names against current provider docs before coding**; secret from `credentials->webhook_secret`). Bad signature → persist event `signature_valid=false`, `OpenExceptionCase('delivery','webhook_signature_invalid')`, return 401. Good → persist event, return 200 fast, dispatch job.
4. **Job** `ProcessDeliveryWebhookJob` (queue `delivery`, `$tries=5`, backoff `[10,30,60,300,600]`, `ShouldBeUnique` by event id): transform via adapter; order-create events need a device-less path → `ResolveSystemDevice` action (find-or-create per-store device `name='_system_delivery'`, `platform='server'`, global `Server Agent` profile, plus an open system register session per business date, `drawer_code='_delivery'`) then reuse `IngestExternalDeliveryOrder::handle($systemDevice, …)` unchanged (its ExternalOrderLink upsert is idempotent). Status/cancel events → `UpdateExternalOrderStatus`/`CancelExternalOrder` by external_order_id; unknown link → exception case `unmatched_delivery_event`.

Tests: valid sig → 200 + link + order via system device; replay → single link; bad sig → 401 + exception case; unknown store → 200 + exception case.

### A-P3.3 — Outbound delivery HTTP clients (Gap 13) — 2.5d

1. `config/pos.php` `delivery_channels`: `sandbox` (env `POS_DELIVERY_SANDBOX`, default true), per-channel `base_url`/`sandbox_base_url` envs, `timeout_seconds: 10`, `connect_timeout_seconds: 3`, `retries: 3`, `retry_backoff_ms: 250`.
2. `app/Modules/DeliveryIntegrations/Infrastructure/Http/DeliveryHttpClient.php`: builds `Http::baseUrl(...)->timeout(...)->connectTimeout(...)->retry(...)->acceptJson()->withToken($credentials['access_token'] ?? '')`. Empty base_url or sandbox-without-URL → return canned success with `'transport' => 'stub'` (preserves current stub behavior).
3. Implement real bodies in `UberEatsAdapter`/`DoorDashAdapter` for `publishMenu`, `setStoreAvailability`, `setItemAvailability`, `updateStatus`, `confirmOrder`, `cancelOrder` using existing transformers; non-2xx after retries → `DeliveryTransportException` → existing jobs (`PublishDeliveryMenuJob`, `PushDeliveryUpdateJob`) catch → `OpenExceptionCase('delivery_publish_failed')`; set `last_menu_published_at` only on success.
4. Tests: `Http::fake()` asserts transformed bodies; 500×3 → exception case; sandbox sends nothing (`Http::fake()->assertNothingSent()`). Add `Http::preventStrayRequests()` to `tests/Pest.php` beforeEach.

### A-P3.4 — Email receipts (Gap 16 + extra) — 1.5d

- `POST pos/v1/receipts/{receipt}/email` (POS group, idempotency block) — FormRequest validates `email`; queued Mailable `ReceiptMail` (`app/Modules/OrderRegister/Interfaces/Mail/`) rendering receipt lines/totals/masked-PAN metadata from the receipt payload; queue `default` mail. Gate: device-scoped (receipt belongs to device's store; ScopedToStore-style check).
- Audit: log send in audit_logs (existing audit pattern).
- Track B adds the email field on the receipt confirmation screen (B-5).

Tests: send queues mailable with correct receipt; cross-store receipt 404.

## Phase A-P4: Async & data layer

### A-P4.1 — Billing metering via domain events (Gap 15) — 2d

First events in the codebase — thin DTOs (IDs only), explicit registration (module paths are not auto-discovered).
1. Events in `app/Modules/{Owner}/Domain/Events/`: `OrderPaid(orderId, merchantId, storeId, totalMinor)`, `DeliveryOrderIngested(...)`, `GiftCardIssued(...)`, `PayrollSnapshotGenerated(...)`.
2. Dispatch (after commit — `DB::afterCommit`): `TenderOrder` + `CashCheckoutOrder` (MOVE the existing direct `RecordUsage` call from TenderOrder into the listener so both paths meter), `IngestExternalDeliveryOrder`, `IssueGiftCard`, `GeneratePayrollSnapshot`.
3. Listeners (one per event) in `app/Modules/Billing/Application/Listeners/`, `ShouldQueue`, `public string $queue = 'reporting';`, each calling `RecordUsage::handle(...)` with metric keys: `orders.paid`, `delivery.orders.ingested`, `gift_cards.issued`, `payroll.snapshots.generated`. Register in `AppServiceProvider::boot()` via `Event::listen(...)`.
4. **Idempotency** (queued retries can double-meter): migration adds `usage_records.source_ref` nullable string + unique `(metric_key, source_ref)`; `RecordUsage` accepts optional `sourceRef`, upsert-ignores conflicts.

Tests: `Event::fake` dispatch assertions; sync-queue listener integration writes one row; replay → still one row.

### A-P4.2 — Reporting summary tables (Gap 14) — 3d

1. **Migration** `create_report_daily_store_summaries`: ulid PK, merchant_id, store_id, business_date date, orders_count, gross_minor, tax_minor, discount_minor, net_minor, tender_breakdown json, last_aggregated_at, is_final bool, unique `(store_id, business_date)`, inline RLS. Writes go to PRIMARY (replica is read-only; it replicates).
2. `RefreshDailyStoreSummary::handle(storeId, businessDate)` — extract the aggregation SQL shared with `BusinessDaySummaryQuery` into one query object/trait (`BuildsBusinessDayAggregates`) so live and materialized paths can't drift; upsert.
3. Scheduling (`routes/console.php`): `RefreshReportSummariesCommand` dispatches per-store jobs on queue `reporting` — `everyFiveMinutes()` for today; `dailyAt('04:30')` finalizes yesterday (`is_final=true`).
4. Read path: `BusinessDaySummaryQuery` serves the summary row when fresh (`is_final` or `last_aggregated_at` within `pos.reporting.summary_staleness_seconds`, default 300) else falls back to live aggregation; response gains `source: 'summary'|'live'`.

Tests: refresh matches live aggregation on seeded data; freshness/fallback behavior; nightly finalization.

### A-P4.3 — Sync conflict arbitration (Gap 17) — 3.5d (riskiest item)

1. **Migration** `add_sync_arbitration_columns`: `orders.status_seq` unsignedInteger default 1; new append-only `inventory_ledger_entries` (ulid PK, merchant_id, store_id, sku, nullable catalog_item_id, seq bigint, delta_quantity, reason enum-ish string `sale|receive|transfer_in|transfer_out|adjust|return`, source_type/source_id, nullable device_id, occurred_at, unique `(store_id, sku, seq)`, RLS); `sync_events.conflict_code` nullable string.
2. Append ledger entries from existing Retail actions (receive/transfer/adjust/return) and order-line sale paths — inside their existing transactions (`inventory_balances` row already `lockForUpdate`s per sku; compute `seq = max(seq)+1` under that lock).
3. `ArbitrateSyncEvent` action invoked per event in `PushSyncEvents` after dedupe:
   - `order` + `status_change`: payload carries `base_status_seq`. Stale (`< order.status_seq`) → mark event `conflict_superseded`, no mutation; if money-bearing → `OpenExceptionCase('sync','order_conflict')`. Fresh + legal transition (define const map `app/Modules/OrderRegister/Domain/OrderStatus.php`: open→sent→closed/voided) → apply + increment seq. Illegal → `conflict_rejected` + exception case.
   - `inventory`: deltas always append + re-derive balance (commutative); absolute "set on-hand" events with stale `base_ledger_seq` → `conflict_rejected` + exception case.
   - All other entity types: keep accept-all (document explicitly).
4. Push response rows gain `status` (`accepted|conflict_superseded|conflict_rejected`), `conflict_code`, and server seqs — additive, no client break.

Tests: stale seq → superseded + exception for tender events; legal transition increments; delta replay idempotent via `(device_id, local_event_id)`; stale absolute set rejected.

---

# TRACK B — Flutter Cashier UI (apps/pos)

Architectural rules: `PosHomeController` remains the only state holder; new screens get it by constructor + `ListenableBuilder`. Navigation = new `PosShell` with `IndexedStack` + `NavigationRail` (≥840dp) / bottom `NavigationBar`; transient flows via `Navigator.push`. **The existing console screen survives as the "Operations" tab** (only change: an `embedded: true` flag that skips its own Scaffold/AppBar). Money formatting via a new dep-free `formatMinor()` util.

Target layout: new dirs `lib/src/navigation/`, `lib/src/features/{shell,checkout,tables,appointments}/`, `lib/src/core/hardware/`, `lib/src/app/pos_theme.dart`, shared `test/support/test_harness.dart`.

### B-1 — Theme extraction + money util + test harness — 1d
- `PosTheme.light()` moves the seed `#1F3A2E`/cream `#F4F0E8` theme out of `pos_app.dart`; `PosColors` constants harvested from `pos_home_screen.dart` literals + semantic status colors (table: available/claimedByMe/claimedOther/occupied; appointment: booked/checked_in/completed/cancelled). New code uses `PosColors`; do NOT mass-replace in the old screen.
- `core/support/money_format.dart`: `formatMinor(int, {currency})` integer math, no intl.
- `test/support/test_harness.dart`: extract the seeded `PosDatabase.memory()` + fake gateway pattern from `pos_home_screen_test.dart` into `buildSeededController(...)` + `FakePosGateway`/`FakeCardTerminalGateway`/`RecordingReceiptPrinter`.

### B-2 — Controller additive API — 1d
Add to `PosHomeController` (additive only): `removeLine(item)`, `setItemQuantity(item, q)`, `clearCart()`, `quantityOf(id)`, `catalogCategories` getter; `bookAppointment({staffProfileId, serviceItemId, startsAt, endsAt, customerId?, notes?})` wrapping the existing two-step gateway flow (`claimAppointmentSlot` → `createAppointment` — verify the slot-claim response key in `pos_api_client.dart`). Add nullable `categoryId`/`categoryName` to `CatalogItemSnapshot` (pos_models.dart:119) parsed null-safely (old caches keep working). Unit tests for all.

### B-3 — Navigation shell + enrollment screen (Gap-enabler) — 2d
`lib/src/navigation/pos_shell.dart`: `ListenableBuilder` branching — loading → splash; `!isEnrolled` → `EnrollmentScreen` (extracted from `_EnrollmentCard`); `requiresReauth` → `DeviceLockScreen` (B-9; until then falls through to enrollment which already handles reauth); else `IndexedStack` tabs: Register / Tables / Appointments / Operations(=`PosHomeScreen(embedded: true)`). Shell appbar/rail header: store name, register-session chip, sync + print-spool badges; route errors/status to `ScaffoldMessenger` for new tabs. `pos_app.dart`: `theme: PosTheme.light(), home: PosShell(...)`. When `requiresReauth` flips true, `popUntil(first)` so pushed routes can't sit above the lock.

### B-4 — Register screen (Gap 6, part 1) — 4d
`features/checkout/register_screen.dart`: wide = `Row[product area flex:3, cart panel 360-420px]`; narrow = grid + bottom total bar opening cart as modal sheet.
- Product area: `CategoryBar` (ChoiceChips from `catalogCategories` + "All") + debounced name search (essential while categories await A-P2.3 payload change — UI degrades to "All" when absent); `GridView` (`maxCrossAxisExtent: 180`), `ProductTile` = name, `formatMinor(effectivePriceMinor)`, sold-out ribbon, qty badge, tap → `addItem`.
- `CartPanel`: header (claimed-table chip from `activeClaimedTable`, clear-cart confirm), `CartLineTile` rows (qty steppers via `addItem`/`removeItem`, trash → `removeLine`), customer/discount chips opening `CustomerDiscountSheet` (reuses `searchCustomers`/`selectCustomer`/`selectDiscount`), totals from the four existing `cartEstimated*Minor` getters, CTA "Charge {total}" — disabled when cart empty/busy/no open register (then reads "Open Register" → float dialog) — pushes `TenderScreen`.

### B-5 — Tender flow + receipt confirmation (Gap 6, part 2 + extras) — 3d
`TenderScreen`: `SegmentedButton` Cash|Card|Split|Gift Card filtered by `paymentCapabilities.supportedTenders`.
- Cash: `CashKeypad` with computed quick amounts (exact, then next 1/5/10/20 major units) + numpad; change-due line; confirm → `checkoutCash(tenderedMinor)`.
- Card: **tip prompt** (preset % chips via `MinorAmount.percentageOf` + custom) → `checkoutCard(tipMinor)`; busy interstitial "Follow prompts on terminal"; if `inDoubtOrderId != null` after await → blocking in-doubt panel (no pop).
- Split: validated cash-applied (0 < cash < total) + tendered + card tip → `checkoutSplitCashCard`.
- Gift card: code field (+ scanner autofill from B-8) + balance check.
- Success detection: compare `lastReceipt?.receiptId` before/after the await (NOT messages — `_runBusy` clears them). Success → `pushReplacement(ReceiptConfirmationScreen)`.
- `ReceiptConfirmationScreen`: change due, receipt number, **receipt options** — Print again (`printPendingReceipts`), **Email receipt** (field → new gateway method calling A-P3.4 endpoint), No receipt, New Sale (pop; cart already cleared by controller).

### B-6 — Tables floor plan (Gap 7) — 2d
`features/tables/tables_screen.dart`: group `diningTables` by `zoneName ?? 'Floor'`; per-zone `GridView` of `TableTile` (state colors per `PosColors`: mine=filled forest + lease countdown (1s timer only on claimed-by-me tiles, cancel in dispose), other=amber+lock, occupied=outlined, available=muted green; shows name/capacity/party). Tap: available → `ClaimTableDialog` (party+guests) → `claimDiningTable`; mine → action sheet [Open order → shell callback switches to Register tab (claimed-table chip already shows there), Release → confirm]; other → read-only info. Pull-to-refresh.

### B-7 — Appointments day view (Gap 8) — 3d
`features/appointments/appointments_screen.dart`: date header (prev/today/next + picker; screen-local state); staff columns (`workforceStaff` + "Unassigned") over a shared vertical time axis 08:00–22:00 (1px/min; `Positioned` blocks per column `Stack`, naive horizontal offset on overlap). `AppointmentBlock` colored by status; tap → detail sheet with contextual Check-in (`checkInAppointment`) / Complete (`completeAppointment`). FAB/long-press → `BookAppointmentDialog` (staff dropdown, service dropdown from catalog, start time, duration 15/30/45/60, optional customer, notes) → `bookAppointment` (B-2). Filter `appointments` client-side by date for v1.

### B-8 — Hardware adapter seams (Gap 9 + no-sale extra) — 2d
New `lib/src/core/hardware/`:
- Move `receipt_printer.dart` here (deprecated re-export at old path). `EscPosBuilder` (pure, byte-tested: init/text/feed/cut/drawerKick). `NetworkEscPosReceiptPrinter implements ReceiptPrinter`: resolves `printerNames` → host:port via new `PrinterEndpointStore` (local drift/secure-storage map; needed because print routes carry no address — backend ticket optional), 5s socket timeout; throw keeps job pending in the 7-day spool (existing retry semantics — no controller change).
- `CashDrawer` interface + `DebugCashDrawer` + `PrinterKickCashDrawer`; inject as optional controller param (default Debug — existing tests unaffected); fire on cash/split success. Add **No-Sale drawer-open** button in Operations tab (audit event to sync outbox).
- `BarcodeScanner` interface + `HidKeyboardBarcodeScanner` (HardwareKeyboard handler; buffer ≥4 chars, inter-key gap <50ms, emit on Enter; **disable while a non-scanner TextField has focus** via `FocusManager`) + `DebugBarcodeScanner`. Wire: RegisterScreen → match item by id/SKU (SKU needs A-P2.3 payload) → `addItem`; TenderScreen gift-card autofill. Camera impl deferred (stub throwing `UnimplementedError`).
- Wire impls in `app_bootstrapper.dart` (`kDebugMode` → debug impls).

### B-9 — Device lock / auth-hold UX (Gap 22) — 1.5d
`DeviceLockScreen` (full-bleed forest green): lock icon, "Device authorization revoked", quarantined-event count (`authHoldSyncEvents`), data-preserved note, re-enrollment form (URL prefilled, code, name) → `enroll(...)`. Server-issued enrollment code IS the manager gate. After enroll, if `authHoldSyncEvents > 0` → inline manager-approval step (`approveHeldSyncEvents`). Shell branch on `requiresReauth` makes the lock take over the whole UI mid-flow; `PopScope(canPop: false)`.

### B-10 — Test completion + polish — 2d
Per-screen widget tests (pump with `PosTheme.light()`): register (tile tap → totals; category filter; line void; charge disabled w/o session), tender (quick amount → change due; success pushes confirmation; in-doubt panel via scripted fake), tables (zone grouping; claim params; release on mine), appointments (block positioning; check-in call), shell (rail at 1280×800 vs bottom bar at 400×800; lock takeover), unit (EscPos bytes, HID debounce via FakeAsync, cart methods, bookAppointment sequencing). Keep `pos_home_screen_test.dart` green throughout.

### B-CROSS — Backend tickets Track B depends on (file under Track A)
1. `category_id`/`category_name` + `sku`/`barcode` per item in `pos/v1/config` (bundled into A-P2.3). UI degrades gracefully until then.
2. Email receipt endpoint (A-P3.4).
3. Optional/non-blocking: `dining_table_id` on `createOrder` (table↔order linkage is display-only until added); printer host/port in print-route payload (else local `PrinterEndpointStore` UI in Operations tab).

---

# External / non-code items

### EXT-1 — Live card payments (Gap 10) — client-owned, eng support ~1d when artifacts arrive
Checklist: client provides terminal estate list + boarding values → load into terminal profile config consumed at device bootstrap; Fiserv certification scripts + signed evidence → approval → live credentials loaded via `.env`/secret store (never committed); flip `FISERV_BLUEPAY_MODE=LIVE`. Engineering tasks now: none blocked; document the secret-loading path in `infra/VPS-SETUP.md`.

### EXT-2 — Production hardening (Gap 20, right-sized) — 2d
In scope now: offsite backup push (extend `infra/backup.sh` to rclone the nightly pg_dump to S3/B2), uptime monitoring (UptimeRobot/healthchecks.io on `https://app.zendev.us` + `/up` health route), error tracking (Sentry SDK for Laravel — `composer require sentry/sentry-laravel`, DSN via env), Horizon alert thresholds already configured (verify notifications route to mail/Slack), documented restore drill (run quarterly: restore latest dump to scratch DB, boot, smoke). Explicitly deferred (client infra decision, PRD DR section): multi-region warm standby, Route 53 failover, canary deploys.

---

# Migrations (chronological, all in apps/platform/database/migrations)
1. `add_super_admin_and_invitations` (A-P1.1/A-P1.3)
2. `create_delivery_provider_events` + delivery_channel_configs columns (A-P3.2)
3. `create_report_daily_store_summaries` (A-P4.2)
4. `add_sync_arbitration_columns` (orders.status_seq, inventory_ledger_entries, sync_events.conflict_code) (A-P4.3)
5. `add_usage_record_source_ref` (A-P4.1)

# Verification (end-to-end)
- **Per item**: Pest feature tests listed inline (Track A) / widget+unit tests (Track B). CI must be green (A-P0.1 re-enables gating) before each merge.
- **Track A smoke** (staging/VPS): grant super admin → create merchant → roles auto-provisioned → invite user → accept → assign Store Admin → create store + catalog items → mint enrollment code → enroll device → browse orders in back office after a POS sale → simulate delivery webhook (curl with computed HMAC) → order appears + exception case on bad signature → check `usage_records` rows + `report_daily_store_summaries` after 5 min.
- **Track B smoke** (emulator/device against staging): enroll → Register tab: add items via grid → cash checkout with quick-amount → change due + receipt confirmation → email receipt → Tables: claim/release with second device showing amber lock → Appointments: book/check-in/complete → revoke device server-side → lock screen appears mid-flow → re-enroll → approve held events.
- **Regression**: full `php artisan test --parallel` + `flutter test` green; existing console (Operations tab) byte-for-byte functional.

# Effort summary
| Track | Items | Est |
|---|---|---|
| A-P0 foundations | 19, 3-core/18, 21 | 5.5d |
| A-P1 platform ops | 1, 2, 3-UI | 8.5d |
| A-P2 back office | DataTable, 4, 5 | 6.5d |
| A-P3 operational | 11, 12, 13, 16 | 9d |
| A-P4 async/data | 15, 14, 17 | 8.5d |
| **Track A total** | | **~38d** |
| B-1..B-10 | 6, 7, 8, 9, 22 + extras | **~21.5d** |
| EXT-1/EXT-2 | 10, 20 | ~3d |
| **Grand total** | | **~60 dev-days** (2 devs in parallel ≈ 6-7 weeks) |

Risk notes: A-P4.3 (sync arbitration) is the most iteration-prone; A-P3.2 signature header names must be confirmed against current Uber/DoorDash docs; A-P1.1 needs a 1-hour spike confirming how the current DB role interacts with RLS on admin routes.
