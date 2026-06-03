# VPS one-time setup — `app.zendev.us` (`2.25.165.25`)

Run this once on a fresh Ubuntu 22.04 / 24.04 VPS. Each section is self-contained — copy-paste in order.

> **Phase 1** (steps 1–9) only needs the VPS itself.
> **Phase 2** (steps 10–14) needs the deployment infra files in the repo (`Dockerfile.production`, `docker-compose.production.yml`, `infra/nginx/app.zendev.us.conf`, `infra/deploy.sh`). Don't run Phase 2 until those are written.

---

## 1. Point DNS at the VPS

In your DNS provider:

```
Type: A      Host: app      Value: 2.25.165.25      TTL: 300
```

Wait until it resolves:

```bash
dig +short app.zendev.us
# should print 2.25.165.25
```

Don't move on until this returns the IP — certbot will fail otherwise.

## 2. SSH in as root

```bash
ssh root@2.25.165.25
# password login
```

All subsequent commands run on the VPS.

## 3. Update system + install OS packages

```bash
apt update && apt upgrade -y
apt install -y curl ca-certificates gnupg ufw fail2ban git nginx certbot python3-certbot-nginx unattended-upgrades
dpkg-reconfigure --priority=low unattended-upgrades   # accept defaults; enables auto security patches
```

## 4. Add 2 GB swap

```bash
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
```

## 5. Firewall — only SSH + HTTP/HTTPS

```bash
ufw allow OpenSSH
ufw allow 'Nginx Full'
ufw --force enable
ufw status
```

## 6. Install Docker (official repo)

```bash
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release; echo "$VERSION_CODENAME") stable" \
  > /etc/apt/sources.list.d/docker.list
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable --now docker
docker version && docker compose version    # sanity check
```

## 7. Harden SSH password login (fail2ban + strong password)

GitHub Actions will log in to this VPS as `root` over SSH using your VPS root password. To make password auth safe:

```bash
# fail2ban was installed in step 3 — confirm it's watching SSH
systemctl enable --now fail2ban
fail2ban-client status sshd
```

If your current root password isn't already strong, change it now:

```bash
passwd root      # pick a 20+ character random password
```

Keep this password somewhere safe — you'll paste it into GitHub next.

## 8. Add GitHub Actions secrets

In the GitHub repo: **Settings → Secrets and variables → Actions → New repository secret:**

| Secret | Value |
|---|---|
| `DEPLOY_HOST` | `2.25.165.25` |
| `DEPLOY_USER` | `root` |
| `DEPLOY_PASSWORD` | your VPS root password |

## 9. Clone the repo over HTTPS

```bash
mkdir -p /srv
git clone https://github.com/<GH_OWNER>/<GH_REPO>.git /srv/pos-platform
ls /srv/pos-platform
```

Replace `<GH_OWNER>/<GH_REPO>` with the real values.

> **Private repo?** `git clone` will prompt for username + password. Use your GitHub username and a **Personal Access Token** (PAT) with `repo:read` scope as the password. Store it persistently so future `git pull`s don't re-prompt:
> ```bash
> git -C /srv/pos-platform config credential.helper store
> git -C /srv/pos-platform pull   # enter username + PAT once; saved to ~/.git-credentials
> ```

---

## ⛔ Stop here if the deployment infra files aren't in the repo yet

Steps 10–14 expect these files to exist:
- `infra/Dockerfile.production`
- `infra/docker-compose.production.yml`
- `infra/nginx/app.zendev.us.conf`
- `infra/deploy.sh`
- `infra/backup.sh`

If they aren't in the repo, ping me to write them next.

---

## 10. Configure production environment

```bash
cd /srv/pos-platform/apps/platform
cp .env.example .env
nano .env
```

Set at minimum:

```ini
APP_NAME="POS Platform"
APP_ENV=production
APP_KEY=                              # will fill in next step
APP_DEBUG=false
APP_URL=https://app.zendev.us

DB_CONNECTION=pgsql
DB_HOST=postgres
DB_PORT=5432
DB_DATABASE=pos_platform
DB_USERNAME=pos_app
DB_PASSWORD=<STRONG_RANDOM_PASSWORD>

REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=null

SESSION_DRIVER=database
SESSION_DOMAIN=app.zendev.us
SESSION_SECURE_COOKIE=true

TRUSTED_PROXIES=*
QUEUE_CONNECTION=redis
CACHE_STORE=redis

# leave Fiserv values empty until certified
```

Generate the app key (writes into `.env`):

```bash
docker run --rm -v "$PWD":/app -w /app php:8.4-cli php artisan key:generate
```

## 11. Install the nginx site config (HTTP only for now)

```bash
cp /srv/pos-platform/infra/nginx/app.zendev.us.conf /etc/nginx/sites-available/
ln -sf /etc/nginx/sites-available/app.zendev.us.conf /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl reload nginx
```

`certbot --nginx` needs an existing `server_name app.zendev.us` block to graft TLS onto — installing the config first is what gives it one.

## 12. Get TLS cert (Let's Encrypt)

```bash
certbot --nginx -d app.zendev.us --non-interactive --agree-tos -m you@yourdomain.com --redirect
systemctl status certbot.timer    # auto-renew is already enabled
```

certbot rewrites the site file in place: adds a `:443 ssl` server block with your cert and turns the `:80` block into a 301 redirect.

## 13. First deploy

```bash
cd /srv/pos-platform
./infra/deploy.sh
```

The script builds the production image, brings up Postgres + Redis, runs migrations, then starts the app + horizon + scheduler.

## 14. Smoke check + backups cron

```bash
curl -I https://app.zendev.us                              # expect 200/302
docker compose -f infra/docker-compose.production.yml ps
```

Install daily backups:

```bash
install -m 0755 /srv/pos-platform/infra/backup.sh /usr/local/bin/pos-backup.sh
mkdir -p /var/backups/pos-platform
echo "0 3 * * * root /usr/local/bin/pos-backup.sh >> /var/log/pos-backup.log 2>&1" \
  > /etc/cron.d/pos-platform-backup
```

You're live. Subsequent deploys happen automatically when CI pushes to `main`.
