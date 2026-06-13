# Production hardening (EXT-2)

Right-sized operational hardening for the single-VPS deployment. Multi-region
warm standby / Route 53 failover / canary deploys are intentionally deferred
(client infra decision, PRD DR section).

## 1. Offsite backups

The nightly backup (`infra/backup.sh`) keeps 14 days locally and, when
configured, pushes each dump offsite via rclone.

```bash
# On the VPS, as root:
apt install -y rclone
rclone config            # add a remote, e.g. Backblaze B2 named "b2"

# Tell the backup cron where to push (edit /etc/cron.d/pos-platform-backup):
#   0 3 * * * root BACKUP_RCLONE_REMOTE="b2:my-bucket/pos-backups" /usr/local/bin/pos-backup.sh >> /var/log/pos-backup.log 2>&1
```

Set a bucket lifecycle rule to expire offsite copies (e.g. 90 days). Without
`BACKUP_RCLONE_REMOTE`, backups stay local-only (still functional).

## 2. Error tracking (Sentry)

Sentry is wired but inert until a DSN is set, so it adds nothing in dev.

```bash
# One-time, on a machine with composer + network (or in the build):
cd apps/platform
composer require sentry/sentry-laravel
php artisan sentry:publish --dsn=__YOUR_DSN__   # writes config/sentry.php
```

Then set in `apps/platform/.env`:

```ini
SENTRY_LARAVEL_DSN=https://...ingest.sentry.io/...
SENTRY_TRACES_SAMPLE_RATE=0.1
```

If the DSN is empty, the SDK is a no-op. The app's exception handler already
funnels through Laravel's reporting, which Sentry hooks automatically once
installed.

> Until `composer require` is run, the package is absent and the DSN is ignored —
> this keeps the repo installable without the dependency. Install it as part of
> the production image build (add the `composer require` to a build step) when you
> want live error capture.

## 3. Uptime monitoring

Point a free external monitor at the health endpoint (already exposed by
`bootstrap/app.php` via `health: '/up'`):

- URL to monitor: `https://app.zendev.us/up` (expects HTTP 200)
- Providers: UptimeRobot, healthchecks.io, or Better Stack — 1–5 min interval.
- Alert channel: email or Slack webhook.

## 4. Horizon queue alerts

Horizon is already configured with per-supervisor queues. Verify its
notifications route somewhere actionable:

```bash
# In apps/platform/.env, confirm a mail/Slack channel is set for failures.
# Horizon's long-wait + failed-job thresholds live in config/horizon.php.
```

The scheduled `pos:dlq-monitor` command already opens a critical exception case
when a job sits in the DLQ too long — make sure the scheduler container is
running (`docker compose ... ps` shows `pos-scheduler` up).

## 5. Quarterly restore drill

Backups are only as good as a tested restore. Run this every quarter:

```bash
# 1. Copy the latest dump to a scratch location.
LATEST=$(ls -t /var/backups/pos-platform/*.sql.gz | head -1)

# 2. Spin up a throwaway Postgres and restore into it.
docker run -d --name pos-restore-test -e POSTGRES_PASSWORD=test -p 55433:5432 postgres:16
sleep 5
gunzip -c "$LATEST" | docker exec -i pos-restore-test psql -U postgres -d postgres

# 3. Smoke check: row counts on key tables.
docker exec pos-restore-test psql -U postgres -d postgres \
  -c "SELECT (SELECT count(*) FROM merchants) AS merchants,
             (SELECT count(*) FROM orders) AS orders,
             (SELECT count(*) FROM receipts) AS receipts;"

# 4. Tear down.
docker rm -f pos-restore-test
```

Record the date and result. A restore that errors or shows zero rows is a
backup failure to investigate immediately.
