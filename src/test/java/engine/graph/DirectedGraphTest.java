package engine.graph;

import engine.datastructures.DynamicArray;

/**
 * Standalone test suite for {@link DirectedGraph}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 */
public class DirectedGraphTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running DirectedGraphTest...");

        runTest("testNodeInsertion", DirectedGraphTest::testNodeInsertion);
        runTest("testEdgeInsertion", DirectedGraphTest::testEdgeInsertion);
        runTest("testDirectionality", DirectedGraphTest::testDirectionality);
        runTest("testNodeLookup", DirectedGraphTest::testNodeLookup);
        runTest("testEdgeLookup", DirectedGraphTest::testEdgeLookup);
        runTest("testDuplicateHandling", DirectedGraphTest::testDuplicateHandling);
        runTest("testNeighborRetrieval", DirectedGraphTest::testNeighborRetrieval);
        runTest("testClearAndReset", DirectedGraphTest::testClearAndReset);

        System.out.println("DirectedGraphTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void testNodeInsertion() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        assertEquals(0, graph.getNodeCount(), "Initial node count must be 0");

        graph.addNode("A", "Node-A");
        graph.addNode("B", "Node-B");
        graph.addNode("C", "Node-C");

        assertEquals(3, graph.getNodeCount(), "Node count should be 3");
        DynamicArray<GraphNode<String>> nodes = graph.getNodes();
        assertEquals(3, nodes.size(), "getNodes size should be 3");
        assertEquals("A", nodes.get(0).getId(), "First node should be A");
        assertEquals("B", nodes.get(1).getId(), "Second node should be B");
        assertEquals("C", nodes.get(2).getId(), "Third node should be C");
    }

    private static void testEdgeInsertion() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B", "CONNECTS");
        graph.addEdge("B", "C", "ADVANCES");

        assertEquals(2, graph.getEdgeCount(), "Edge count should be 2");
        assertEquals(3, graph.getNodeCount(), "Nodes should be auto-created");
        assertTrue(graph.containsEdge("A", "B"), "Edge A->B should exist");
        assertTrue(graph.containsEdge("B", "C"), "Edge B->C should exist");
    }

    private static void testDirectionality() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("X", "Y");

        assertTrue(graph.containsEdge("X", "Y"), "Edge X->Y must exist");
        assertFalse(graph.containsEdge("Y", "X"), "Edge Y->X must NOT exist in directed graph");

        DynamicArray<GraphNode<String>> neighborsX = graph.getNeighbors("X");
        assertEquals(1, neighborsX.size(), "X should have 1 outgoing neighbor");
        assertEquals("Y", neighborsX.get(0).getId(), "Neighbor of X should be Y");

        DynamicArray<GraphNode<String>> neighborsY = graph.getNeighbors("Y");
        assertEquals(0, neighborsY.size(), "Y should have 0 outgoing neighbors");
    }

    private static void testNodeLookup() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("ALPHA", "Data-Alpha");

        assertTrue(graph.containsNode("ALPHA"), "ALPHA must be contained");
        assertFalse(graph.containsNode("BETA"), "BETA must not be contained");

        GraphNode<String> node = graph.getNode("ALPHA");
        assertEquals("ALPHA", node.getId(), "Node ID must match");
        assertEquals("Data-Alpha", node.getData(), "Node payload must match");

        GraphNode<String> missing = graph.getNode("MISSING");
        assertEquals(null, missing, "Missing node must return null");
    }

    private static void testEdgeLookup() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("N1", "N2", "REL-1");
        graph.addEdge("N2", "N3", "REL-2");

        assertTrue(graph.containsEdge("N1", "N2"), "N1->N2 must exist");
        assertTrue(graph.containsEdge("N2", "N3"), "N2->N3 must exist");
        assertFalse(graph.containsEdge("N1", "N3"), "N1->N3 direct edge must not exist");
        assertFalse(graph.containsEdge("N3", "N1"), "N3->N1 reverse edge must not exist");
    }

    private static void testDuplicateHandling() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("A");
        graph.addNode("A"); // Duplicate node
        assertEquals(1, graph.getNodeCount(), "Duplicate node should not increase count");

        boolean added1 = graph.addDirectedEdge("A", "B");
        boolean added2 = graph.addDirectedEdge("A", "B"); // Duplicate edge
        assertTrue(added1, "First edge add must return true");
        assertFalse(added2, "Duplicate edge add must return false");
        assertEquals(1, graph.getEdgeCount(), "Duplicate edge should not increase edge count");
    }

    private static void testNeighborRetrieval() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("SOURCE", "T1");
        graph.addEdge("SOURCE", "T2");
        graph.addEdge("SOURCE", "T3");

        DynamicArray<GraphNode<String>> neighbors = graph.getNeighbors("SOURCE");
        assertEquals(3, neighbors.size(), "SOURCE should have 3 neighbors");
        assertEquals("T1", neighbors.get(0).getId(), "First neighbor should be T1");
        assertEquals("T2", neighbors.get(1).getId(), "Second neighbor should be T2");
        assertEquals("T3", neighbors.get(2).getId(), "Third neighbor should be T3");
    }

    private static void testClearAndReset() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        assertEquals(3, graph.getNodeCount(), "Should have 3 nodes");
        assertEquals(2, graph.getEdgeCount(), "Should have 2 edges");

        graph.clear();
        assertEquals(0, graph.getNodeCount(), "Cleared graph should have 0 nodes");
        assertEquals(0, graph.getEdgeCount(), "Cleared graph should have 0 edges");
        assertFalse(graph.containsNode("A"), "Node A should no longer exist");
    }
}
