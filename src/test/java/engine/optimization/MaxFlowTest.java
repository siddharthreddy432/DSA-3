package engine.optimization;

/**
 * Standalone test suite for {@link MaxFlow}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 */
public class MaxFlowTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running MaxFlowTest...");

        runTest("testSinglePath", MaxFlowTest::testSinglePath);
        runTest("testTwoIndependentPaths", MaxFlowTest::testTwoIndependentPaths);
        runTest("testBottleneckCapacity", MaxFlowTest::testBottleneckCapacity);
        runTest("testMultipleAugmentingPaths", MaxFlowTest::testMultipleAugmentingPaths);
        runTest("testZeroCapacityEdge", MaxFlowTest::testZeroCapacityEdge);
        runTest("testDisconnectedSink", MaxFlowTest::testDisconnectedSink);
        runTest("testCyclicNetwork", MaxFlowTest::testCyclicNetwork);
        runTest("testSourceEqualsSinkValidation", MaxFlowTest::testSourceEqualsSinkValidation);
        runTest("testMissingVertexValidation", MaxFlowTest::testMissingVertexValidation);
        runTest("testResidualGraphCorrectness", MaxFlowTest::testResidualGraphCorrectness);

        System.out.println("MaxFlowTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion failed: " + message + " (expected: " + expected + ", got: " + actual + ")");
    }

    private static void testSinglePath() {
        // A -> B (capacity 10) -> C (capacity 10)
        FlowNetwork net = new FlowNetwork();
        net.addEdge("A", "B", 10);
        net.addEdge("B", "C", 10);

        MaxFlowResult result = MaxFlow.compute(net, "A", "C");
        assertEquals(10, result.getMaxFlow(), "Single path max flow must equal path capacity 10");
        assertEquals(10, result.getFlow("A", "B"), "Flow A->B must be 10");
        assertEquals(10, result.getFlow("B", "C"), "Flow B->C must be 10");
    }

    private static void testTwoIndependentPaths() {
        // Path 1: S -> A -> T (cap 5)
        // Path 2: S -> B -> T (cap 7)
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "A", 5);
        net.addEdge("A", "T", 5);
        net.addEdge("S", "B", 7);
        net.addEdge("B", "T", 7);

        MaxFlowResult result = MaxFlow.compute(net, "S", "T");
        assertEquals(12, result.getMaxFlow(), "Independent paths sum 5 + 7 = 12");
        assertEquals(5, result.getFlow("S", "A"), "Flow on path A");
        assertEquals(7, result.getFlow("S", "B"), "Flow on path B");
    }

    private static void testBottleneckCapacity() {
        // S -> A (cap 100) -> B (cap 15) -> T (cap 80)
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "A", 100);
        net.addEdge("A", "B", 15);
        net.addEdge("B", "T", 80);

        MaxFlowResult result = MaxFlow.compute(net, "S", "T");
        assertEquals(15, result.getMaxFlow(), "Max flow limited by narrowest bottleneck 15");
    }

    private static void testMultipleAugmentingPaths() {
        // Classical network flow example:
        // S -> A (10), S -> B (10)
        // A -> B (2), A -> T (4)
        // B -> T (8)
        // Max flow should be: 4 (via A->T) + 10 (via S->B + A->B to T) = 10?
        // Path 1: S -> A -> T (4)
        // Path 2: S -> A -> B -> T (2)
        // Path 3: S -> B -> T (6)
        // Total = 4 + 2 + 6 = 12
        // Wait: S->B has capacity 10, B->T has capacity 8.
        // S sends 6 to A: 4 to T, 2 to B.
        // S sends 6 to B: total into B is 8, out of B to T is 8.
        // Total flow into T is 4 + 8 = 12. S can push 6 + 6 = 12 <= 10+10. Max flow = 12.
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "A", 10);
        net.addEdge("S", "B", 10);
        net.addEdge("A", "B", 2);
        net.addEdge("A", "T", 4);
        net.addEdge("B", "T", 8);

        MaxFlowResult result = MaxFlow.compute(net, "S", "T");
        assertEquals(12, result.getMaxFlow(), "Multiple augmenting paths max flow should be 12");
    }

    private static void testZeroCapacityEdge() {
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "A", 10);
        net.addEdge("A", "T", 0); // zero capacity block
        net.addEdge("S", "B", 5);
        net.addEdge("B", "T", 5);

        MaxFlowResult result = MaxFlow.compute(net, "S", "T");
        assertEquals(5, result.getMaxFlow(), "Zero-capacity edge A->T should carry 0 flow");
        assertEquals(0, result.getFlow("A", "T"), "Flow through zero-cap edge must be 0");
    }

    private static void testDisconnectedSink() {
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "A", 10);
        net.addEdge("B", "T", 10); // A and B disconnected

        MaxFlowResult result = MaxFlow.compute(net, "S", "T");
        assertEquals(0, result.getMaxFlow(), "Disconnected sink must yield 0 max flow");
    }

    private static void testCyclicNetwork() {
        // S -> A (10), A -> B (5), B -> A (5) [cycle], B -> T (10)
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "A", 10);
        net.addEdge("A", "B", 5);
        net.addEdge("B", "A", 5);
        net.addEdge("B", "T", 10);

        MaxFlowResult result = MaxFlow.compute(net, "S", "T");
        assertEquals(5, result.getMaxFlow(), "Max flow in cyclic network must be 5");
    }

    private static void testSourceEqualsSinkValidation() {
        FlowNetwork net = new FlowNetwork();
        net.addEdge("A", "B", 10);

        boolean caught = false;
        try {
            MaxFlow.compute(net, "A", "A");
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Source equals sink must throw IllegalArgumentException");
    }

    private static void testMissingVertexValidation() {
        FlowNetwork net = new FlowNetwork();
        net.addEdge("A", "B", 10);

        boolean caught = false;
        try {
            MaxFlow.compute(net, "A", "NON_EXISTENT");
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Missing vertex must throw IllegalArgumentException");
    }

    private static void testResidualGraphCorrectness() {
        // S -> M (10) -> T (10)
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "M", 10);
        net.addEdge("M", "T", 10);

        MaxFlowResult result = MaxFlow.compute(net, "S", "T");
        assertEquals(10, result.getMaxFlow(), "Max flow is 10");

        FlowNetwork residual = result.getFlowNetwork();
        int sIdx = residual.getVertexIndex("S");
        int mIdx = residual.getVertexIndex("M");
        int tIdx = residual.getVertexIndex("T");

        // Forward residual capacity S->M should be 10 - 10 = 0
        assertEquals(0, residual.getResidualCapacity(sIdx, mIdx), "Forward residual S->M must be 0");
        // Backward residual capacity M->S should be 10
        assertEquals(10, residual.getResidualCapacity(mIdx, sIdx), "Reverse residual M->S must be 10");
        // Forward residual M->T should be 0
        assertEquals(0, residual.getResidualCapacity(mIdx, tIdx), "Forward residual M->T must be 0");
    }
}
