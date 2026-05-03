param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$TestPath
)

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$posDir = Join-Path $repoRoot "apps/pos"
$buildDir = Join-Path $posDir "build"
$unitAssetsDir = Join-Path $buildDir "unit_test_assets"
$nativeAssetsDir = Join-Path $buildDir "native_assets"

Push-Location $posDir
try {
    Get-CimInstance Win32_Process |
        Where-Object {
            ($_.Name -in @('dart.exe', 'flutter.bat', 'flutter.exe')) -and
            $_.CommandLine -like "*$posDir*"
        } |
        ForEach-Object {
            Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
        }

    if (Test-Path $unitAssetsDir) {
        try {
            Remove-Item -LiteralPath $unitAssetsDir -Recurse -Force -ErrorAction Stop
        }
        catch [System.IO.DirectoryNotFoundException], [System.Management.Automation.ItemNotFoundException], [System.ComponentModel.Win32Exception] {
        }
    }

    if (Test-Path $nativeAssetsDir) {
        try {
            Remove-Item -LiteralPath $nativeAssetsDir -Recurse -Force -ErrorAction Stop
        }
        catch [System.IO.DirectoryNotFoundException], [System.Management.Automation.ItemNotFoundException], [System.ComponentModel.Win32Exception] {
        }
    }

    flutter test $TestPath
}
finally {
    Pop-Location
}
