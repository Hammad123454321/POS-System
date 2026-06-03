param(
    [string] $PrimaryRegion = "us-east-1",
    [string] $StandbyRegion = "us-west-2",
    [string] $HostedZoneId,
    [string] $RecordName
)

$ErrorActionPreference = "Stop"

Write-Host "POS Platform DR failover checklist"
Write-Host "Primary: $PrimaryRegion"
Write-Host "Standby: $StandbyRegion"

$checks = @(
    "Primary app health checks failed for at least 120 seconds",
    "Primary database write probe failed",
    "Primary app reachability failed",
    "Manual ops approval recorded",
    "Standby PostgreSQL replica lag is within RPO",
    "Standby Laravel stack is warm and ready",
    "Payment/stored-value worker restart order approved"
)

foreach ($check in $checks) {
    Write-Host "[ ] $check"
}

Write-Host ""
Write-Host "After approval:"
Write-Host "1. Promote standby PostgreSQL in $StandbyRegion."
Write-Host "2. Set old primary to return 503 Retry-After: 30 if reachable."
Write-Host "3. Cut Route 53 failover alias record with 30-second TTL."
Write-Host "4. Resume workers: payments, stored value, sync, reporting."
Write-Host "5. Monitor POS bootstrap/sync recovery and DLQ thresholds."

if ($HostedZoneId -and $RecordName) {
    Write-Host ""
    Write-Host "Route 53 target supplied. Prepare a change batch before executing DNS cutover."
    Write-Host "HostedZoneId: $HostedZoneId"
    Write-Host "RecordName: $RecordName"
}
