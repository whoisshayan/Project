@echo off
setlocal
set FX_LIB=.fx\javafx-sdk-22.0.2\lib

if not exist "%FX_LIB%" (
  echo JavaFX SDK not found at %FX_LIB%.
  echo Please run: powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/22.0.2/openjfx-22.0.2_windows-x64_bin-sdk.zip' -OutFile .fx/openjfx-22.0.2_windows-x64_bin-sdk.zip -UseBasicParsing; tar -xf .fx/openjfx-22.0.2_windows-x64_bin-sdk.zip -C .fx"
  exit /b 1
)

REM Ensure Gson exists
if not exist ".fx\gson-2.10.1.jar" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar' -OutFile .fx/gson-2.10.1.jar -UseBasicParsing" || goto :eof
)

if not exist out mkdir out

echo Compiling...
javac --module-path "%FX_LIB%" --add-modules javafx.controls,javafx.graphics,javafx.media -cp .fx\gson-2.10.1.jar -d out src\*.java || goto :eof

echo Running...
java --module-path "%FX_LIB%" --add-modules javafx.controls,javafx.graphics,javafx.media -cp out;.fx\gson-2.10.1.jar Main

endlocal

