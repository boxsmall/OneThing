[CmdletBinding()]
param(
    [string]$ApkPath = (Join-Path $PSScriptRoot '..\app\build\outputs\apk\release\app-release.apk')
)

$ErrorActionPreference = 'Stop'
$resolvedApkPath = [System.IO.Path]::GetFullPath($ApkPath)
if (-not (Test-Path -LiteralPath $resolvedApkPath -PathType Leaf)) {
    throw "Signed Release APK not found: $resolvedApkPath"
}

$androidSdkPath = if ($env:ANDROID_HOME) {
    $env:ANDROID_HOME
} elseif ($env:ANDROID_SDK_ROOT) {
    $env:ANDROID_SDK_ROOT
} else {
    Join-Path ([Environment]::GetFolderPath('LocalApplicationData')) 'Android\Sdk'
}
$buildToolsRoot = Join-Path $androidSdkPath 'build-tools'
$apksignerPath = Get-ChildItem -LiteralPath $buildToolsRoot -Directory |
    Sort-Object Name -Descending |
    ForEach-Object { Join-Path $_.FullName 'apksigner.bat' } |
    Where-Object { Test-Path -LiteralPath $_ } |
    Select-Object -First 1
if (-not $apksignerPath) {
    throw "apksigner.bat was not found below $buildToolsRoot"
}

& $apksignerPath verify --verbose --print-certs $resolvedApkPath
if ($LASTEXITCODE -ne 0) {
    throw "APK signature verification failed with exit code $LASTEXITCODE."
}

$apk = Get-Item -LiteralPath $resolvedApkPath
$hash = Get-FileHash -LiteralPath $resolvedApkPath -Algorithm SHA256
Write-Output "APK: $($apk.FullName)"
Write-Output "Bytes: $($apk.Length)"
Write-Output "SHA-256: $($hash.Hash)"
