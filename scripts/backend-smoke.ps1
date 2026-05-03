param()

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$platformDir = Join-Path $repoRoot "apps/platform"
$phpPackage = Get-ChildItem "$env:LOCALAPPDATA\Microsoft\WinGet\Packages" -Directory |
    Where-Object { $_.Name -like 'PHP.PHP.NTS.8.4*' } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if ($null -eq $phpPackage) {
    throw 'PHP 8.4 NTS is not installed.'
}

$phpExe = Join-Path $phpPackage.FullName 'php.exe'
$extensionDir = Join-Path $phpPackage.FullName 'ext'
$stdoutLog = Join-Path $platformDir 'storage\logs\backend-smoke-stdout.log'
$stderrLog = Join-Path $platformDir 'storage\logs\backend-smoke-stderr.log'

if (Test-Path $stdoutLog) {
    Remove-Item $stdoutLog -Force
}

if (Test-Path $stderrLog) {
    Remove-Item $stderrLog -Force
}

$phpArgs = @(
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
    '-d', 'extension=php_zip.dll',
    '-S',
    '127.0.0.1:8000',
    'public/index.php'
)

$server = Start-Process -FilePath $phpExe -ArgumentList $phpArgs -WorkingDirectory $platformDir -WindowStyle Hidden -RedirectStandardOutput $stdoutLog -RedirectStandardError $stderrLog -PassThru

try {
    $response = $null

    for ($attempt = 0; $attempt -lt 15; $attempt++) {
        Start-Sleep -Seconds 2

        try {
            $response = Invoke-WebRequest -UseBasicParsing http://127.0.0.1:8000/up
            break
        }
        catch {
            if ($server.HasExited) {
                throw
            }
        }
    }

    if ($null -eq $response) {
        throw 'Backend smoke check did not receive a response from http://127.0.0.1:8000/up.'
    }

    $response | Select-Object StatusCode, Content
}
finally {
    if (!$server.HasExited) {
        Stop-Process -Id $server.Id -Force
    }
}
