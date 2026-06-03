$ErrorActionPreference = "Stop"

$php = Get-Command php -ErrorAction Stop
$modules = & $php.Source -m
$ini = & $php.Source --ini
$extensionDir = (& $php.Source -i | Select-String -Pattern '^extension_dir =>').ToString()

Write-Host "PHP: $($php.Source)"
Write-Host $ini
Write-Host $extensionDir

if ($modules -contains "pdo_pgsql") {
    Write-Host "pdo_pgsql is enabled."
    exit 0
}

Write-Error @"
pdo_pgsql is not enabled for the active PHP CLI.

The active PHP binary is:
$($php.Source)

Install or switch to a PHP build that includes php_pdo_pgsql.dll, then add this
to the loaded php.ini:

extension=pdo_pgsql
extension=pgsql

After that, rerun:
php -m
php artisan test --env=testing
"@
