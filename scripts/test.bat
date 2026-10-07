@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Threat Intelligence Engine - Tests
echo ========================================

rem Navigate to project root directory
cd /d "%~dp0.."

rem Verify javac and java
where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] javac compiler not found on PATH.
    exit /b 1
)
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] java runtime not found on PATH.
    exit /b 1
)

rem Ensure target directories exist
if not exist "target\classes" mkdir "target\classes"
if not exist "target\test-classes" mkdir "target\test-classes"

rem Compile main sources
if exist "target\main_sources.txt" del "target\main_sources.txt"
for /r "src\main\java" %%F in (*.java) do (
    set "FULLPATH=%%F"
    set "RELPATH=!FULLPATH:%CD%\=!"
    set "RELPATH=!RELPATH:\=/!"
    echo !RELPATH!>> "target\main_sources.txt"
)
javac -d "target\classes" @"target\main_sources.txt"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation of main sources failed.
    exit /b 1
)

rem Compile test sources
if exist "target\test_sources.txt" del "target\test_sources.txt"
for /r "src\test\java" %%F in (*.java) do (
    set "FULLPATH=%%F"
    set "RELPATH=!FULLPATH:%CD%\=!"
    set "RELPATH=!RELPATH:\=/!"
    echo !RELPATH!>> "target\test_sources.txt"
)
javac -d "target\test-classes" -cp "target\classes" @"target\test_sources.txt"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation of test sources failed.
    exit /b 1
)

echo [INFO] Executing Phase 2 Test Suites...
echo ----------------------------------------

java -cp "target\classes;target\test-classes" engine.datastructures.DynamicArrayTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] DynamicArrayTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.datastructures.LinkedListTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] LinkedListTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.datastructures.StackTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] StackTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.datastructures.QueueTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] QueueTest failed.
    exit /b 1
)

echo.
echo [INFO] Executing Phase 3 Test Suites...
echo ----------------------------------------

java -cp "target\classes;target\test-classes" engine.strings.KMPTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] KMPTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.strings.ZAlgorithmTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] ZAlgorithmTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.strings.RabinKarpTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] RabinKarpTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.ingestion.LogParserTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] LogParserTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.ingestion.IOCLoaderTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] IOCLoaderTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.integration.SignatureMatchingIntegrationTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] SignatureMatchingIntegrationTest failed.
    exit /b 1
)

echo.
echo [INFO] Executing Phase 4 Test Suites...
echo ----------------------------------------

java -cp "target\classes;target\test-classes" engine.graph.DirectedGraphTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] DirectedGraphTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.graph.DFSTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] DFSTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.graph.BFSTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] BFSTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.graph.PathFinderTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] PathFinderTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.correlation.KillChainTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] KillChainTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.correlation.AttackCorrelatorTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] AttackCorrelatorTest failed.
    exit /b 1
)

echo.
echo [INFO] Executing Phase 5 Test Suites...
echo ----------------------------------------

java -cp "target\classes;target\test-classes" engine.optimization.MaxFlowTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] MaxFlowTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.optimization.MinCutTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] MinCutTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.optimization.ArticulationPointsTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] ArticulationPointsTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.optimization.Phase5IntegrationTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Phase5IntegrationTest failed.
    exit /b 1
)

echo.
echo [INFO] Executing Phase 6 Test Suites...
echo ----------------------------------------

java -cp "target\classes;target\test-classes" engine.monitoring.VertexCoverTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] VertexCoverTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.monitoring.MonitoringPlacementTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] MonitoringPlacementTest failed.
    exit /b 1
)

echo.
echo [INFO] Executing Phase 7 Test Suites...
echo ----------------------------------------

java -cp "target\classes;target\test-classes" engine.alerting.BinaryHeapTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] BinaryHeapTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.alerting.AlertRouterTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] AlertRouterTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.alerting.AlertManagerTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] AlertManagerTest failed.
    exit /b 1
)

java -cp "target\classes;target\test-classes" engine.integration.Phase6Phase7IntegrationTest
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Phase6Phase7IntegrationTest failed.
    exit /b 1
)

echo ----------------------------------------
echo [SUCCESS] All Phase 2, 3, 4, 5, 6, and 7 test suites passed successfully!
exit /b 0


