package engine.optimization;

import engine.datastructures.DynamicArray;

/**
 * Standalone test suite for {@link MinCut}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 */
public class MinCutTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running MinCutTest...");

        runTest("testKnownMaxFlowMinCutEquivalence", MinCutTest::testKnownMaxFlowMinCutEquivalence);
        runTest("testSourceAndSinkPartitions", MinCutTest::testSourceAndSinkPartitions);
        runTest("testCutSeparatesSourceAndSink", MinCutTest::testCutSeparatesSourceAndSink);
        runTest("testCutEdgesIdentification", MinCutTest::testCutEdgesIdentification);
        runTest("testDisconnectedSinkMinCut", MinCutTest::testDisconnectedSinkMinCut);
        runTest("testMultiBottleneckCut", MinCutTest::testMultiBottleneckCut);
        runTest("testAsymmetricCapacityCut", MinCutTest::testAsymmetricCapacityCut);

        System.out.println("MinCutTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void testKnownMaxFlowMinCutEquivalence() {
        // S -> A (10), S -> B (10)
        // A -> T (10), B -> T (10)
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "A", 10);
        net.addEdge("S", "B", 10);
        net.addEdge("A", "T", 10);
        net.addEdge("B", "T", 10);

        MinCutResult cut = MinCut.compute(net, "S", "T");
        assertEquals(20, cut.getCutValue(), "MinCut value must equal MaxFlow (20)");
        assertTrue(cut.separatesSourceAndSink(), "MinCut must separate S and T");
    }

    private static void testSourceAndSinkPartitions() {
        // Linear network: S -> A (5) -> B (5) -> T (5)
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "A", 5);
        net.addEdge("A", "B", 5);
        net.addEdge("B", "T", 5);

        MinCutResult cut = MinCut.compute(net, "S", "T");
        assertEquals(5, cut.getCutValue(), "Cut value should be 5");
        assertTrue(cut.isSourceSide("S"), "S must be on source side");
        assertTrue(cut.isSinkSide("T"), "T must be on sink side");

        DynamicArray<String> sPart = cut.getSourcePartition();
        DynamicArray<String> tPart = cut.getSinkPartition();

        assertEquals(4, sPart.size() + tPart.size(), "All 4 vertices must be partitioned");
    }

    private static void testCutSeparatesSourceAndSink() {
        // S -> U (100) -> V (2) -> T (100)
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "U", 100);
        net.addEdge("U", "V", 2);
        net.addEdge("V", "T", 100);

        MinCutResult cut = MinCut.compute(net, "S", "T");
        assertEquals(2, cut.getCutValue(), "Bottleneck cut capacity is 2");

        // The saturated edge is U -> V. S and U should be on source side, V and T on sink side.
        assertTrue(cut.isSourceSide("S"), "S is in S");
        assertTrue(cut.isSourceSide("U"), "U is reachable in residual graph");
        assertTrue(cut.isSinkSide("V"), "V is in T");
        assertTrue(cut.isSinkSide("T"), "T is in T");
        assertTrue(cut.separatesSourceAndSink(), "Must separate S from T");
    }

    private static void testCutEdgesIdentification() {
        // S -> A (3), S -> B (4)
        // A -> T (3), B -> T (4)
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "A", 3);
        net.addEdge("S", "B", 4);
        net.addEdge("A", "T", 3);
        net.addEdge("B", "T", 4);

        MinCutResult cut = MinCut.compute(net, "S", "T");
        assertEquals(7, cut.getCutValue(), "Min cut value must be 7");

        DynamicArray<FlowEdge> cutEdges = cut.getCutEdges();
        assertTrue(cutEdges.size() > 0, "Must identify cut edges");

        int totalCutCap = 0;
        for (int i = 0; i < cutEdges.size(); i++) {
            FlowEdge e = cutEdges.get(i);
            assertTrue(cut.isSourceSide(e.getFrom()), "Cut edge source must be in S");
            assertTrue(cut.isSinkSide(e.getTo()), "Cut edge target must be in T");
            totalCutCap += e.getCapacity();
        }
        assertEquals(cut.getCutValue(), totalCutCap, "Sum of cut edge capacities must equal min cut value");
    }

    private static void testDisconnectedSinkMinCut() {
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "A", 10);
        net.addEdge("B", "T", 10); // S and T disconnected

        MinCutResult cut = MinCut.compute(net, "S", "T");
        assertEquals(0, cut.getCutValue(), "Disconnected network must have min cut 0");
        assertTrue(cut.isSourceSide("S"), "S is on source side");
        assertTrue(cut.isSourceSide("A"), "A is reachable from S");
        assertTrue(cut.isSinkSide("T"), "T is unreachable from S");
        assertTrue(cut.isSinkSide("B"), "B is unreachable from S");
        assertEquals(0, cut.getCutEdges().size(), "Zero cut edges crossing S to T");
    }

    private static void testMultiBottleneckCut() {
        // S -> A (10), S -> B (20)
        // A -> C (5), B -> C (5)
        // C -> T (50)
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "A", 10);
        net.addEdge("S", "B", 20);
        net.addEdge("A", "C", 5);
        net.addEdge("B", "C", 5);
        net.addEdge("C", "T", 50);

        MinCutResult cut = MinCut.compute(net, "S", "T");
        assertEquals(10, cut.getCutValue(), "Bottlenecks A->C and B->C total capacity 10");
        assertTrue(cut.isSourceSide("S"), "S is in S");
        assertTrue(cut.isSourceSide("A"), "A is in S");
        assertTrue(cut.isSourceSide("B"), "B is in S");
        assertTrue(cut.isSinkSide("C"), "C is in T");
        assertTrue(cut.isSinkSide("T"), "T is in T");
    }

    private static void testAsymmetricCapacityCut() {
        // Upper branch: S -> U (1) -> T (100)
        // Lower branch: S -> L (100) -> T (2)
        FlowNetwork net = new FlowNetwork();
        net.addEdge("S", "U", 1);
        net.addEdge("U", "T", 100);
        net.addEdge("S", "L", 100);
        net.addEdge("L", "T", 2);

        MinCutResult cut = MinCut.compute(net, "S", "T");
        assertEquals(3, cut.getCutValue(), "Cut value should be 1 + 2 = 3");
        assertTrue(cut.separatesSourceAndSink(), "Must separate S and T");
    }
}
