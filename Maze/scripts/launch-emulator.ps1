param(
    [string]$AvdName = "",
    [int]$TimeoutSeconds = 300,
    [switch]$LaunchApp
)

$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "android-common.ps1")

$projectRoot = Get-MazeProjectRoot
& (Join-Path $projectRoot "configure-android-sdk.ps1")

$emulatorPath = Get-EmulatorPath

if (-not $AvdName) {
    $avdList = & $emulatorPath -list-avds
    if (-not $avdList) {
        throw "No AVDs found. Create one in Android Studio > Device Manager first."
    }

    if ($avdList.Count -gt 1) {
        throw "More than one AVD is available. Pass -AvdName. Available AVDs: $($avdList -join ', ')"
    }

    $AvdName = $avdList[0]
}

Write-Host "Starting emulator: $AvdName"
$process = Start-Process -FilePath $emulatorPath -ArgumentList @('-avd', $AvdName, '-netdelay', 'none', '-netspeed', 'full') -PassThru

$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
$emulatorId = $null

while ((Get-Date) -lt $deadline) {
    $emulatorIds = Get-ConnectedEmulatorIds
    if ($emulatorIds.Count -gt 0) {
        $emulatorId = $emulatorIds[0]
        break
    }
    Start-Sleep -Seconds 5
}

if (-not $emulatorId) {
    throw "Emulator did not connect in time. Check the Android Emulator window."
}

Wait-ForAndroidBoot -DeviceId $emulatorId -TimeoutSeconds $TimeoutSeconds
Install-MazeApk -DeviceId $emulatorId

if ($LaunchApp) {
    Launch-MazeApp -DeviceId $emulatorId
}

Write-Host "Emulator ready: $emulatorId"
Write-Host "APK installed and app launched."
