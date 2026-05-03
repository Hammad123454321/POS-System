param(
    [ValidateSet('php', 'composer', 'artisan')]
    [string]$Tool = 'php',

    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Arguments
)

function Get-PlatformPhpRoot {
    $phpPackage = Get-ChildItem "$env:LOCALAPPDATA\Microsoft\WinGet\Packages" -Directory |
        Where-Object { $_.Name -like 'PHP.PHP.NTS.8.4*' } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($null -eq $phpPackage) {
        throw 'PHP 8.4 NTS is not installed. Install it with winget before running backend commands.'
    }

    return $phpPackage.FullName
}

function Get-PlatformPhpCommandPrefix {
    $phpRoot = Get-PlatformPhpRoot
    $extensionDir = Join-Path $phpRoot 'ext'

    return @{
        Php = Join-Path $phpRoot 'php.exe'
        Composer = Join-Path $env:USERPROFILE '.config\herd-lite\bin\composer.phar'
        IniArgs = @(
            '-d', "extension_dir=$extensionDir",
            '-d', 'extension=php_curl.dll',
            '-d', 'extension=php_fileinfo.dll',
            '-d', 'extension=php_intl.dll',
            '-d', 'extension=php_mbstring.dll',
            '-d', 'extension=php_mysqli.dll',
            '-d', 'extension=php_openssl.dll',
            '-d', 'extension=php_pdo_pgsql.dll',
            '-d', 'extension=php_pgsql.dll',
            '-d', 'extension=php_pdo_sqlite.dll',
            '-d', 'extension=php_soap.dll',
            '-d', 'extension=php_sockets.dll',
            '-d', 'extension=php_sqlite3.dll',
            '-d', 'extension=php_zip.dll'
        )
    }
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$platformDir = Join-Path $repoRoot 'apps/platform'
$runtime = Get-PlatformPhpCommandPrefix

Push-Location $platformDir
try {
    switch ($Tool) {
        'composer' {
            & $runtime.Php @($runtime.IniArgs + $runtime.Composer + $Arguments)
        }
        'artisan' {
            & $runtime.Php @($runtime.IniArgs + 'artisan' + $Arguments)
        }
        default {
            & $runtime.Php @($runtime.IniArgs + $Arguments)
        }
    }

    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
