@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Threat Intelligence Engine - Build
echo ========================================

rem Navigate to project root directory
cd /d "%~dp0.."

rem Check JAVA_HOME fallback
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-17" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    )
)

rem Verify Java compiler is present
where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    ) else (
        echo [ERROR] javac compiler not found on PATH or JAVA_HOME. Please install JDK 17.
        exit /b 1
    )
)

rem Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [INFO] Maven detected. Building with Maven...
    mvn clean package
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Maven build failed.
        exit /b 1
    )
    echo [SUCCESS] Maven build successful.
    exit /b 0
)

echo [INFO] Maven is not detected on PATH.
echo [INFO] Compiling directly with javac (Java 17)...

rem Ensure target output directories exist
if not exist "target\classes" mkdir "target\classes"

rem Clean previous sources file
if exist "target\sources.txt" del "target\sources.txt"

rem Gather all Java source files as forward-slash relative paths
for /r "src\main\java" %%F in (*.java) do (
    set "FULLPATH=%%F"
    set "RELPATH=!FULLPATH:%CD%\=!"
    set "RELPATH=!RELPATH:\=/!"
    echo !RELPATH!>> "target\sources.txt"
)

rem Compile using javac
javac -d "target\classes" @"target\sources.txt"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] javac compilation failed.
    exit /b 1
)

rem Locate jar tool
set "JAR_CMD=jar"
where jar >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jar.exe" (
        set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
    )
)

rem Package into JAR file
"%JAR_CMD%" --create --file "target\kill-chain-correlation-engine-1.0.0.jar" --main-class engine.Main -C "target\classes" . >nul 2>&1
if exist "target\kill-chain-correlation-engine-1.0.0.jar" (
    echo [SUCCESS] Packaged target\kill-chain-correlation-engine-1.0.0.jar
) else (
    echo [INFO] jar tool not available; compiled classes reside in target\classes.
)

echo [SUCCESS] Build completed successfully.
exit /b 0
