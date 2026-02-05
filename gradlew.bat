@echo off
setlocal
set DIR=%~dp0
set PROPS=%DIR%gradle\wrapper\gradle-wrapper.properties

for /f "tokens=2 delims==" %%a in ('findstr /r "^distributionUrl" "%PROPS%"') do set DIST_URL=%%a
set DIST_URL=%DIST_URL:\=%

if "%DIST_URL%"=="" (
  echo distributionUrl not found
  exit /b 1
)

set ZIP_NAME=%DIST_URL:*/=%
set DIST_DIR=%DIR%\.gradle-wrapper
set ZIP_PATH=%DIST_DIR%\%ZIP_NAME%

if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

if not exist "%ZIP_PATH%" (
  powershell -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%ZIP_PATH%'"
)

if not exist "%DIST_DIR%\gradle" (
  powershell -Command "Expand-Archive -Path '%ZIP_PATH%' -DestinationPath '%DIST_DIR%'"
  for /d %%d in ("%DIST_DIR%\gradle-*") do move "%%d" "%DIST_DIR%\gradle" >nul
)

"%DIST_DIR%\gradle\bin\gradle" %*
endlocal
