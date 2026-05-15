$ErrorActionPreference = "Stop"

function Get-MazeProjectRoot {
    return Split-Path -Parent $PSScriptRoot
}

function Convert-ToForwardSlashPath {
    param([string]$PathValue)

    return ($PathValue -replace '\\', '/')
}

function Get-AndroidSdkRoot {
    $candidates = @()

    if ($env:ANDROID_SDK_ROOT) { $candidates += $env:ANDROID_SDK_ROOT }
    if ($env:ANDROID_HOME) { $candidates += $env:ANDROID_HOME }

    $projectRoot = Get-MazeProjectRoot
    $localPropertiesPath = Join-Path $projectRoot "local.properties"

    if (Test-Path $localPropertiesPath) {
        $sdkLine = Get-Content $localPropertiesPath -ErrorAction SilentlyContinue | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
        if ($sdkLine) {
            $sdkDir = ($sdkLine -replace '^sdk\.dir=', '').Trim()
            if ($sdkDir) {
                $candidates += (Convert-ToForwardSlashPath $sdkDir)
            }
        }
    }

    $candidates += @(
        "$env:LOCALAPPDATA\Android\Sdk",
        "$env:USERPROFILE\AppData\Local\Android\Sdk",
        "C:\Android\Sdk",
        "C:\Android\android-sdk"
    ) | Where-Object { $_ -and $_.Trim().Length -gt 0 }

    $sdkDir = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1
    if (-not $sdkDir) {
        throw "Android SDK not found. Open Android Studio once or run configure-android-sdk.ps1."
    }

    return (Convert-ToForwardSlashPath $sdkDir)
}

function Get-AdbPath {
    return (Join-Path (Get-AndroidSdkRoot) "platform-tools\adb.exe")
}

function Get-EmulatorPath {
    return (Join-Path (Get-AndroidSdkRoot) "emulator\emulator.exe")
}

function Get-ConnectedDeviceIds {
    $adb = Get-AdbPath
    $output = & $adb devices
    if (-not $output) {
        return @()
    }

    $deviceIds = @()
    foreach ($line in $output) {
        if ($line -match '^(?<id>\S+)\s+device$') {
            $deviceIds += $Matches.id
        }
    }

    return $deviceIds
}

function Get-ConnectedEmulatorIds {
    return (Get-ConnectedDeviceIds | Where-Object { $_ -like 'emulator-*' })
}

function Wait-ForAndroidBoot {
    param(
        [Parameter(Mandatory = $true)]
        [string]$DeviceId,
        [int]$TimeoutSeconds = 300
    )

    $adb = Get-AdbPath
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        $bootCompleted = & $adb -s $DeviceId shell getprop sys.boot_completed 2>$null
        if ($bootCompleted -match '1') {
            return
        }
        Start-Sleep -Seconds 5
    }

    throw "Timed out waiting for $DeviceId to finish booting."
}

function Launch-MazeApp {
    param(
        [Parameter(Mandatory = $true)]
        [string]$DeviceId,
        [string]$PackageName = "com.maze"
    )

    $adb = Get-AdbPath
    & $adb -s $DeviceId shell monkey -p $PackageName -c android.intent.category.LAUNCHER 1 | Out-Null
}

function Install-MazeApk {
    param(
        [Parameter(Mandatory = $true)]
        [string]$DeviceId
    )

    $projectRoot = Get-MazeProjectRoot
    & (Join-Path $projectRoot "build-and-install.ps1") -DeviceId $DeviceId
}
