package engine.graph;

import engine.datastructures.DynamicArray;

/**
 * Standalone test suite for {@link BFS}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 */
public class BFSTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running BFSTest...");

        runTest("testBFSLinearChain", BFSTest::testBFSLinearChain);
        runTest("testBFSBranchingDeterministicOrder", BFSTest::testBFSBranchingDeterministicOrder);
        runTest("testBFSCycleHandling", BFSTest::testBFSCycleHandling);
        runTest("testBFSDisconnectedGraph", BFSTest::testBFSDisconnectedGraph);
        runTest("testBFSReachability", BFSTest::testBFSReachability);
        runTest("testBFSEmptyOrInvalidInputs", BFSTest::testBFSEmptyOrInvalidInputs);

        System.out.println("BFSTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void testBFSLinearChain() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");

        DynamicArray<GraphNode<String>> order = BFS.traverse(graph, "A");
        assertEquals(4, order.size(), "Order size should be 4");
        assertEquals("A", order.get(0).getId(), "Order[0] should be A");
        assertEquals("B", order.get(1).getId(), "Order[1] should be B");
        assertEquals("C", order.get(2).getId(), "Order[2] should be C");
        assertEquals("D", order.get(3).getId(), "Order[3] should be D");
    }

    private static void testBFSBranchingDeterministicOrder() {
        // Construct graph:
        //       A
        //     /   \
        //    B     C
        //   / \   / \
        //  D   E F   G
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");
        graph.addEdge("B", "E");
        graph.addEdge("C", "F");
        graph.addEdge("C", "G");

        DynamicArray<GraphNode<String>> order = BFS.traverse(graph, "A");
        assertEquals(7, order.size(), "All 7 nodes should be visited");

        // BFS visits level by level: [A], then [B, C], then [D, E, F, G]
        assertEquals("A", order.get(0).getId(), "Level 0: A");
        assertEquals("B", order.get(1).getId(), "Level 1: B");
        assertEquals("C", order.get(2).getId(), "Level 1: C");
        assertEquals("D", order.get(3).getId(), "Level 2: D");
        assertEquals("E", order.get(4).getId(), "Level 2: E");
        assertEquals("F", order.get(5).getId(), "Level 2: F");
        assertEquals("G", order.get(6).getId(), "Level 2: G");
    }

    private static void testBFSCycleHandling() {
        // Construct cyclic graph: 1 -> 2 -> 3 -> 1, and 2 -> 4
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("1", "2");
        graph.addEdge("2", "3");
        graph.addEdge("3", "1");
        graph.addEdge("2", "4");

        DynamicArray<GraphNode<String>> order = BFS.traverse(graph, "1");
        assertEquals(4, order.size(), "BFS must terminate cleanly on cycle");
        assertEquals("1", order.get(0).getId(), "First is 1");
        assertEquals("2", order.get(1).getId(), "Second is 2");
        // Neighbors of 2 are 3 and 4
        assertEquals("3", order.get(2).getId(), "Third is 3");
        assertEquals("4", order.get(3).getId(), "Fourth is 4");
    }

    private static void testBFSDisconnectedGraph() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("C1_A", "C1_B");
        graph.addEdge("C2_A", "C2_B");

        DynamicArray<GraphNode<String>> singleComp = BFS.traverse(graph, "C1_A");
        assertEquals(2, singleComp.size(), "Single component traversal should have size 2");

        DynamicArray<GraphNode<String>> allComp = BFS.traverseAll(graph);
        assertEquals(4, allComp.size(), "traverseAll should visit all 4 nodes across components");
    }

    private static void testBFSReachability() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("START", "HOP1");
        graph.addEdge("HOP1", "HOP2");
        graph.addEdge("HOP2", "FINISH");
        graph.addNode("ISOLATED");

        assertTrue(BFS.isReachable(graph, "START", "FINISH"), "FINISH must be reachable from START");
        assertFalse(BFS.isReachable(graph, "FINISH", "START"), "START must NOT be reachable from FINISH");
        assertFalse(BFS.isReachable(graph, "START", "ISOLATED"), "ISOLATED must not be reachable");
        assertTrue(BFS.isReachable(graph, "START", "START"), "Node must be self-reachable");
    }

    private static void testBFSEmptyOrInvalidInputs() {
        DirectedGraph<String> emptyGraph = new DirectedGraph<>();
        assertEquals(0, BFS.traverse(emptyGraph, "X").size(), "Empty graph traversal must have size 0");
        assertFalse(BFS.isReachable(emptyGraph, "X", "Y"), "Empty graph reachability must be false");

        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("Z");
        assertEquals(0, BFS.traverse(graph, "NON_EXISTENT").size(), "Missing node traversal must have size 0");
    }
}
