package engine.correlation;

import engine.datastructures.DynamicArray;
import engine.graph.DirectedGraph;
import engine.graph.GraphNode;
import engine.graph.PathFinder;
import engine.ingestion.IOCLoader;
import engine.ingestion.LogParser;
import engine.ingestion.SignatureMatcher;
import engine.ingestion.SignatureMatcher.Algorithm;
import engine.models.AttackEdge;
import engine.models.AttackNode;
import engine.models.AttackPath;
import engine.models.IOC;
import engine.models.LogEntry;
import engine.models.SignatureMatch;

import java.io.File;

/**
 * Standalone test suite for {@link AttackCorrelator}.
 *
 * <p>Verifies Phase 4 correlation logic, attack node and edge creation,
 * and end-to-end reconstruction from real Phase 3 telemetry feeds.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 */
public class AttackCorrelatorTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running AttackCorrelatorTest...");

        runTest("testAttackNodeCreation", AttackCorrelatorTest::testAttackNodeCreation);
        runTest("testAttackEdgeCreation", AttackCorrelatorTest::testAttackEdgeCreation);
        runTest("testIntraHostEscalationCorrelation", AttackCorrelatorTest::testIntraHostEscalationCorrelation);
        runTest("testLateralPivotCorrelation", AttackCorrelatorTest::testLateralPivotCorrelation);
        runTest("testEndToEndPhase4Reconstruction", AttackCorrelatorTest::testEndToEndPhase4Reconstruction);
        runTest("testEmptyEvidenceHandling", AttackCorrelatorTest::testEmptyEvidenceHandling);

        System.out.println("AttackCorrelatorTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError("Assertion failed: " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion failed: " + message + " (expected: " + expected + ", got: " + actual + ")");
    }

    private static void testAttackNodeCreation() {
        LogEntry log = new LogEntry("2026-09-22T10:43:02", "10.0.14.23", "HOST-014", "EXEC", "powershell -enc");
        IOC ioc = new IOC("IOC-001", "COMMAND", "powershell -enc", "Suspicious PowerShell", "HIGH");
        SignatureMatch match = new SignatureMatch(ioc, log, "KMP", 0, "powershell -enc");

        AttackNode node = new AttackNode("AN-001", match, KillChainStage.EXPLOITATION);

        assertEquals("AN-001", node.getId(), "Node ID must match");
        assertEquals("10.0.14.23", node.getSource(), "Source IP must match");
        assertEquals("HOST-014", node.getTarget(), "Target host must match");
        assertEquals(KillChainStage.EXPLOITATION, node.getStage(), "Stage must match");
        assertEquals("2026-09-22T10:43:02", node.getTimestamp(), "Timestamp must match");
        assertTrue(node.hasEvidence(), "Node must have underlying evidence");
        assertEquals("powershell -enc", node.getMatchedPattern(), "Matched pattern must match");
    }

    private static void testAttackEdgeCreation() {
        AttackNode src = new AttackNode("AN-1", "10.0.1.1", "HOST-1", KillChainStage.RECONNAISSANCE, "10:00:00");
        AttackNode dst = new AttackNode("AN-2", "10.0.1.1", "HOST-2", KillChainStage.LATERAL_MOVEMENT, "10:05:00");

        AttackEdge edge = new AttackEdge("EDGE-1", src, dst, "LATERAL_PIVOT");

        assertEquals("EDGE-1", edge.getId(), "Edge ID must match");
        assertEquals(src, edge.getSource(), "Source node must match");
        assertEquals(dst, edge.getTarget(), "Target node must match");
        assertEquals("LATERAL_PIVOT", edge.getRelationship(), "Relationship must match");
        assertFalse(edge.hasEvidence(), "Edge has no explicit match evidence");
    }

    private static void testIntraHostEscalationCorrelation() {
        AttackCorrelator correlator = new AttackCorrelator();

        LogEntry log1 = new LogEntry("2026-09-22T10:42:18", "10.0.14.23", "HOST-014", "EXEC", "whoami /priv");
        IOC ioc1 = new IOC("IOC-006", "COMMAND", "whoami /priv", "desc", "LOW");
        SignatureMatch match1 = new SignatureMatch(ioc1, log1, "KMP", 0, "whoami /priv");

        LogEntry log2 = new LogEntry("2026-09-22T10:43:02", "10.0.14.23", "HOST-014", "EXEC", "powershell -enc");
        IOC ioc2 = new IOC("IOC-001", "COMMAND", "powershell -enc", "desc", "HIGH");
        SignatureMatch match2 = new SignatureMatch(ioc2, log2, "KMP", 0, "powershell -enc");

        DynamicArray<SignatureMatch> matches = new DynamicArray<>();
        matches.add(match1);
        matches.add(match2);

        DirectedGraph<AttackNode> graph = correlator.correlate(matches);

        assertEquals(2, graph.getNodeCount(), "Graph should have 2 nodes");
        assertTrue(graph.containsEdge("AN-1", "AN-2"), "Intra-host progression edge AN-1 -> AN-2 must exist");
        assertFalse(graph.containsEdge("AN-2", "AN-1"), "Reverse edge must NOT exist");
    }

    private static void testLateralPivotCorrelation() {
        AttackCorrelator correlator = new AttackCorrelator();

        LogEntry log1 = new LogEntry("2026-09-22T10:43:02", "10.0.14.23", "HOST-014", "EXEC", "powershell -enc");
        IOC ioc1 = new IOC("IOC-001", "COMMAND", "powershell -enc", "desc", "HIGH");
        SignatureMatch match1 = new SignatureMatch(ioc1, log1, "KMP", 0, "powershell -enc");

        LogEntry log2 = new LogEntry("2026-09-22T10:45:17", "10.0.14.23", "HOST-022", "LATERAL", "net use IPC$");
        IOC ioc2 = new IOC("IOC-004", "PROTOCOL", "IPC$", "desc", "MEDIUM");
        SignatureMatch match2 = new SignatureMatch(ioc2, log2, "KMP", 0, "IPC$");

        DynamicArray<SignatureMatch> matches = new DynamicArray<>();
        matches.add(match1);
        matches.add(match2);

        DirectedGraph<AttackNode> graph = correlator.correlate(matches);

        assertEquals(2, graph.getNodeCount(), "Should have 2 nodes");
        assertTrue(graph.containsEdge("AN-1", "AN-2"), "Lateral pivot edge AN-1 -> AN-2 must exist across hosts");
    }

    private static void testEndToEndPhase4Reconstruction() {
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
        assertTrue(logs.size() > 0, "Logs must be parsed");
        assertTrue(iocs.size() > 0, "IOCs must be loaded");

        // Execute Phase 3 Signature Matcher
        DynamicArray<SignatureMatch> matches = SignatureMatcher.matchAll(logs, iocs, Algorithm.KMP);
        assertTrue(matches.size() >= 7, "At least 7 signature matches expected in sample telemetry");

        // Execute Phase 4 Attack Correlator
        AttackCorrelator correlator = new AttackCorrelator();
        DirectedGraph<AttackNode> attackGraph = correlator.correlate(matches);

        // Verification 1: Node Count
        assertEquals(matches.size(), attackGraph.getNodeCount(), "Every evidence item should produce an attack node");

        // Verification 2: Kill-Chain Association
        KillChain killChain = correlator.getKillChain();
        assertTrue(killChain.getTotalNodeCount() > 0, "KillChain must have associated nodes");
        assertTrue(killChain.getNodesForStage(KillChainStage.PRIVILEGE_ESCALATION).size() > 0, "Should have PRIVILEGE_ESCALATION nodes");
        assertTrue(killChain.getNodesForStage(KillChainStage.LATERAL_MOVEMENT).size() > 0, "Should have LATERAL_MOVEMENT nodes");
        assertTrue(killChain.getNodesForStage(KillChainStage.COMMAND_AND_CONTROL).size() > 0, "Should have COMMAND_AND_CONTROL nodes");

        // Verification 3: Edge Count & Directionality
        assertTrue(attackGraph.getEdgeCount() > 0, "Attack graph must contain correlated edges");

        // Verification 4: End-to-End Attack Path Reconstruction from Initial Ingress (AN-1) to Final Action (AN-N)
        String entryNodeId = "AN-1";
        String sinkNodeId = "AN-" + matches.size();

        assertTrue(PathFinder.hasPath(attackGraph, entryNodeId, sinkNodeId),
                "Directed path must exist from initial ingress node to final sink node");

        AttackPath reconstructedPath = correlator.reconstructPath(entryNodeId, sinkNodeId);
        assertFalse(reconstructedPath.isEmpty(), "Reconstructed attack path must not be empty");
        assertEquals(entryNodeId, reconstructedPath.getStartNode().getId(), "Path must start at initial node");
        assertEquals(sinkNodeId, reconstructedPath.getEndNode().getId(), "Path must end at final target node");
        assertTrue(reconstructedPath.length() >= 1, "Path must span multiple attack steps");
    }

    private static void testEmptyEvidenceHandling() {
        AttackCorrelator correlator = new AttackCorrelator();
        DirectedGraph<AttackNode> graph = correlator.correlate(new DynamicArray<>());

        assertEquals(0, graph.getNodeCount(), "Empty matches must produce empty graph");
        assertEquals(0, graph.getEdgeCount(), "Empty matches must produce 0 edges");
        assertEquals(0, correlator.getCorrelatedNodes().size(), "Correlated nodes list must be empty");
    }
}
