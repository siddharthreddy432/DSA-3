package engine.graph;

import engine.datastructures.DynamicArray;

/**
 * Standalone test suite for {@link DFS}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 */
public class DFSTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running DFSTest...");

        runTest("testDFSLinearChain", DFSTest::testDFSLinearChain);
        runTest("testDFSBranchingDeterministicOrder", DFSTest::testDFSBranchingDeterministicOrder);
        runTest("testDFSCycleHandling", DFSTest::testDFSCycleHandling);
        runTest("testDFSDisconnectedGraph", DFSTest::testDFSDisconnectedGraph);
        runTest("testDFSReachability", DFSTest::testDFSReachability);
        runTest("testDFSEmptyOrInvalidInputs", DFSTest::testDFSEmptyOrInvalidInputs);

        System.out.println("DFSTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void testDFSLinearChain() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("N1", "N2");
        graph.addEdge("N2", "N3");
        graph.addEdge("N3", "N4");

        DynamicArray<GraphNode<String>> order = DFS.traverse(graph, "N1");
        assertEquals(4, order.size(), "Order size should be 4");
        assertEquals("N1", order.get(0).getId(), "Order[0] should be N1");
        assertEquals("N2", order.get(1).getId(), "Order[1] should be N2");
        assertEquals("N3", order.get(2).getId(), "Order[2] should be N3");
        assertEquals("N4", order.get(3).getId(), "Order[3] should be N4");
    }

    private static void testDFSBranchingDeterministicOrder() {
        // Construct graph:
        //      A
        //     / \
        //    B   C
        //   /     \
        //  D       E
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");
        graph.addEdge("C", "E");

        DynamicArray<GraphNode<String>> order = DFS.traverse(graph, "A");
        assertEquals(5, order.size(), "All 5 nodes should be visited");
        assertEquals("A", order.get(0).getId(), "Order[0] should be A");
        assertEquals("B", order.get(1).getId(), "Order[1] should be B");
        assertEquals("D", order.get(2).getId(), "Order[2] should be D (depth-first before C)");
        assertEquals("C", order.get(3).getId(), "Order[3] should be C");
        assertEquals("E", order.get(4).getId(), "Order[4] should be E");
    }

    private static void testDFSCycleHandling() {
        // Construct cyclic graph: A -> B -> C -> A, and B -> D
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");
        graph.addEdge("B", "D");

        DynamicArray<GraphNode<String>> order = DFS.traverse(graph, "A");
        assertEquals(4, order.size(), "DFS must terminate on cyclic graph visiting each node exactly once");
        assertTrue(DFS.isReachable(graph, "A", "D"), "D should be reachable from A despite cycle");
    }

    private static void testDFSDisconnectedGraph() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A1", "A2");
        graph.addEdge("B1", "B2");

        DynamicArray<GraphNode<String>> orderA = DFS.traverse(graph, "A1");
        assertEquals(2, orderA.size(), "Traversal from A1 should only visit component A");

        DynamicArray<GraphNode<String>> orderAll = DFS.traverseAll(graph);
        assertEquals(4, orderAll.size(), "traverseAll should visit all 4 nodes across components");
    }

    private static void testDFSReachability() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("S", "M1");
        graph.addEdge("M1", "M2");
        graph.addEdge("M2", "TARGET");
        graph.addNode("ISOLATED");

        assertTrue(DFS.isReachable(graph, "S", "TARGET"), "TARGET must be reachable from S");
        assertFalse(DFS.isReachable(graph, "TARGET", "S"), "S must NOT be reachable from TARGET (directed)");
        assertFalse(DFS.isReachable(graph, "S", "ISOLATED"), "ISOLATED must not be reachable");
        assertTrue(DFS.isReachable(graph, "S", "S"), "Node must be self-reachable");
    }

    private static void testDFSEmptyOrInvalidInputs() {
        DirectedGraph<String> emptyGraph = new DirectedGraph<>();
        assertEquals(0, DFS.traverse(emptyGraph, "NONE").size(), "Empty graph should yield empty traversal");
        assertFalse(DFS.isReachable(emptyGraph, "A", "B"), "Empty graph reachability must be false");

        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("X");
        assertEquals(0, DFS.traverse(graph, "MISSING").size(), "Missing node start ID should yield empty list");
    }
}
