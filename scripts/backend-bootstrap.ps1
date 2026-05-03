param()

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$composeFile = Join-Path $repoRoot "infra/docker-compose.yml"
$platformDir = Join-Path $repoRoot "apps/platform"
$platformCli = Join-Path $PSScriptRoot "platform-cli.ps1"

docker compose -f $composeFile up -d postgres redis

Push-Location $platformDir
try {
    if (!(Test-Path ".env")) {
        Copy-Item ".env.example" ".env"
    }

    npm install
}
finally {
    Pop-Location
}

powershell -ExecutionPolicy Bypass -File $platformCli composer install --ignore-platform-req=ext-pcntl --ignore-platform-req=ext-posix
powershell -ExecutionPolicy Bypass -File $platformCli artisan key:generate --ansi
powershell -ExecutionPolicy Bypass -File $platformCli artisan migrate:fresh --env=testing --force
