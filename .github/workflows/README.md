# CI / CD

One workflow lives here:

| Workflow | Trigger | What it does |
|---|---|---|
| `ci-cd.yml` | Push or PR to `main`, manual | Backend tests (PostgreSQL + Redis service containers) + Pint code-style check. On push to `main`, SSHes to the VPS and runs `./infra/deploy.sh`. Smoke-checks `https://app.zendev.us` afterward. |

The mobile app (Flutter) is not part of CI; APKs are built manually and distributed as files.

## GitHub secrets

In the GitHub repo: **Settings → Secrets and variables → Actions → New repository secret**.

| Name | Value |
|---|---|
| `DEPLOY_HOST` | `2.25.165.25` |
| `DEPLOY_USER` | `root` |
| `DEPLOY_PASSWORD` | your VPS root password |
| `DEPLOY_PORT` *(optional)* | `22` |

> Password auth is more brute-forceable than SSH keys. Make sure **fail2ban** is enabled on the VPS (it is, per the [VPS setup runbook](../../infra/VPS-SETUP.md)) and use a strong password.

## Add a `production` environment

**Settings → Environments → New environment → name: `production`**.
Optionally check "Required reviewers" so deploys need a click before running.

## Notes

- Tests use ephemeral Postgres 16 + Redis 7 service containers, same versions as production.
- The deploy step assumes `/srv/pos-platform/infra/deploy.sh` exists on the VPS — that script comes from the deployment plan.
- PRs run tests but never deploy.
