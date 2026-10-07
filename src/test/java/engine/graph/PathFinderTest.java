package engine.graph;

import engine.datastructures.DynamicArray;
import engine.models.AttackNode;
import engine.models.AttackPath;
import engine.correlation.KillChainStage;

/**
 * Standalone test suite for {@link PathFinder}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 */
public class PathFinderTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running PathFinderTest...");

        runTest("testDirectPathExists", PathFinderTest::testDirectPathExists);
        runTest("testMultiHopPathSequenceReconstruction", PathFinderTest::testMultiHopPathSequenceReconstruction);
        runTest("testPathDoesNotExistDisconnected", PathFinderTest::testPathDoesNotExistDisconnected);
        runTest("testReverseDirectionPathFails", PathFinderTest::testReverseDirectionPathFails);
        runTest("testSelfLoopIdentityPath", PathFinderTest::testSelfLoopIdentityPath);
        runTest("testCyclicGraphPathReconstruction", PathFinderTest::testCyclicGraphPathReconstruction);
        runTest("testShortestHopSelectionInBranchingGraph", PathFinderTest::testShortestHopSelectionInBranchingGraph);
        runTest("testAttackPathModelReconstruction", PathFinderTest::testAttackPathModelReconstruction);
        runTest("testUnreachableAttackPath", PathFinderTest::testUnreachableAttackPath);

        System.out.println("PathFinderTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void testDirectPathExists() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("SRC", "DST");

        assertTrue(PathFinder.hasPath(graph, "SRC", "DST"), "Path should exist");
        DynamicArray<GraphNode<String>> path = PathFinder.findPath(graph, "SRC", "DST");
        assertEquals(2, path.size(), "Direct path should have 2 nodes");
        assertEquals("SRC", path.get(0).getId(), "First node should be SRC");
        assertEquals("DST", path.get(1).getId(), "Second node should be DST");
    }

    private static void testMultiHopPathSequenceReconstruction() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("H1", "H2");
        graph.addEdge("H2", "H3");
        graph.addEdge("H3", "H4");

        DynamicArray<GraphNode<String>> path = PathFinder.findPath(graph, "H1", "H4");
        assertEquals(4, path.size(), "Path should contain 4 nodes");
        assertEquals("H1", path.get(0).getId(), "Node 0 should be H1");
        assertEquals("H2", path.get(1).getId(), "Node 1 should be H2");
        assertEquals("H3", path.get(2).getId(), "Node 2 should be H3");
        assertEquals("H4", path.get(3).getId(), "Node 3 should be H4");
    }

    private static void testPathDoesNotExistDisconnected() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("C", "D");

        assertFalse(PathFinder.hasPath(graph, "A", "D"), "No path should exist between disconnected components");
        DynamicArray<GraphNode<String>> path = PathFinder.findPath(graph, "A", "D");
        assertTrue(path.isEmpty(), "Path should be empty");
    }

    private static void testReverseDirectionPathFails() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("U", "V");

        assertFalse(PathFinder.hasPath(graph, "V", "U"), "Directed edge does not allow reverse path");
        DynamicArray<GraphNode<String>> path = PathFinder.findPath(graph, "V", "U");
        assertTrue(path.isEmpty(), "Path should be empty");
    }

    private static void testSelfLoopIdentityPath() {
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("SELF");

        assertTrue(PathFinder.hasPath(graph, "SELF", "SELF"), "Node should have path to itself");
        DynamicArray<GraphNode<String>> path = PathFinder.findPath(graph, "SELF", "SELF");
        assertEquals(1, path.size(), "Identity path should have 1 node");
        assertEquals("SELF", path.get(0).getId(), "Node should be SELF");
    }

    private static void testCyclicGraphPathReconstruction() {
        // Cycle: A -> B -> C -> A, and C -> D
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");
        graph.addEdge("C", "D");

        DynamicArray<GraphNode<String>> path = PathFinder.findPath(graph, "A", "D");
        assertEquals(4, path.size(), "Path should have 4 nodes");
        assertEquals("A", path.get(0).getId(), "Order[0] = A");
        assertEquals("B", path.get(1).getId(), "Order[1] = B");
        assertEquals("C", path.get(2).getId(), "Order[2] = C");
        assertEquals("D", path.get(3).getId(), "Order[3] = D");
    }

    private static void testShortestHopSelectionInBranchingGraph() {
        // Path 1 (short): S -> A -> T (2 hops)
        // Path 2 (long):  S -> B -> C -> D -> T (4 hops)
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addEdge("S", "A");
        graph.addEdge("A", "T");
        graph.addEdge("S", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");
        graph.addEdge("D", "T");

        DynamicArray<GraphNode<String>> path = PathFinder.findPath(graph, "S", "T");
        assertEquals(3, path.size(), "BFS should discover 2-hop path (3 nodes)");
        assertEquals("S", path.get(0).getId(), "Path node 0 = S");
        assertEquals("A", path.get(1).getId(), "Path node 1 = A");
        assertEquals("T", path.get(2).getId(), "Path node 2 = T");
    }

    private static void testAttackPathModelReconstruction() {
        DirectedGraph<AttackNode> graph = new DirectedGraph<>();
        AttackNode n1 = new AttackNode("N1", "10.0.1.1", "HOST-1", KillChainStage.RECONNAISSANCE, "10:00:00");
        AttackNode n2 = new AttackNode("N2", "10.0.1.1", "HOST-2", KillChainStage.LATERAL_MOVEMENT, "10:05:00");
        AttackNode n3 = new AttackNode("N3", "10.0.1.1", "HOST-3", KillChainStage.EXFILTRATION, "10:10:00");

        graph.addNode("N1", n1);
        graph.addNode("N2", n2);
        graph.addNode("N3", n3);
        graph.addEdge("N1", "N2");
        graph.addEdge("N2", "N3");

        AttackPath attackPath = PathFinder.findAttackPath(graph, "N1", "N3");
        assertFalse(attackPath.isEmpty(), "AttackPath should not be empty");
        assertEquals(3, attackPath.nodeCount(), "Should contain 3 nodes");
        assertEquals(2, attackPath.edgeCount(), "Should contain 2 edges");
        assertEquals(2, attackPath.length(), "Hop length should be 2");
        assertEquals("N1", attackPath.getStartNode().getId(), "Start node must be N1");
        assertEquals("N3", attackPath.getEndNode().getId(), "End node must be N3");
        assertEquals("N1 -> N2 -> N3", attackPath.toPathString(), "Formatted path should match expected string");
        assertTrue(attackPath.containsNode("N2"), "Path should contain N2");
    }

    private static void testUnreachableAttackPath() {
        DirectedGraph<AttackNode> graph = new DirectedGraph<>();
        AttackNode n1 = new AttackNode("N1", "10.0.1.1", "HOST-1", KillChainStage.RECONNAISSANCE, "10:00:00");
        AttackNode n2 = new AttackNode("N2", "10.0.1.1", "HOST-2", KillChainStage.EXFILTRATION, "10:05:00");
        graph.addNode("N1", n1);
        graph.addNode("N2", n2);

        AttackPath attackPath = PathFinder.findAttackPath(graph, "N1", "N2");
        assertTrue(attackPath.isEmpty(), "Unreachable attack path should be empty");
        assertEquals(0, attackPath.nodeCount(), "Node count should be 0");
        assertEquals(null, attackPath.getStartNode(), "Start node should be null");
    }
}
