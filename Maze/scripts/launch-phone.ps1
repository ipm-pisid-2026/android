param(
    [string]$DeviceId = "",
    [int]$TimeoutSeconds = 300,
    [switch]$LaunchApp
)

$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "android-common.ps1")

$projectRoot = Get-MazeProjectRoot
& (Join-Path $projectRoot "configure-android-sdk.ps1")

$deviceIds = Get-ConnectedDeviceIds | Where-Object { $_ -notlike 'emulator-*' }

if (-not $DeviceId) {
    if ($deviceIds.Count -eq 0) {
        throw "No physical Android device found. Connect a phone with USB debugging enabled."
    }

    if ($deviceIds.Count -gt 1) {
        throw "More than one physical device is connected. Pass -DeviceId. Available devices: $($deviceIds -join ', ')"
    }

    $DeviceId = $deviceIds[0]
}

Wait-ForAndroidBoot -DeviceId $DeviceId -TimeoutSeconds $TimeoutSeconds
Install-MazeApk -DeviceId $DeviceId

if ($LaunchApp) {
    Launch-MazeApp -DeviceId $DeviceId
}

Write-Host "Device ready: $DeviceId"
Write-Host "APK installed and app launched."
