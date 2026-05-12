param(
    [string]$DeviceId = "",
    [string]$BuildType = "Debug"
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

& (Join-Path $projectRoot "configure-android-sdk.ps1")

$gradleCommand = if ($IsWindows) { ".\gradlew.bat" } else { "./gradlew" }
& $gradleCommand "assemble$BuildType"

$apkPath = Join-Path $projectRoot "app\build\outputs\apk\$($BuildType.ToLower())\app-$($BuildType.ToLower()).apk"

if (-not (Test-Path $apkPath)) {
    throw "APK not found at $apkPath"
}

$adb = "adb"
if ($DeviceId) {
    & $adb -s $DeviceId install -r $apkPath
} else {
    & $adb install -r $apkPath
}

Write-Host "APK installed successfully: $apkPath"
