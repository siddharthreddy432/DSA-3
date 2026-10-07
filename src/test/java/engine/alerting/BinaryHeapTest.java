package engine.alerting;

/**
 * Unit and algorithmic validation tests for custom BinaryHeap in Phase 7.
 */
public class BinaryHeapTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("Running BinaryHeapTest...");

        testEmptyHeap();
        testSingleElement();
        testMultipleElementsMaxHeapOrder();
        testDuplicatePriorities();
        testExtractUntilEmpty();
        testClearAndReuse();
        testNullRejection();
        testIntegerMaxHeapStress();

        System.out.printf("BinaryHeapTest Summary: Total=%d, Passed=%d, Failed=%d%n",
                testsRun, testsPassed, testsFailed);

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void testEmptyHeap() {
        startTest("testEmptyHeap");
        BinaryHeap<Integer> heap = new BinaryHeap<>(Integer::compare);
        assertTrue(heap.isEmpty(), "Heap should be empty initially");
        assertEquals(0, heap.size(), "Size should be 0");

        boolean peekThrows = false;
        try {
            heap.peek();
        } catch (IllegalStateException e) {
            peekThrows = true;
        }
        assertTrue(peekThrows, "peek() on empty heap must throw IllegalStateException");

        boolean extractThrows = false;
        try {
            heap.extractMax();
        } catch (IllegalStateException e) {
            extractThrows = true;
        }
        assertTrue(extractThrows, "extractMax() on empty heap must throw IllegalStateException");
        passTest();
    }

    private static void testSingleElement() {
        startTest("testSingleElement");
        BinaryHeap<Integer> heap = new BinaryHeap<>(Integer::compare);
        heap.insert(42);
        assertFalse(heap.isEmpty(), "Heap not empty");
        assertEquals(1, heap.size(), "Size is 1");
        assertEquals(42, (int) heap.peek(), "Peek returns inserted element");
        assertEquals(42, (int) heap.extractMax(), "Extract returns inserted element");
        assertTrue(heap.isEmpty(), "Heap empty after extract");
        assertEquals(0, heap.size(), "Size is 0 after extract");
        passTest();
    }

    private static void testMultipleElementsMaxHeapOrder() {
        startTest("testMultipleElementsMaxHeapOrder");
        BinaryHeap<Integer> heap = new BinaryHeap<>(Integer::compare);
        int[] values = {10, 4, 15, 20, 0, 8, 30, 2};
        for (int v : values) {
            heap.insert(v);
        }

        assertEquals(values.length, heap.size(), "Size matches inserted count");

        // Should extract in descending order: 30, 20, 15, 10, 8, 4, 2, 0
        int[] expected = {30, 20, 15, 10, 8, 4, 2, 0};
        for (int exp : expected) {
            assertEquals(exp, (int) heap.extractMax(), "Extracted element matches descending order");
        }
        assertTrue(heap.isEmpty(), "Heap is empty after extracting all");
        passTest();
    }

    private static void testDuplicatePriorities() {
        startTest("testDuplicatePriorities");
        BinaryHeap<Integer> heap = new BinaryHeap<>(Integer::compare);
        heap.insert(5);
        heap.insert(5);
        heap.insert(10);
        heap.insert(5);
        heap.insert(10);

        assertEquals(5, heap.size(), "Size is 5");
        assertEquals(10, (int) heap.extractMax(), "First is 10");
        assertEquals(10, (int) heap.extractMax(), "Second is 10");
        assertEquals(5, (int) heap.extractMax(), "Third is 5");
        assertEquals(5, (int) heap.extractMax(), "Fourth is 5");
        assertEquals(5, (int) heap.extractMax(), "Fifth is 5");
        assertTrue(heap.isEmpty(), "Heap is empty");
        passTest();
    }

    private static void testExtractUntilEmpty() {
        startTest("testExtractUntilEmpty");
        BinaryHeap<String> heap = new BinaryHeap<>(String::compareTo);
        heap.insert("B");
        heap.insert("A");
        heap.insert("C");

        assertEquals("C", heap.extractMax(), "Max is C");
        assertEquals("B", heap.extractMax(), "Max is B");
        assertEquals("A", heap.extractMax(), "Max is A");
        assertTrue(heap.isEmpty(), "Heap is empty");

        boolean threw = false;
        try {
            heap.extractMax();
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue(threw, "Extracting from empty heap throws IllegalStateException");
        passTest();
    }

    private static void testClearAndReuse() {
        startTest("testClearAndReuse");
        BinaryHeap<Integer> heap = new BinaryHeap<>(Integer::compare);
        heap.insert(100);
        heap.insert(200);
        assertEquals(2, heap.size(), "Size is 2");

        heap.clear();
        assertEquals(0, heap.size(), "Size is 0 after clear");
        assertTrue(heap.isEmpty(), "Heap is empty after clear");

        heap.insert(50);
        assertEquals(1, heap.size(), "Size is 1 after reuse");
        assertEquals(50, (int) heap.extractMax(), "Extracted item is 50");
        passTest();
    }

    private static void testNullRejection() {
        startTest("testNullRejection");
        BinaryHeap<Integer> heap = new BinaryHeap<>(Integer::compare);
        boolean caught = false;
        try {
            heap.insert(null);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Null insert must be rejected");

        boolean caughtComp = false;
        try {
            new BinaryHeap<Integer>(null);
        } catch (IllegalArgumentException e) {
            caughtComp = true;
        }
        assertTrue(caughtComp, "Null comparator must be rejected");
        passTest();
    }

    private static void testIntegerMaxHeapStress() {
        startTest("testIntegerMaxHeapStress");
        BinaryHeap<Integer> heap = new BinaryHeap<>(Integer::compare);
        int n = 100;
        // Insert values in pseudo-random sequence: (i * 37) % 100
        for (int i = 0; i < n; i++) {
            heap.insert((i * 37) % 100);
        }
        assertEquals(n, heap.size(), "Size is 100");

        int prev = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            int curr = heap.extractMax();
            assertTrue(curr <= prev, "Elements extracted in monotonic non-increasing order: " + curr + " <= " + prev);
            prev = curr;
        }
        assertTrue(heap.isEmpty(), "Heap is empty after stress test");
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
