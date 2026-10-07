package engine.monitoring;

import engine.datastructures.DynamicArray;
import engine.graph.DirectedGraph;
import engine.models.AttackNode;
import engine.correlation.KillChainStage;

/**
 * Unit and algorithmic validation tests for MonitoringPlacement (Greedy Set Cover and overall optimizer).
 */
public class MonitoringPlacementTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("Running MonitoringPlacementTest...");

        testEmptyGraphGreedy();
        testSingleEdgeGreedy();
        testStarGraphGreedyOptimal();
        testTieBreakingDeterministic();
        testPartialCoverageTarget();
        testIsolatedNodesIgnored();
        testInvalidInputs();
        testCompleteGraphGreedy();

        System.out.printf("MonitoringPlacementTest Summary: Total=%d, Passed=%d, Failed=%d%n",
                testsRun, testsPassed, testsFailed);

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void testEmptyGraphGreedy() {
        startTest("testEmptyGraphGreedy");
        DirectedGraph<String> graph = new DirectedGraph<>();
        MonitoringResult<String> result = MonitoringPlacement.computeGreedySetCover(graph);

        assertEquals(0, result.getSelectedCount(), "Empty graph has 0 monitoring points");
        assertEquals(0, result.getTotalGraphNodes(), "Total nodes is 0");
        assertEquals(0, result.getTotalGraphEdges(), "Total edges is 0");
        passTest();
    }

    private static void testSingleEdgeGreedy() {
        startTest("testSingleEdgeGreedy");
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", "E1");

        MonitoringResult<String> result = MonitoringPlacement.computeGreedySetCover(graph);
        // Greedy Set Cover: A covers 1 edge, B covers 1 edge.
        // Tie-breaker: lowest index (A is index 0).
        // Selecting A covers the only edge!
        assertEquals(1, result.getSelectedCount(), "Greedy selects only 1 node for single edge");
        assertEquals(1, result.getTotalGraphEdges(), "Total edges is 1");
        assertEquals(1, result.getCoveredEdgesCount(), "Covered edges is 1");
        assertEquals(1.0, result.getCoverageRatio(), "100% coverage");
        assertTrue(result.containsMonitoringPoint("A"), "Node A chosen due to lower index tie-break");
        passTest();
    }

    private static void testStarGraphGreedyOptimal() {
        startTest("testStarGraphGreedyOptimal");
        // Center C connected to 5 leaves: L1, L2, L3, L4, L5
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("C");
        for (int i = 1; i <= 5; i++) {
            graph.addNode("L" + i);
            graph.addEdge("C", "L" + i, "E" + i);
        }

        MonitoringResult<String> result = MonitoringPlacement.computeGreedySetCover(graph);
        // Center C covers all 5 edges in 1 step!
        assertEquals(1, result.getSelectedCount(), "Greedy optimal selects 1 central hub");
        assertTrue(result.containsMonitoringPoint("C"), "Center node C selected");
        assertEquals(5, result.getTotalGraphEdges(), "Total edges is 5");
        assertEquals(5, result.getCoveredEdgesCount(), "All 5 edges covered");
        assertEquals(1.0, result.getCoverageRatio(), "100% coverage");
        passTest();
    }

    private static void testTieBreakingDeterministic() {
        startTest("testTieBreakingDeterministic");
        // Two symmetric independent edges: A -> B and C -> D
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addEdge("A", "B", "E1");
        graph.addEdge("C", "D", "E2");

        MonitoringResult<String> r1 = MonitoringPlacement.computeGreedySetCover(graph);
        MonitoringResult<String> r2 = MonitoringPlacement.computeGreedySetCover(graph);

        assertEquals(2, r1.getSelectedCount(), "Two nodes needed for two disjoint edges");
        // Due to lowest index preference, A (idx 0) and C (idx 2) are selected
        assertTrue(r1.containsMonitoringPoint("A"), "Node A selected");
        assertTrue(r1.containsMonitoringPoint("C"), "Node C selected");

        for (int i = 0; i < r1.getSelectedCount(); i++) {
            assertEquals(r1.getMonitoringPoints().get(i).getNode(),
                         r2.getMonitoringPoints().get(i).getNode(),
                         "Deterministic ordering matches");
        }
        passTest();
    }

    private static void testPartialCoverageTarget() {
        startTest("testPartialCoverageTarget");
        // Disjoint edges E1: A->B, E2: C->D, E3: E->F, E4: G->H (4 edges total)
        DirectedGraph<String> graph = new DirectedGraph<>();
        String[] nodes = {"A", "B", "C", "D", "E", "F", "G", "H"};
        for (int i = 0; i < nodes.length; i++) {
            graph.addNode(nodes[i]);
        }
        graph.addEdge("A", "B", "E1");
        graph.addEdge("C", "D", "E2");
        graph.addEdge("E", "F", "E3");
        graph.addEdge("G", "H", "E4");

        // Request 50% coverage (2 of 4 edges)
        MonitoringResult<String> result = MonitoringPlacement.computeGreedySetCover(graph, 0.50);
        assertEquals(2, result.getSelectedCount(), "2 nodes needed to cover 2 edges");
        assertEquals(2, result.getCoveredEdgesCount(), "2 edges covered");
        assertEquals(0.50, result.getCoverageRatio(), "50% coverage achieved");
        passTest();
    }

    private static void testIsolatedNodesIgnored() {
        startTest("testIsolatedNodesIgnored");
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("ISO1");
        graph.addNode("ISO2");
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", "E1");

        MonitoringResult<String> result = MonitoringPlacement.computeGreedySetCover(graph);
        assertEquals(1, result.getSelectedCount(), "Only 1 node selected");
        assertFalse(result.containsMonitoringPoint("ISO1"), "Isolated 1 not selected");
        assertFalse(result.containsMonitoringPoint("ISO2"), "Isolated 2 not selected");
        passTest();
    }

    private static void testInvalidInputs() {
        startTest("testInvalidInputs");
        boolean caughtNull = false;
        try {
            MonitoringPlacement.computeVertexCover(null);
        } catch (IllegalArgumentException e) {
            caughtNull = true;
        }
        assertTrue(caughtNull, "Null graph rejected in Vertex Cover");

        boolean caughtRatio = false;
        try {
            MonitoringPlacement.computeGreedySetCover(new DirectedGraph<>(), -0.1);
        } catch (IllegalArgumentException e) {
            caughtRatio = true;
        }
        assertTrue(caughtRatio, "Negative ratio rejected");

        boolean caughtRatioHigh = false;
        try {
            MonitoringPlacement.computeGreedySetCover(new DirectedGraph<>(), 1.5);
        } catch (IllegalArgumentException e) {
            caughtRatioHigh = true;
        }
        assertTrue(caughtRatioHigh, "Ratio > 1.0 rejected");
        passTest();
    }

    private static void testCompleteGraphGreedy() {
        startTest("testCompleteGraphGreedy");
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("N1");
        graph.addNode("N2");
        graph.addNode("N3");
        graph.addNode("N4");
        graph.addEdge("N1", "N2", "E12");
        graph.addEdge("N1", "N3", "E13");
        graph.addEdge("N1", "N4", "E14");
        graph.addEdge("N2", "N3", "E23");
        graph.addEdge("N2", "N4", "E24");
        graph.addEdge("N3", "N4", "E34");

        MonitoringResult<String> result = MonitoringPlacement.computeGreedySetCover(graph);
        // Complete graph K4: total 6 edges.
        // N1 has degree 3 (gain 3).
        // After N1, 3 edges remain. N2 covers 2 of them (gain 2).
        // 1 edge remains (N3-N4). N3 covers it (gain 1).
        // Total 3 vertices selected to cover all 6 edges.
        assertEquals(3, result.getSelectedCount(), "3 vertices cover K4");
        assertEquals(6, result.getCoveredEdgesCount(), "All 6 edges covered");
        assertEquals(1.0, result.getCoverageRatio(), "100% coverage ratio");
        passTest();
    }

    // ------------------------------------------------------------------------
    // Helpers
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
