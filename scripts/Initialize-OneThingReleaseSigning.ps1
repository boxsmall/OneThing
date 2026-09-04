[CmdletBinding()]
param(
    [string]$SigningDirectory = (Join-Path ([Environment]::GetFolderPath('UserProfile')) '.onething-signing'),
    [string]$KeyAlias = 'onething-release',
    [int]$ValidityDays = 10000
)

$ErrorActionPreference = 'Stop'

if ($ValidityDays -lt 3650) {
    throw 'ValidityDays must be at least 3650 days.'
}

$resolvedSigningDirectory = [System.IO.Path]::GetFullPath($SigningDirectory)
$keystorePath = Join-Path $resolvedSigningDirectory 'onething-release.jks'
$propertiesPath = Join-Path $resolvedSigningDirectory 'keystore.properties'

if ((Test-Path -LiteralPath $keystorePath) -or (Test-Path -LiteralPath $propertiesPath)) {
    throw "Signing material already exists at $resolvedSigningDirectory. Refusing to overwrite it."
}

$keytoolCandidates = @()
if ($env:JAVA_HOME) {
    $keytoolCandidates += Join-Path $env:JAVA_HOME 'bin\keytool.exe'
}
$keytoolCandidates += 'C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe'
$keytoolCandidates = $keytoolCandidates | Where-Object { Test-Path -LiteralPath $_ }
$keytoolPath = $keytoolCandidates | Select-Object -First 1
if (-not $keytoolPath) {
    throw 'keytool.exe was not found. Install Android Studio JBR or configure JAVA_HOME.'
}

$randomBytes = New-Object byte[] 32
$randomGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$randomGenerator.GetBytes($randomBytes)
$randomGenerator.Dispose()
$generatedPassword = [Convert]::ToBase64String($randomBytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')

New-Item -ItemType Directory -Path $resolvedSigningDirectory | Out-Null
$temporaryPasswordVariable = 'ONETHING_GENERATED_KEY_PASSWORD'
[Environment]::SetEnvironmentVariable($temporaryPasswordVariable, $generatedPassword, 'Process')

try {
    & $keytoolPath `
        -genkeypair `
        -storetype JKS `
        -keystore $keystorePath `
        -storepass:env $temporaryPasswordVariable `
        -keypass:env $temporaryPasswordVariable `
        -alias $KeyAlias `
        -keyalg RSA `
        -keysize 4096 `
        -sigalg SHA256withRSA `
        -validity $ValidityDays `
        -dname 'CN=OneThing Local Release, OU=BoxSmall, O=BoxSmall, L=Local, ST=Local, C=CN'
    if ($LASTEXITCODE -ne 0) {
        throw "keytool failed with exit code $LASTEXITCODE."
    }

    $normalizedKeystorePath = $keystorePath.Replace('\', '/')
    $properties = @(
        "storeFile=$normalizedKeystorePath"
        "storePassword=$generatedPassword"
        "keyAlias=$KeyAlias"
        "keyPassword=$generatedPassword"
    )
    [System.IO.File]::WriteAllLines(
        $propertiesPath,
        $properties,
        [System.Text.UTF8Encoding]::new($false)
    )
} finally {
    [Environment]::SetEnvironmentVariable($temporaryPasswordVariable, $null, 'Process')
    [Array]::Clear($randomBytes, 0, $randomBytes.Length)
    $generatedPassword = $null
}

Write-Output "Release signing material created outside the repository: $resolvedSigningDirectory"
Write-Output 'Back up both files together. Losing either the keystore or password prevents future signed upgrades.'
