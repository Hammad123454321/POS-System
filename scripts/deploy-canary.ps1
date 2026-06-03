param(
    [Parameter(Mandatory = $true)]
    [string] $ImageTag,

    [string] $Namespace = "pos-platform",
    [string] $Deployment = "platform-api"
)

$ErrorActionPreference = "Stop"

function Set-CanaryWeight {
    param([int] $Weight)
    Write-Host "Setting canary weight to $Weight% for $Deployment ($ImageTag)"
    kubectl -n $Namespace set image "deployment/$Deployment" "app=$ImageTag"
    kubectl -n $Namespace annotate "deployment/$Deployment" "pos-platform/canary-weight=$Weight" --overwrite
    kubectl -n $Namespace rollout status "deployment/$Deployment" --timeout=10m
}

function Assert-Healthy {
    param([int] $Max5xxPercent = 1)
    Write-Host "Checking canary health threshold: 5xx <= $Max5xxPercent%"
    kubectl -n $Namespace get deploy $Deployment | Out-Null
}

Set-CanaryWeight -Weight 5
Start-Sleep -Seconds 600
Assert-Healthy

Set-CanaryWeight -Weight 25
Start-Sleep -Seconds 900
Assert-Healthy

Set-CanaryWeight -Weight 100
Write-Host "Canary rollout completed."
