package engine.datastructures;

/**
 * Standalone test suite for {@link Stack}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 * Self-contained assertions verify LIFO ordering, underflow protections, capacity expansion, and null handling.
 */
public class StackTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running StackTest...");

        runTest("testEmptyStack", StackTest::testEmptyStack);
        runTest("testPushSingle", StackTest::testPushSingle);
        runTest("testLIFOOrdering", StackTest::testLIFOOrdering);
        runTest("testPeekBehavior", StackTest::testPeekBehavior);
        runTest("testPopUntilEmptyAndUnderflow", StackTest::testPopUntilEmptyAndUnderflow);
        runTest("testClearAndReuse", StackTest::testClearAndReuse);
        runTest("testNullAndDuplicates", StackTest::testNullAndDuplicates);
        runTest("testLargeVolumeExpansion", StackTest::testLargeVolumeExpansion);

        System.out.println("StackTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void testEmptyStack() {
        Stack<String> stack = new Stack<>();
        assertEquals(0, stack.size(), "Size of empty stack should be 0");
        assertTrue(stack.isEmpty(), "isEmpty() should be true for empty stack");

        assertThrows(IllegalStateException.class, stack::pop, "pop() on empty stack");
        assertThrows(IllegalStateException.class, stack::peek, "peek() on empty stack");
    }

    private static void testPushSingle() {
        Stack<String> stack = new Stack<>();
        stack.push("ITEM_1");
        assertEquals(1, stack.size(), "Size should be 1 after 1 push");
        assertFalse(stack.isEmpty(), "isEmpty() should be false after push");
        assertEquals("ITEM_1", stack.peek(), "peek() should return top item");
        assertEquals("ITEM_1", stack.pop(), "pop() should return top item");
        assertEquals(0, stack.size(), "Size should be 0 after pop");
        assertTrue(stack.isEmpty(), "isEmpty() should be true after pop");
    }

    private static void testLIFOOrdering() {
        Stack<String> stack = new Stack<>();
        stack.push("A");
        stack.push("B");
        stack.push("C");

        assertEquals("C", stack.pop(), "First pop should return C");
        assertEquals("B", stack.pop(), "Second pop should return B");
        assertEquals("A", stack.pop(), "Third pop should return A");
        assertTrue(stack.isEmpty(), "Stack should be empty after all pops");
    }

    private static void testPeekBehavior() {
        Stack<String> stack = new Stack<>();
        stack.push("TOP");

        assertEquals("TOP", stack.peek(), "First peek");
        assertEquals("TOP", stack.peek(), "Second peek");
        assertEquals(1, stack.size(), "Peek must not change size");
    }

    private static void testPopUntilEmptyAndUnderflow() {
        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        stack.push(2);

        assertEquals(2, stack.pop(), "Pop 2");
        assertEquals(1, stack.pop(), "Pop 1");
        assertTrue(stack.isEmpty(), "Stack is now empty");

        assertThrows(IllegalStateException.class, stack::pop, "Pop after underflow");
    }

    private static void testClearAndReuse() {
        Stack<String> stack = new Stack<>();
        stack.push("A");
        stack.push("B");
        stack.clear();

        assertEquals(0, stack.size(), "Size after clear");
        assertTrue(stack.isEmpty(), "isEmpty after clear");
        assertThrows(IllegalStateException.class, stack::pop, "pop after clear");

        stack.push("REUSED");
        assertEquals(1, stack.size(), "Size after reuse push");
        assertEquals("REUSED", stack.pop(), "pop after reuse");
    }

    private static void testNullAndDuplicates() {
        Stack<String> stack = new Stack<>();
        stack.push("DUP");
        stack.push(null);
        stack.push("DUP");

        assertEquals(3, stack.size(), "Size with null and duplicate");
        assertEquals("DUP", stack.pop(), "Pop top duplicate");
        assertEquals(null, stack.pop(), "Pop null");
        assertEquals("DUP", stack.pop(), "Pop bottom duplicate");
    }

    private static void testLargeVolumeExpansion() {
        Stack<Integer> stack = new Stack<>(4);
        int count = 1000;
        for (int i = 0; i < count; i++) {
            stack.push(i);
        }
        assertEquals(count, stack.size(), "Size after 1000 pushes");

        for (int i = count - 1; i >= 0; i--) {
            assertEquals(i, stack.pop(), "LIFO order verification on volume test");
        }
        assertTrue(stack.isEmpty(), "Stack empty after 1000 pops");
    }
}
