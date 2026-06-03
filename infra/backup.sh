#!/usr/bin/env bash
# Nightly Postgres backup. Installed at /usr/local/bin/pos-backup.sh and run
# from /etc/cron.d/pos-platform-backup (see infra/VPS-SETUP.md step 14).
#
# Writes gzipped pg_dump files to /var/backups/pos-platform/ and prunes
# anything older than $RETENTION_DAYS.

set -euo pipefail

REPO_DIR=/srv/pos-platform
DEST=/var/backups/pos-platform
RETENTION_DAYS=14

COMPOSE_FILE="$REPO_DIR/infra/docker-compose.production.yml"
ENV_FILE="$REPO_DIR/apps/platform/.env"

mkdir -p "$DEST"
TS=$(date -u +%Y%m%dT%H%M%SZ)
OUT="$DEST/$TS.sql.gz"

# Pull credentials out of the runtime .env so we never hard-code them here.
# shellcheck disable=SC2046
export $(grep -E '^(DB_DATABASE|DB_USERNAME)=' "$ENV_FILE" | xargs)
: "${DB_DATABASE:=pos_platform}"
: "${DB_USERNAME:=pos_app}"

echo "[$(date -Is)] Dumping $DB_DATABASE → $OUT"

docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" \
  exec -T postgres pg_dump -U "$DB_USERNAME" -d "$DB_DATABASE" \
  | gzip --best > "$OUT"

# Sanity check — fail loudly if the dump is suspiciously small.
if [[ $(stat -c %s "$OUT") -lt 1024 ]]; then
  echo "ERROR: dump file is < 1 KB, likely failed" >&2
  exit 1
fi

# Prune.
find "$DEST" -name '*.sql.gz' -type f -mtime "+$RETENTION_DAYS" -print -delete

echo "[$(date -Is)] Backup OK ($(du -h "$OUT" | cut -f1))"
