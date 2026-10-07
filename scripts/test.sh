#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo "Threat Intelligence Engine - Tests"
echo "========================================"

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Check javac and java
if ! command -v javac &> /dev/null; then
    echo "[ERROR] javac compiler not found on PATH."
    exit 1
fi
if ! command -v java &> /dev/null; then
    echo "[ERROR] java runtime not found on PATH."
    exit 1
fi

mkdir -p target/classes target/test-classes

# Compile main sources
find src/main/java -name "*.java" > target/main_sources.txt
javac -d target/classes @target/main_sources.txt

# Compile test sources
find src/test/java -name "*.java" > target/test_sources.txt
javac -d target/test-classes -cp target/classes @target/test_sources.txt

echo "[INFO] Executing Phase 2 Test Suites..."
echo "----------------------------------------"

java -cp "target/classes:target/test-classes" engine.datastructures.DynamicArrayTest
java -cp "target/classes:target/test-classes" engine.datastructures.LinkedListTest
java -cp "target/classes:target/test-classes" engine.datastructures.StackTest
java -cp "target/classes:target/test-classes" engine.datastructures.QueueTest

echo ""
echo "[INFO] Executing Phase 3 Test Suites..."
echo "----------------------------------------"

java -cp "target/classes:target/test-classes" engine.strings.KMPTest
java -cp "target/classes:target/test-classes" engine.strings.ZAlgorithmTest
java -cp "target/classes:target/test-classes" engine.strings.RabinKarpTest
java -cp "target/classes:target/test-classes" engine.ingestion.LogParserTest
java -cp "target/classes:target/test-classes" engine.ingestion.IOCLoaderTest
java -cp "target/classes:target/test-classes" engine.integration.SignatureMatchingIntegrationTest

echo ""
echo "[INFO] Executing Phase 4 Test Suites..."
echo "----------------------------------------"

java -cp "target/classes:target/test-classes" engine.graph.DirectedGraphTest
java -cp "target/classes:target/test-classes" engine.graph.DFSTest
java -cp "target/classes:target/test-classes" engine.graph.BFSTest
java -cp "target/classes:target/test-classes" engine.graph.PathFinderTest
java -cp "target/classes:target/test-classes" engine.correlation.KillChainTest
java -cp "target/classes:target/test-classes" engine.correlation.AttackCorrelatorTest

echo ""
echo "[INFO] Executing Phase 5 Test Suites..."
echo "----------------------------------------"

java -cp "target/classes:target/test-classes" engine.optimization.MaxFlowTest
java -cp "target/classes:target/test-classes" engine.optimization.MinCutTest
java -cp "target/classes:target/test-classes" engine.optimization.ArticulationPointsTest
java -cp "target/classes:target/test-classes" engine.optimization.Phase5IntegrationTest

echo ""
echo "[INFO] Executing Phase 6 Test Suites..."
echo "----------------------------------------"

java -cp "target/classes:target/test-classes" engine.monitoring.VertexCoverTest
java -cp "target/classes:target/test-classes" engine.monitoring.MonitoringPlacementTest

echo ""
echo "[INFO] Executing Phase 7 Test Suites..."
echo "----------------------------------------"

java -cp "target/classes:target/test-classes" engine.alerting.BinaryHeapTest
java -cp "target/classes:target/test-classes" engine.alerting.AlertRouterTest
java -cp "target/classes:target/test-classes" engine.alerting.AlertManagerTest
java -cp "target/classes:target/test-classes" engine.integration.Phase6Phase7IntegrationTest

echo "----------------------------------------"
echo "[SUCCESS] All Phase 2, 3, 4, 5, 6, and 7 test suites passed successfully!"
exit 0


