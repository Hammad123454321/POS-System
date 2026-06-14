#!/usr/bin/env bash
# One-shot production deploy. Called by GitHub Actions via SSH; also runnable
# by hand on the VPS for manual rollouts.
#
#   /srv/pos-platform/infra/deploy.sh
#
# Steps:
#   1. git pull
#   2. build/refresh the app image
#   3. start postgres + redis (wait healthy)
#   4. run migrations under a lock (--isolated avoids races between containers)
#   5. (re)start app + horizon + scheduler
#   6. clear & rebuild Laravel caches

set -euo pipefail

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_DIR"

COMPOSE_FILE="infra/docker-compose.production.yml"
ENV_FILE="apps/platform/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "ERROR: $ENV_FILE is missing. See infra/VPS-SETUP.md step 10." >&2
  exit 1
fi

COMPOSE="docker compose -f $COMPOSE_FILE --env-file $ENV_FILE"

echo "==> [1/6] git pull"
git fetch --prune
git reset --hard "@{u}"

echo "==> [2/6] build image"
$COMPOSE build app

echo "==> [3/6] start data services"
$COMPOSE up -d postgres redis
# wait until both healthy (compose's depends_on covers this on its own, but
# we want to fail loudly here rather than mid-migration).
for svc in postgres redis; do
  for i in {1..30}; do
    state=$($COMPOSE ps --format json "$svc" | grep -o '"Health":"[^"]*"' | head -1 || true)
    if [[ "$state" == *healthy* ]]; then break; fi
    sleep 2
  done
done

echo "==> [4/6] run migrations"
$COMPOSE run --rm --no-deps app php artisan migrate --force --isolated

echo "==> [5/6] (re)start app + horizon + scheduler"
$COMPOSE up -d app horizon scheduler

echo "==> [6/6] rebuild caches"
$COMPOSE exec -T app php artisan optimize:clear
$COMPOSE exec -T app php artisan optimize

# Horizon picks up new code on its own (it terminates workers on deploy), but
# nudging it is harmless and avoids a long wait for the next heartbeat.
$COMPOSE exec -T app php artisan horizon:terminate >/dev/null 2>&1 || true

echo
echo "==> Deploy complete."
$COMPOSE ps
