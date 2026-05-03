# POS Platform Workspace

This repository contains the approved implementation through Phase 3 of the multi-tenant POS platform:

- [apps/platform](apps/platform) is the Laravel 12 cloud application for tenant-scoped POS state, payments, stored value, sync authority, reporting, and admin operations.
- [apps/pos](apps/pos) is the Flutter POS client for device auth, offline cache, local queues, printing, and the Phase 2 tendering surface.
- [infra/docker-compose.yml](infra/docker-compose.yml) provides local PostgreSQL 16 and Redis services for development.

## Implemented scope (Phase 1a -> Phase 3)

- Phase 1a and 1b foundations: tenancy, stores, devices, auth, sync primitives, catalog, pricing, tax, register sessions, customer basics, discounts, table leases, print routing, and business-day reporting
- Phase 2 backend: payment boundary, cash and card tenders, split tenders, tips, refunds, voids, gift cards, memberships, sync recovery runs, audit logs, and billing metering hooks
- Phase 2 Flutter client: cash/card/split/gift-card checkout, stored-value lookup and issue/top-up, membership activation and lookup, receipt and label spool handling, and sync recovery controls
- Phase 3 backend: salon services, staff profiles, staff-service assignments, appointment slot claims with no-overlap enforcement, appointment lifecycle (confirm/check-in/complete), shift and attendance tracking, hourly wage rules, dynamic commission rules, weekly payroll snapshot generation, and labor analytics APIs

## Prerequisites

- Docker Desktop
- PHP `8.3+` with `pdo_pgsql`
- Composer
- Node.js `20+`
- Flutter SDK with Windows desktop support enabled if you want to run the POS app on Windows

## Start local services

From the repo root:

```powershell
docker compose -f infra/docker-compose.yml up -d
```

This starts:

- PostgreSQL on `127.0.0.1:5432`
- Redis on `127.0.0.1:6379`

## Start the Laravel platform app

Open a terminal in `apps/platform` and run:

```powershell
composer install
npm install
Copy-Item .env.example .env
php artisan key:generate
php artisan migrate
```

For Fiserv + PAX card flows, set these `.env` values in `apps/platform/.env` before boot:

```powershell
POS_DEFAULT_PAYMENT_PROVIDER=fiserv_bluepay
FISERV_BLUEPAY_ACCOUNT_ID=...
FISERV_BLUEPAY_USER_ID=...
FISERV_BLUEPAY_SECRET_KEY=...
FISERV_BLUEPAY_HASH_TYPE=HMAC_SHA256
FISERV_BLUEPAY_MODE=TEST
FISERV_BLUEPAY_BP20POST_URL=https://secure.bluepay.com/interfaces/bp20post
FISERV_BLUEPAY_DAILY_REPORT_URL=https://secure.bluepay.com/interfaces/bpdailyreport2
FISERV_BLUEPAY_WEBHOOK_VERIFY_BP_STAMP=true
```

Then start the platform processes:

```powershell
php artisan serve --host=127.0.0.1 --port=8000
```

In a second terminal in `apps/platform`:

```powershell
php artisan horizon
```

If you want the back-office frontend assets/watcher too, use a third terminal in `apps/platform`:

```powershell
npm run dev
```

The API will be available at `http://127.0.0.1:8000`.
Fiserv webhook endpoint: `POST /api/webhooks/fiserv/trans-notify`.

## Start the Flutter POS app

Open a terminal in `apps/pos` and run:

```powershell
flutter pub get
flutter run -d windows
```

For Android terminal checkout testing:

```powershell
flutter run -d android
```

The Android `pos_app/pax_terminal` bridge is wired for the PAX flow contract and currently returns `in_doubt` unless a POSLink-backed call is linked in `PaxTerminalChannel.kt`.

When the POS app opens:

1. Enter the platform API base URL as `http://127.0.0.1:8000`
2. Enroll the device with a valid store-scoped enrollment code
3. Refresh cloud state to load the store config, tender capabilities, and print routes

## Verification commands

Laravel:

```powershell
php artisan test --env=testing
vendor/bin/pint
```

Flutter:

```powershell
flutter analyze
flutter test
```

## Important local note

The Laravel app is PostgreSQL-first. If your local PHP runtime does not have `pdo_pgsql`, the platform app will not boot against the Docker PostgreSQL instance until that extension is installed.
