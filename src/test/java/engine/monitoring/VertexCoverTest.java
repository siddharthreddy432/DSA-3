package engine.monitoring;

import engine.datastructures.DynamicArray;
import engine.graph.DirectedGraph;
import engine.graph.GraphNode;
import engine.models.AttackNode;
import engine.correlation.KillChainStage;

/**
 * Unit and algorithmic validation tests for Vertex Cover (2-approximation) in Phase 6.
 */
public class VertexCoverTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("Running VertexCoverTest...");

        testEmptyGraph();
        testSingleEdge();
        testLinearChain();
        testStarGraph();
        testTriangleCycle();
        testDisconnectedComponents();
        testCoverageCorrectness();
        testDeterministicSelection();
        testNoDuplicateMonitoringPoints();
        testPhase4AttackNodeIntegration();

        System.out.printf("VertexCoverTest Summary: Total=%d, Passed=%d, Failed=%d%n",
                testsRun, testsPassed, testsFailed);

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void testEmptyGraph() {
        startTest("testEmptyGraph");
        DirectedGraph<String> graph = new DirectedGraph<>();
        MonitoringResult<String> result = MonitoringPlacement.computeVertexCover(graph);

        assertEquals(0, result.getSelectedCount(), "Empty graph should have 0 selected points");
        assertEquals(0, result.getTotalGraphNodes(), "Total nodes should be 0");
        assertEquals(0, result.getTotalGraphEdges(), "Total edges should be 0");
        assertEquals(0, result.getCoveredEdgesCount(), "Covered edges should be 0");
        assertEquals(1.0, result.getCoverageRatio(), "Coverage ratio should be 1.0 on empty");
        passTest();
    }

    private static void testSingleEdge() {
        startTest("testSingleEdge");
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", "EDGE-AB");

        MonitoringResult<String> result = MonitoringPlacement.computeVertexCover(graph);
        assertEquals(2, result.getSelectedCount(), "2-approx picks both endpoints of single edge");
        assertEquals(1, result.getTotalGraphEdges(), "Total edges is 1");
        assertEquals(1, result.getCoveredEdgesCount(), "Covered edges is 1");
        assertEquals(1.0, result.getCoverageRatio(), "Coverage ratio should be 1.0");
        assertTrue(result.containsMonitoringPoint("A"), "Cover contains A");
        assertTrue(result.containsMonitoringPoint("B"), "Cover contains B");
        passTest();
    }

    private static void testLinearChain() {
        startTest("testLinearChain");
        // A -> B -> C -> D
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addEdge("A", "B", "E1");
        graph.addEdge("B", "C", "E2");
        graph.addEdge("C", "D", "E3");

        MonitoringResult<String> result = MonitoringPlacement.computeVertexCover(graph);
        // In 2-approx:
        // Edge (A, B) selected -> A, B in cover. Covers (A, B) and (B, C).
        // Remaining uncovered edge: (C, D) -> C, D in cover.
        // All 3 edges covered.
        assertEquals(4, result.getSelectedCount(), "Linear chain 4 nodes selected in 2-approx");
        assertEquals(3, result.getTotalGraphEdges(), "Total edges is 3");
        assertEquals(3, result.getCoveredEdgesCount(), "Covered edges is 3");
        assertEquals(1.0, result.getCoverageRatio(), "Coverage ratio should be 1.0");
        passTest();
    }

    private static void testStarGraph() {
        startTest("testStarGraph");
        // Center C connected to leaves L1, L2, L3, L4
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("C");
        graph.addNode("L1");
        graph.addNode("L2");
        graph.addNode("L3");
        graph.addNode("L4");
        graph.addEdge("C", "L1", "E1");
        graph.addEdge("C", "L2", "E2");
        graph.addEdge("C", "L3", "E3");
        graph.addEdge("C", "L4", "E4");

        MonitoringResult<String> result = MonitoringPlacement.computeVertexCover(graph);
        // Edge (C, L1) selected -> C, L1 in cover.
        // Since C is in cover, all edges (C, L1), (C, L2), (C, L3), (C, L4) become covered!
        // Cover contains C and L1 (2 vertices).
        assertEquals(2, result.getSelectedCount(), "Star graph 2-approx picks center and first leaf");
        assertEquals(4, result.getTotalGraphEdges(), "Total edges is 4");
        assertEquals(4, result.getCoveredEdgesCount(), "All 4 edges covered");
        assertEquals(1.0, result.getCoverageRatio(), "Coverage ratio is 1.0");
        assertTrue(result.containsMonitoringPoint("C"), "Center is in cover");
        assertTrue(result.containsMonitoringPoint("L1"), "Leaf 1 is in cover");
        passTest();
    }

    private static void testTriangleCycle() {
        startTest("testTriangleCycle");
        // A -> B -> C -> A
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B", "E1");
        graph.addEdge("B", "C", "E2");
        graph.addEdge("C", "A", "E3");

        MonitoringResult<String> result = MonitoringPlacement.computeVertexCover(graph);
        // Pick (A, B) -> A and B in cover.
        // Incident to A: (A, B) and (C, A).
        // Incident to B: (A, B) and (B, C).
        // All 3 edges are now covered!
        // Selected: A, B (2 vertices).
        assertEquals(2, result.getSelectedCount(), "Triangle covered by 2 vertices");
        assertEquals(3, result.getTotalGraphEdges(), "Total edges is 3");
        assertEquals(3, result.getCoveredEdgesCount(), "All 3 edges covered");
        assertEquals(1.0, result.getCoverageRatio(), "Coverage ratio is 1.0");
        passTest();
    }

    private static void testDisconnectedComponents() {
        startTest("testDisconnectedComponents");
        // Comp 1: A -> B
        // Comp 2: C -> D
        // Isolated node: E
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        graph.addEdge("A", "B", "E1");
        graph.addEdge("C", "D", "E2");

        MonitoringResult<String> result = MonitoringPlacement.computeVertexCover(graph);
        assertEquals(4, result.getSelectedCount(), "Covers both edges across components");
        assertEquals(5, result.getTotalGraphNodes(), "Total nodes is 5");
        assertEquals(2, result.getTotalGraphEdges(), "Total edges is 2");
        assertEquals(2, result.getCoveredEdgesCount(), "All 2 edges covered");
        assertFalse(result.containsMonitoringPoint("E"), "Isolated node E not in cover");
        passTest();
    }

    private static void testCoverageCorrectness() {
        startTest("testCoverageCorrectness");
        // Fully dense 4-vertex graph
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("V1");
        graph.addNode("V2");
        graph.addNode("V3");
        graph.addNode("V4");
        graph.addEdge("V1", "V2", "E12");
        graph.addEdge("V1", "V3", "E13");
        graph.addEdge("V1", "V4", "E14");
        graph.addEdge("V2", "V3", "E23");
        graph.addEdge("V2", "V4", "E24");
        graph.addEdge("V3", "V4", "E34");

        MonitoringResult<String> result = MonitoringPlacement.computeVertexCover(graph);
        assertEquals(6, result.getTotalGraphEdges(), "Total edges 6");
        assertEquals(6, result.getCoveredEdgesCount(), "All 6 edges must be covered");
        assertEquals(1.0, result.getCoverageRatio(), "Coverage ratio must be 1.0");
        passTest();
    }

    private static void testDeterministicSelection() {
        startTest("testDeterministicSelection");
        // Run vertex cover twice on identical graph, verify exact same monitoring points
        DirectedGraph<String> g1 = new DirectedGraph<>();
        g1.addNode("X");
        g1.addNode("Y");
        g1.addNode("Z");
        g1.addEdge("X", "Y", "E1");
        g1.addEdge("Y", "Z", "E2");

        MonitoringResult<String> r1 = MonitoringPlacement.computeVertexCover(g1);
        MonitoringResult<String> r2 = MonitoringPlacement.computeVertexCover(g1);

        assertEquals(r1.getSelectedCount(), r2.getSelectedCount(), "Count matches");
        for (int i = 0; i < r1.getSelectedCount(); i++) {
            assertEquals(r1.getMonitoringPoints().get(i).getNode(),
                         r2.getMonitoringPoints().get(i).getNode(),
                         "Monitoring points match deterministically");
        }
        passTest();
    }

    private static void testNoDuplicateMonitoringPoints() {
        startTest("testNoDuplicateMonitoringPoints");
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B", "E1");
        graph.addEdge("B", "A", "E2"); // Bidirectional
        graph.addEdge("B", "C", "E3");

        MonitoringResult<String> result = MonitoringPlacement.computeVertexCover(graph);
        DynamicArray<MonitoringPoint<String>> points = result.getMonitoringPoints();

        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                assertFalse(points.get(i).getNode().equals(points.get(j).getNode()),
                        "No duplicate monitoring points in result");
            }
        }
        passTest();
    }

    private static void testPhase4AttackNodeIntegration() {
        startTest("testPhase4AttackNodeIntegration");
        DirectedGraph<AttackNode> graph = new DirectedGraph<>();
        AttackNode n1 = new AttackNode("N1", "10.0.0.1", "HOST-1", KillChainStage.RECONNAISSANCE, "2026-03-30T10:00:00", null);
        AttackNode n2 = new AttackNode("N2", "10.0.0.1", "HOST-2", KillChainStage.EXPLOITATION, "2026-03-30T10:05:00", null);
        AttackNode n3 = new AttackNode("N3", "10.0.0.2", "HOST-3", KillChainStage.COMMAND_AND_CONTROL, "2026-03-30T10:10:00", null);

        graph.addNode(n1.getId(), n1);
        graph.addNode(n2.getId(), n2);
        graph.addNode(n3.getId(), n3);
        graph.addEdge(n1.getId(), n2.getId(), "PIVOT-1");
        graph.addEdge(n2.getId(), n3.getId(), "PIVOT-2");

        MonitoringResult<AttackNode> result = MonitoringPlacement.computeVertexCover(graph);
        assertTrue(result.getSelectedCount() > 0, "Selected monitoring points > 0");
        assertEquals(2, result.getTotalGraphEdges(), "Total edges is 2");
        assertEquals(2, result.getCoveredEdgesCount(), "All edges covered");
        assertEquals(1.0, result.getCoverageRatio(), "Coverage is 100%");
        passTest();
    }

    // ------------------------------------------------------------------------
    // Test Harness Helpers
    // ------------------------------------------------------------------------
    private static void startTest(String testName) {
        testsRun++;
        System.out.printf("  [TEST] %s... ", testName);
    }

    private static void passTest() {
        testsPassed++;
        System.out.println("PASS");
    }

    private static void failTest(String msg) {
        testsFailed++;
        System.out.println("FAIL: " + msg);
        throw new AssertionError(msg);
    }

    private static void assertEquals(int expected, int actual, String msg) {
        if (expected != actual) {
            failTest(msg + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }

    private static void assertEquals(double expected, double actual, String msg) {
        if (Math.abs(expected - actual) > 0.0001) {
            failTest(msg + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        failTest(msg + " (expected: " + expected + ", actual: " + actual + ")");
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            failTest(msg);
        }
    }

    private static void assertFalse(boolean condition, String msg) {
        if (condition) {
            failTest(msg);
        }
    }
}
