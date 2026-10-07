@echo off
setlocal

rem Navigate to project root directory
cd /d "%~dp0.."

rem Check for java runtime
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] java runtime not found on PATH.
    exit /b 1
)

if exist "target\kill-chain-correlation-engine-1.0.0.jar" (
    java -jar "target\kill-chain-correlation-engine-1.0.0.jar" %*
    exit /b %ERRORLEVEL%
)

if exist "target\classes\engine\Main.class" (
    java -cp "target\classes" engine.Main %*
    exit /b %ERRORLEVEL%
)

echo [ERROR] Application is not compiled. Please run scripts\build.bat first.
exit /b 1
