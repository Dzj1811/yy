@echo off
setlocal ENABLEDELAYEDEXPANSION

REM One-click Windows installer build for ClinicManager (includes runtime)
REM Prerequisites:
REM 1) JDK 17 installed (jpackage available)
REM 2) Maven available in PATH

set APP_NAME=ClinicManager
set APP_VERSION=0.1.0
set MAIN_CLASS=com.yourname.clinic.Launcher
set MAIN_JAR=clinic-manager-0.1.0.jar
set ICON_FILE=%~dp0assets\imagetb.ico

if exist "%~dp0pom.xml" (
  set "PROJECT_DIR=%~dp0"
) else if exist "%cd%\pom.xml" (
  set "PROJECT_DIR=%cd%\"
) else (
  echo [ERROR] pom.xml not found.
  echo         Place this script in the Maven project root, or run it from the project root.
  exit /b 1
)

set "TARGET_DIR=%PROJECT_DIR%target"
set "DIST_DIR=%PROJECT_DIR%dist"
set "FALLBACK_IMAGE_DIR=%PROJECT_DIR%dist-image"
set "INPUT_DIR=%TARGET_DIR%\jpackage-input"

where mvn >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Maven not found in PATH.
  exit /b 1
)

where jpackage >nul 2>nul
if errorlevel 1 (
  echo [ERROR] jpackage not found in PATH. Please use JDK 17+.
  exit /b 1
)

if not exist "%ICON_FILE%" (
  echo [ERROR] Icon file not found: %ICON_FILE%
  echo         Please make sure assets\imagetb.ico exists beside this bat file.
  exit /b 1
)

echo [1/4] Build jar...
call mvn -f "%PROJECT_DIR%pom.xml" clean package -DskipTests
if errorlevel 1 (
  echo [ERROR] Maven build failed.
  exit /b 1
)

echo [2/4] Collect runtime dependencies for jpackage input...
if exist "%INPUT_DIR%" rmdir /s /q "%INPUT_DIR%"
mkdir "%INPUT_DIR%"

call mvn -q -f "%PROJECT_DIR%pom.xml" dependency:copy-dependencies -DoutputDirectory="%INPUT_DIR%" -DincludeScope=runtime
if errorlevel 1 (
  echo [ERROR] Copy dependencies failed.
  exit /b 1
)

if not exist "%TARGET_DIR%\%MAIN_JAR%" (
  echo [ERROR] %TARGET_DIR%\%MAIN_JAR% not found.
  echo         Check artifactId/version and package output.
  exit /b 1
)
copy /y "%TARGET_DIR%\%MAIN_JAR%" "%INPUT_DIR%\" >nul

echo [3/4] Try create EXE installer with bundled runtime...
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

jpackage ^
  --name %APP_NAME% ^
  --app-version %APP_VERSION% ^
  --type exe ^
  --input "%INPUT_DIR%" ^
  --main-jar %MAIN_JAR% ^
  --main-class %MAIN_CLASS% ^
  --dest %DIST_DIR% ^
  --icon "%ICON_FILE%" ^
  --win-shortcut ^
  --win-menu

if errorlevel 1 (
  echo [WARN] EXE packaging failed. Trying app-image fallback...
  goto APP_IMAGE_FALLBACK
)

dir /b "%DIST_DIR%\\*.exe" >nul 2>nul
if errorlevel 1 (
  echo [WARN] jpackage finished but no EXE found in %DIST_DIR%.
  echo [INFO] On Windows, EXE generation may require WiX Toolset.
  echo [INFO] Trying app-image fallback...
  goto APP_IMAGE_FALLBACK
)

echo [4/4] Done.
echo EXE installer generated under: %DIST_DIR%
endlocal
exit /b 0

:APP_IMAGE_FALLBACK
if not exist "%FALLBACK_IMAGE_DIR%" mkdir "%FALLBACK_IMAGE_DIR%"
jpackage ^
  --name %APP_NAME% ^
  --app-version %APP_VERSION% ^
  --type app-image ^
  --input "%INPUT_DIR%" ^
  --main-jar %MAIN_JAR% ^
  --main-class %MAIN_CLASS% ^
  --dest %FALLBACK_IMAGE_DIR% ^
  --icon "%ICON_FILE%"

if errorlevel 1 (
  echo [ERROR] app-image fallback also failed.
  exit /b 1
)

echo [4/4] Done with fallback.
echo App-image generated under: %FALLBACK_IMAGE_DIR%
echo TIP: Install WiX Toolset if you need EXE/MSI installer output.
endlocal
