package engine.optimization;

import engine.correlation.AttackCorrelator;
import engine.datastructures.DynamicArray;
import engine.graph.DirectedGraph;
import engine.ingestion.IOCLoader;
import engine.ingestion.LogParser;
import engine.ingestion.SignatureMatcher;
import engine.ingestion.SignatureMatcher.Algorithm;
import engine.models.AttackNode;
import engine.models.IOC;
import engine.models.LogEntry;
import engine.models.SignatureMatch;

import java.io.File;

/**
 * End-to-end integration test demonstrating Phase 4 attack graph analysis
 * through Phase 5 choke-point optimization.
 *
 * <p>Pipeline Flow:
 * <pre>
 * Phase 4 DirectedGraph (AttackNodes, AttackEdges)
 *         ↓
 * Phase 5 FlowNetwork (with explicitly synthetic algorithmic capacities)
 *         ↓
 * Max Flow &amp; Min Cut &amp; Tarjan Articulation Points
 *         ↓
 * Structural Choke-Point Results
 * </pre>
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 */
public class Phase5IntegrationTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running Phase5IntegrationTest...");

        runTest("testEndToEndChokePointAnalysisPipeline", Phase5IntegrationTest::testEndToEndChokePointAnalysisPipeline);
        runTest("testSyntheticCapacityChokePointBottleneck", Phase5IntegrationTest::testSyntheticCapacityChokePointBottleneck);

        System.out.println("Phase5IntegrationTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, Runnable test) {
        totalTests++;
        try {
            test.run();
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } catch (Throwable t) {
            failedTests++;
            System.err.println("  [FAIL] " + testName + ": " + t.getMessage());
            t.printStackTrace(System.err);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Assertion failed: " + message);
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError("Assertion failed: " + message + " (expected: " + expected + ", got: " + actual + ")");
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion failed: " + message + " (expected: " + expected + ", got: " + actual + ")");
    }

    private static void testEndToEndChokePointAnalysisPipeline() {
        String logsPath = "data" + File.separator + "input" + File.separator + "logs.txt";
        String iocsPath = "data" + File.separator + "input" + File.separator + "iocs.txt";

        DynamicArray<LogEntry> logs;
        DynamicArray<IOC> iocs;
        try {
            logs = LogParser.parseFile(logsPath);
            iocs = IOCLoader.loadFile(iocsPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test telemetry feeds: " + e.getMessage(), e);
        }

        // 1. Phase 3 String Detection
        DynamicArray<SignatureMatch> matches = SignatureMatcher.matchAll(logs, iocs, Algorithm.KMP);
        assertTrue(matches.size() >= 7, "Matches should be detected");

        // 2. Phase 4 Kill-Chain Reconstruction
        AttackCorrelator correlator = new AttackCorrelator();
        DirectedGraph<AttackNode> attackGraph = correlator.correlate(matches);
        assertTrue(attackGraph.getNodeCount() >= 7, "Attack graph contains correlated nodes");
        assertTrue(attackGraph.getEdgeCount() >= 6, "Attack graph contains correlated edges");

        // 3. Phase 5 Articulation Point Analysis
        DynamicArray<String> articulationPoints = ArticulationPoints.findArticulationPoints(attackGraph);
        assertTrue(articulationPoints.size() > 0, "Reconstructed attack chain must contain critical articulation choke points");

        // 4. Phase 5 Flow Network Projection (Assigning explicit synthetic algorithmic capacity = 10)
        // Note: Real network bandwidth is absent from telemetry; synthetic integer capacity is used.
        int syntheticCapacity = 10;
        FlowNetwork flowNet = FlowNetwork.fromDirectedGraph(attackGraph, syntheticCapacity);
        assertEquals(attackGraph.getNodeCount(), flowNet.getVertexCount(), "FlowNetwork vertex count matches attack graph");

        String ingressNode = "AN-1";
        String sinkNode = "AN-" + attackGraph.getNodeCount();

        // 5. Phase 5 Maximum Flow Analysis
        MaxFlowResult maxFlowResult = MaxFlow.compute(flowNet, ingressNode, sinkNode);
        assertTrue(maxFlowResult.getMaxFlow() > 0, "Max flow from ingress to sink must be positive");

        // 6. Phase 5 Minimum Cut Analysis
        MinCutResult minCutResult = MinCut.compute(flowNet, ingressNode, sinkNode);
        assertEquals(maxFlowResult.getMaxFlow(), minCutResult.getCutValue(), "Min cut capacity must equal max flow");
        assertTrue(minCutResult.separatesSourceAndSink(), "Min cut partition must separate ingress from sink");
        assertTrue(minCutResult.getSourcePartition().size() > 0, "Source partition must not be empty");
        assertTrue(minCutResult.getSinkPartition().size() > 0, "Sink partition must not be empty");
        assertTrue(minCutResult.getCutEdges().size() > 0, "Cut edges must bridge the structural bottleneck");
    }

    private static void testSyntheticCapacityChokePointBottleneck() {
        // Build a multi-branch attack graph converging at a pivot jumpbox
        DirectedGraph<String> attackGraph = new DirectedGraph<>();
        // Ingress branches converging at jumpbox HOST-014
        attackGraph.addEdge("INGRESS-1", "HOST-014");
        attackGraph.addEdge("INGRESS-2", "HOST-014");
        // Jumpbox fanning out to internal targets
        attackGraph.addEdge("HOST-014", "DB-001");
        attackGraph.addEdge("HOST-014", "DC-002");

        // Articulation Point analysis: HOST-014 is the pivotal cut vertex
        DynamicArray<String> aps = ArticulationPoints.findArticulationPoints(attackGraph);
        assertEquals(1, aps.size(), "HOST-014 must be the unique articulation choke point");
        assertEquals("HOST-014", aps.get(0), "Articulation point must be HOST-014");

        // Flow Network with synthetic test capacities:
        // Ingress-1 capacity 5, Ingress-2 capacity 5, DB-001 capacity 20
        FlowNetwork flowNet = new FlowNetwork();
        flowNet.addEdge("INGRESS-1", "HOST-014", 5);
        flowNet.addEdge("INGRESS-2", "HOST-014", 5);
        flowNet.addEdge("HOST-014", "DB-001", 20);

        // Max flow from INGRESS-1 to DB-001 is 5
        MaxFlowResult mf1 = MaxFlow.compute(flowNet, "INGRESS-1", "DB-001");
        assertEquals(5, mf1.getMaxFlow(), "Max flow limited to Ingress-1 capacity (5)");

        // Min cut for INGRESS-1 to DB-001
        MinCutResult mc1 = MinCut.compute(flowNet, "INGRESS-1", "DB-001");
        assertEquals(5, mc1.getCutValue(), "Min cut capacity must be 5");
        assertTrue(mc1.separatesSourceAndSink(), "Min cut must separate Ingress-1 from DB-001");
    }
}
