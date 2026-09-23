# Firebase Auth + Firestore emulators for the device tests (see docs/testing.md).
#   no argument: start them and keep them running until stopped
#   -Exec:       start them, run the whole device suite, then shut them down
param([switch]$Exec)

$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)

# The emulators need Java 21+; the java on PATH may be older, so prefer Android Studio's bundled JDK.
$jbr = Join-Path $env:LOCALAPPDATA 'Programs\Android Studio\jbr'
if (Test-Path (Join-Path $jbr 'bin\java.exe')) {
    $env:JAVA_HOME = $jbr
    $env:Path = "$jbr\bin;$env:Path"
}

$firebase = (Get-Command firebase -ErrorAction SilentlyContinue).Source
if (-not $firebase) { $firebase = Join-Path $env:USERPROFILE 'bin\firebase.exe' }
if (-not (Test-Path $firebase)) {
    throw "Firebase CLI not found: download https://firebase.tools/bin/win/latest to $firebase"
}

if ($Exec) {
    & $firebase emulators:exec --only auth,firestore '.\gradlew.bat :app:connectedDebugAndroidTest'
} else {
    & $firebase emulators:start --only auth,firestore
}
exit $LASTEXITCODE
