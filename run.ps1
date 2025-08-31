$ErrorActionPreference = 'Stop'

$fxLib = Join-Path (Resolve-Path '.fx/javafx-sdk-22.0.2') 'lib'
if (-not (Test-Path $fxLib)) {
  Write-Host "JavaFX SDK not found at $fxLib" -ForegroundColor Yellow
  Write-Host "Downloading..."
  New-Item -ItemType Directory -Path .fx -Force | Out-Null
  Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/22.0.2/openjfx-22.0.2_windows-x64_bin-sdk.zip' -OutFile '.fx/openjfx-22.0.2_windows-x64_bin-sdk.zip' -UseBasicParsing
  tar -xf '.fx/openjfx-22.0.2_windows-x64_bin-sdk.zip' -C '.fx'
}

# Ensure Gson is present
$gsonJar = Join-Path (Resolve-Path '.fx') 'gson-2.10.1.jar'
if (-not (Test-Path $gsonJar)) {
  Write-Host "Gson library not found. Downloading..."
  New-Item -ItemType Directory -Path .fx -Force | Out-Null
  Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar' -OutFile $gsonJar -UseBasicParsing
}

if (-not (Test-Path 'out')) { New-Item -ItemType Directory -Path 'out' | Out-Null }

Write-Host "Compiling..."
javac --module-path $fxLib --add-modules javafx.controls,javafx.graphics,javafx.media -cp $gsonJar -d out src/*.java

Write-Host "Running..."
java --module-path $fxLib --add-modules javafx.controls,javafx.graphics,javafx.media -cp "out;$gsonJar" Main

