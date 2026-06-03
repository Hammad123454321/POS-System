#!/usr/bin/env bash
# Production container entrypoint. Warms Laravel's caches against the runtime
# .env, makes sure storage is writable, then hands off to whatever CMD compose
# specified (FrankenPHP, horizon, scheduler, or a one-off artisan command).

set -euo pipefail

cd /app

# Storage directories must be writable by the runtime user.
chown -R www-data:www-data storage bootstrap/cache || true

# Refresh the symlink for /storage/* public URLs (idempotent).
php artisan storage:link --force >/dev/null 2>&1 || true

# Build optimized caches from the runtime .env (config/route/view/event).
# Skipped if Laravel can't even read its environment (e.g. during a one-off
# artisan command run before the container has the env mounted).
php artisan optimize >/dev/null 2>&1 || true

exec "$@"
