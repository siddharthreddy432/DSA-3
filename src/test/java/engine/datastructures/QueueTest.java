package engine.datastructures;

/**
 * Standalone test suite for {@link Queue}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 * Self-contained assertions verify FIFO ordering, circular wrap-around, dynamic resizing with re-alignment,
 * and underflow protections.
 */
public class QueueTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running QueueTest...");

        runTest("testEmptyQueue", QueueTest::testEmptyQueue);
        runTest("testEnqueueSingle", QueueTest::testEnqueueSingle);
        runTest("testFIFOOrdering", QueueTest::testFIFOOrdering);
        runTest("testCircularWrapAround", QueueTest::testCircularWrapAround);
        runTest("testWrapAroundThenResize", QueueTest::testWrapAroundThenResize);
        runTest("testResizeThenWrapAround", QueueTest::testResizeThenWrapAround);
        runTest("testClearAndReuse", QueueTest::testClearAndReuse);
        runTest("testNullAndDuplicates", QueueTest::testNullAndDuplicates);
        runTest("testUnderflowProtection", QueueTest::testUnderflowProtection);

        System.out.println("QueueTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion failed: " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
    }

    private static void assertThrows(Class<? extends Throwable> expected, Runnable action, String message) {
        try {
            action.run();
            throw new AssertionError("Assertion failed: Expected " + expected.getName() + " was not thrown for " + message);
        } catch (Throwable t) {
            if (!expected.isInstance(t)) {
                throw new AssertionError("Assertion failed: Expected " + expected.getName() + " but caught " + t.getClass().getName() + " for " + message);
            }
        }
    }

    private static void testEmptyQueue() {
        Queue<String> queue = new Queue<>();
        assertEquals(0, queue.size(), "Size of empty queue should be 0");
        assertTrue(queue.isEmpty(), "isEmpty() should be true for empty queue");

        assertThrows(IllegalStateException.class, queue::dequeue, "dequeue() on empty queue");
        assertThrows(IllegalStateException.class, queue::peek, "peek() on empty queue");
    }

    private static void testEnqueueSingle() {
        Queue<String> queue = new Queue<>();
        queue.enqueue("MSG_1");
        assertEquals(1, queue.size(), "Size should be 1 after enqueue");
        assertFalse(queue.isEmpty(), "isEmpty should be false");
        assertEquals("MSG_1", queue.peek(), "peek should return front item");
        assertEquals("MSG_1", queue.dequeue(), "dequeue should return front item");
        assertEquals(0, queue.size(), "Size should be 0 after dequeue");
        assertTrue(queue.isEmpty(), "isEmpty should be true after dequeue");
    }

    private static void testFIFOOrdering() {
        Queue<String> queue = new Queue<>();
        queue.enqueue("FIRST");
        queue.enqueue("SECOND");
        queue.enqueue("THIRD");

        assertEquals("FIRST", queue.dequeue(), "FIFO item 1");
        assertEquals("SECOND", queue.dequeue(), "FIFO item 2");
        assertEquals("THIRD", queue.dequeue(), "FIFO item 3");
        assertTrue(queue.isEmpty(), "Queue should be empty after draining");
    }

    private static void testCircularWrapAround() {
        Queue<String> queue = new Queue<>(4);
        queue.enqueue("A");
        queue.enqueue("B");
        queue.enqueue("C");

        assertEquals("A", queue.dequeue(), "Dequeue A");
        assertEquals("B", queue.dequeue(), "Dequeue B");

        // Internal buffer: front has advanced to index 2.
        // Enqueue D and E: rear should wrap around to index 0 and 1.
        queue.enqueue("D");
        queue.enqueue("E");

        assertEquals(3, queue.size(), "Size after wrap-around enqueue");
        assertEquals("C", queue.dequeue(), "Dequeue C (at index 2)");
        assertEquals("D", queue.dequeue(), "Dequeue D (wrapped to index 3)");
        assertEquals("E", queue.dequeue(), "Dequeue E (wrapped to index 0)");
        assertTrue(queue.isEmpty(), "Queue empty after wrap-around drain");
    }

    private static void testWrapAroundThenResize() {
        // Initial capacity 4
        Queue<String> queue = new Queue<>(4);
        queue.enqueue("A");
        queue.enqueue("B");

        // Advance front by dequeuing A and B
        assertEquals("A", queue.dequeue(), "Dequeue A");
        assertEquals("B", queue.dequeue(), "Dequeue B");

        // Fill buffer in wrapped state
        queue.enqueue("C");
        queue.enqueue("D");
        queue.enqueue("E");
        queue.enqueue("F");

        assertEquals(4, queue.size(), "Queue is full at capacity 4");
        assertEquals(4, queue.getCapacity(), "Capacity before resize");

        // Enqueue G triggers resize: buffer must linearize elements C, D, E, F, G
        queue.enqueue("G");

        assertEquals(5, queue.size(), "Size after resize");
        assertEquals(8, queue.getCapacity(), "Capacity should have doubled to 8");

        // Verify that logical FIFO order is strictly preserved
        assertEquals("C", queue.dequeue(), "FIFO 1 after resize");
        assertEquals("D", queue.dequeue(), "FIFO 2 after resize");
        assertEquals("E", queue.dequeue(), "FIFO 3 after resize");
        assertEquals("F", queue.dequeue(), "FIFO 4 after resize");
        assertEquals("G", queue.dequeue(), "FIFO 5 after resize");
        assertTrue(queue.isEmpty(), "Queue empty after drain");
    }

    private static void testResizeThenWrapAround() {
        Queue<Integer> queue = new Queue<>(2);
        for (int i = 0; i < 10; i++) {
            queue.enqueue(i);
        }
        assertEquals(10, queue.size(), "Size after 10 insertions");

        for (int i = 0; i < 8; i++) {
            assertEquals(i, queue.dequeue(), "Dequeue " + i);
        }
        assertEquals(2, queue.size(), "Size after dequeuing 8 elements");

        for (int i = 10; i < 15; i++) {
            queue.enqueue(i);
        }

        // Dequeue remaining 8 and 9, then 10 through 14
        assertEquals(8, queue.dequeue(), "FIFO check 8");
        assertEquals(9, queue.dequeue(), "FIFO check 9");
        for (int i = 10; i < 15; i++) {
            assertEquals(i, queue.dequeue(), "FIFO check " + i);
        }
        assertTrue(queue.isEmpty(), "Empty after wrap test");
    }

    private static void testClearAndReuse() {
        Queue<String> queue = new Queue<>(4);
        queue.enqueue("A");
        queue.enqueue("B");
        queue.clear();

        assertEquals(0, queue.size(), "Size after clear");
        assertTrue(queue.isEmpty(), "isEmpty after clear");
        assertThrows(IllegalStateException.class, queue::dequeue, "dequeue after clear");

        queue.enqueue("REUSED");
        assertEquals(1, queue.size(), "Size after reuse enqueue");
        assertEquals("REUSED", queue.dequeue(), "Dequeue after reuse");
    }

    private static void testNullAndDuplicates() {
        Queue<String> queue = new Queue<>();
        queue.enqueue("DUP");
        queue.enqueue(null);
        queue.enqueue("DUP");

        assertEquals(3, queue.size(), "Size with null and duplicate");
        assertEquals("DUP", queue.dequeue(), "First duplicate");
        assertEquals(null, queue.dequeue(), "Null item");
        assertEquals("DUP", queue.dequeue(), "Second duplicate");
    }

    private static void testUnderflowProtection() {
        Queue<String> queue = new Queue<>();
        queue.enqueue("ONLY");
        assertEquals("ONLY", queue.dequeue(), "Dequeue only");
        assertTrue(queue.isEmpty(), "Empty after dequeue");

        assertThrows(IllegalStateException.class, queue::dequeue, "dequeue after empty");
        assertThrows(IllegalStateException.class, queue::peek, "peek after empty");
    }
}
