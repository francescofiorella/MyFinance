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
    throw "Firebase CLI not found: download firebase-tools-win.exe of the version pinned in .github/workflows/ci.yml from https://github.com/firebase/firebase-tools/releases to $firebase"
}

# Stopping a run from Android Studio ends this script but can leave the emulators running and
# holding their ports, so clear any left from an earlier run. Only the Firebase CLI and the
# Firestore emulator's java process are matched, never whatever else may use port 8080.
$leftovers = Get-CimInstance Win32_Process | Where-Object {
    ($_.Name -eq 'java.exe' -and $_.CommandLine -match 'cloud-firestore-emulator') -or
    ($_.Name -eq 'firebase.exe' -and $_.CommandLine -match 'emulators:')
}
foreach ($process in $leftovers) {
    Write-Host "Stopping a leftover emulator process ($($process.Name), pid $($process.ProcessId))"
    Stop-Process -Id $process.ProcessId -Force -ErrorAction SilentlyContinue
}

if ($Exec) {
    & $firebase emulators:exec --only auth,firestore '.\gradlew.bat :app:connectedDebugAndroidTest'
} else {
    & $firebase emulators:start --only auth,firestore
}
exit $LASTEXITCODE
