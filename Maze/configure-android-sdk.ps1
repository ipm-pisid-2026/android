$ErrorActionPreference = "Stop"

function Convert-ToGradlePath {
    param([string]$PathValue)
    return ($PathValue -replace '\\', '/')
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$localPropertiesPath = Join-Path $projectRoot "local.properties"

if (Test-Path $localPropertiesPath) {
    $existing = Get-Content $localPropertiesPath -ErrorAction SilentlyContinue | Select-String '^sdk\.dir='
    if ($existing) {
        Write-Host "local.properties already configured."
        return
    }
}

$candidates = @()
if ($env:ANDROID_SDK_ROOT) { $candidates += $env:ANDROID_SDK_ROOT }
if ($env:ANDROID_HOME) { $candidates += $env:ANDROID_HOME }

$candidates += @(
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:USERPROFILE\AppData\Local\Android\Sdk",
    "C:\Android\Sdk",
    "C:\Android\android-sdk"
) | Where-Object { $_ -and $_.Trim().Length -gt 0 }

$sdkDir = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1

if (-not $sdkDir) {
    throw "Android SDK not found. Set ANDROID_SDK_ROOT or ANDROID_HOME, or install the Android SDK in a standard Windows location."
}

$sdkDir = Convert-ToGradlePath $sdkDir
Set-Content -Path $localPropertiesPath -Value "sdk.dir=$sdkDir" -Encoding ASCII
Write-Host "Created local.properties with sdk.dir=$sdkDir"
