package engine.optimization;

import engine.datastructures.DynamicArray;
import engine.graph.DirectedGraph;

/**
 * Standalone test suite for {@link ArticulationPoints}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 */
public class ArticulationPointsTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running ArticulationPointsTest...");

        runTest("testLinearChain", ArticulationPointsTest::testLinearChain);
        runTest("testBranchingGraph", ArticulationPointsTest::testBranchingGraph);
        runTest("testTriangleCycleNoAP", ArticulationPointsTest::testTriangleCycleNoAP);
        runTest("testDisconnectedGraph", ArticulationPointsTest::testDisconnectedGraph);
        runTest("testSingleVertex", ArticulationPointsTest::testSingleVertex);
        runTest("testTwoVertices", ArticulationPointsTest::testTwoVertices);
        runTest("testRootArticulation", ArticulationPointsTest::testRootArticulation);
        runTest("testNonRootArticulation", ArticulationPointsTest::testNonRootArticulation);
        runTest("testMultipleArticulationPoints", ArticulationPointsTest::testMultipleArticulationPoints);
        runTest("testCompleteBiconnectedNoAP", ArticulationPointsTest::testCompleteBiconnectedNoAP);

        System.out.println("ArticulationPointsTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError("Assertion failed: " + message + " (expected: " + expected + ", got: " + actual + ")");
        }
    }

    private static boolean contains(DynamicArray<String> array, String val) {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(val)) return true;
        }
        return false;
    }

    private static void testLinearChain() {
        // Directed chain: A -> B -> C -> D
        // In undirected projection: A - B - C - D
        // Articulation points are interior vertices: B and C
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");

        DynamicArray<String> aps = ArticulationPoints.findArticulationPoints(graph);
        assertEquals(2, aps.size(), "Linear chain of 4 vertices has 2 articulation points");
        assertTrue(contains(aps, "B"), "B is an articulation point");
        assertTrue(contains(aps, "C"), "C is an articulation point");
        assertFalse(contains(aps, "A"), "A is not an articulation point");
        assertFalse(contains(aps, "D"), "D is not an articulation point");
    }

    private static void testBranchingGraph() {
        // Star / Hub topology:
        // C is central hub. C -> L1, C -> L2, C -> L3
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("C", "L1");
        graph.addEdge("C", "L2");
        graph.addEdge("C", "L3");

        DynamicArray<String> aps = ArticulationPoints.findArticulationPoints(graph);
        assertEquals(1, aps.size(), "Central hub C is the only articulation point");
        assertTrue(contains(aps, "C"), "Hub C must be an articulation point");
    }

    private static void testTriangleCycleNoAP() {
        // Cycle: A -> B -> C -> A
        // Removing any single node leaves the remaining two connected.
        // Zero articulation points.
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        DynamicArray<String> aps = ArticulationPoints.findArticulationPoints(graph);
        assertEquals(0, aps.size(), "Triangle cycle has 0 articulation points");
    }

    private static void testDisconnectedGraph() {
        // Component 1: A -> B -> C (AP is B)
        // Component 2: D -> E -> F (AP is E)
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("D", "E");
        graph.addEdge("E", "F");

        DynamicArray<String> aps = ArticulationPoints.findArticulationPoints(graph);
        assertEquals(2, aps.size(), "Two disconnected chains have 2 articulation points (B and E)");
        assertTrue(contains(aps, "B"), "B is AP in component 1");
        assertTrue(contains(aps, "E"), "E is AP in component 2");
    }

    private static void testSingleVertex() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("SOLO");

        DynamicArray<String> aps = ArticulationPoints.findArticulationPoints(graph);
        assertEquals(0, aps.size(), "Single vertex graph has 0 articulation points");
    }

    private static void testTwoVertices() {
        // Two vertices connected: A -> B
        // Removing either leaves 1 vertex (which is connected).
        // Neither vertex partitions the graph into multiple connected components.
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");

        DynamicArray<String> aps = ArticulationPoints.findArticulationPoints(graph);
        assertEquals(0, aps.size(), "Graph with 2 vertices has 0 articulation points");
    }

    private static void testRootArticulation() {
        // Root R has two separate subtrees: R -> A1 -> A2 and R -> B1 -> B2
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("R", "A1");
        graph.addEdge("A1", "A2");
        graph.addEdge("R", "B1");
        graph.addEdge("B1", "B2");

        DynamicArray<String> aps = ArticulationPoints.findArticulationPoints(graph);
        // Root R connects two subtrees -> R is AP
        // A1 is between R and A2 -> A1 is AP
        // B1 is between R and B2 -> B1 is AP
        assertTrue(contains(aps, "R"), "Root R must be an articulation point");
        assertTrue(contains(aps, "A1"), "A1 is an articulation point");
        assertTrue(contains(aps, "B1"), "B1 is an articulation point");
    }

    private static void testNonRootArticulation() {
        // Triangle A-B-C connected via bridge C-D to D
        // In undirected projection: A-B-C is cycle, C is connected to D
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");
        graph.addEdge("C", "D"); // Bridge to leaf D

        DynamicArray<String> aps = ArticulationPoints.findArticulationPoints(graph);
        assertEquals(1, aps.size(), "C is the single bridge articulation point to leaf D");
        assertTrue(contains(aps, "C"), "C must be articulation point");
        assertFalse(contains(aps, "D"), "Leaf D is not articulation point");
    }

    private static void testMultipleArticulationPoints() {
        // Two blocks joined at a chain of cut vertices:
        // Cycle (1-2-3-1), 3 -> 4 -> 5, Cycle (5-6-7-5)
        // Cut vertices: 3, 4, 5
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("1", "2");
        graph.addEdge("2", "3");
        graph.addEdge("3", "1");
        graph.addEdge("3", "4");
        graph.addEdge("4", "5");
        graph.addEdge("5", "6");
        graph.addEdge("6", "7");
        graph.addEdge("7", "5");

        DynamicArray<String> aps = ArticulationPoints.findArticulationPoints(graph);
        assertEquals(3, aps.size(), "There are 3 articulation points: 3, 4, 5");
        assertTrue(contains(aps, "3"), "3 is an AP");
        assertTrue(contains(aps, "4"), "4 is an AP");
        assertTrue(contains(aps, "5"), "5 is an AP");
    }

    private static void testCompleteBiconnectedNoAP() {
        // Biconnected 4-node graph: A-B, B-C, C-D, D-A, A-C, B-D
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");
        graph.addEdge("D", "A");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");

        DynamicArray<String> aps = ArticulationPoints.findArticulationPoints(graph);
        assertEquals(0, aps.size(), "Biconnected complete 4-node graph has 0 articulation points");
    }
}
