package engine.datastructures;

/**
 * Standalone test suite for {@link LinkedList}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 * Self-contained assertions verify head/tail pointer integrity, indexing, removals, and boundary cases.
 */
public class LinkedListTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running LinkedListTest...");

        runTest("testEmptyList", LinkedListTest::testEmptyList);
        runTest("testAddFirstOperations", LinkedListTest::testAddFirstOperations);
        runTest("testAddLastOperations", LinkedListTest::testAddLastOperations);
        runTest("testSingleElementRemoval", LinkedListTest::testSingleElementRemoval);
        runTest("testRemoveFirstAndLastMultiNode", LinkedListTest::testRemoveFirstAndLastMultiNode);
        runTest("testRemoveMiddleNode", LinkedListTest::testRemoveMiddleNode);
        runTest("testSetAndGet", LinkedListTest::testSetAndGet);
        runTest("testInvalidIndices", LinkedListTest::testInvalidIndices);
        runTest("testContainsNullAndDuplicates", LinkedListTest::testContainsNullAndDuplicates);
        runTest("testClearAndReuse", LinkedListTest::testClearAndReuse);

        System.out.println("LinkedListTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void testEmptyList() {
        LinkedList<String> list = new LinkedList<>();
        assertEquals(0, list.size(), "Size of empty list should be 0");
        assertTrue(list.isEmpty(), "Empty list should report isEmpty() == true");
        assertFalse(list.contains("X"), "Empty list should not contain element");

        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0), "get(0) on empty list");
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(0, "val"), "set(0) on empty list");
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0), "remove(0) on empty list");
        assertThrows(IllegalStateException.class, list::removeFirst, "removeFirst() on empty list");
        assertThrows(IllegalStateException.class, list::removeLast, "removeLast() on empty list");
    }

    private static void testAddFirstOperations() {
        LinkedList<Integer> list = new LinkedList<>();
        list.addFirst(10);
        assertEquals(1, list.size(), "Size after first addFirst");
        assertEquals(10, list.get(0), "Element 0 after first addFirst");

        list.addFirst(20);
        list.addFirst(30);
        assertEquals(3, list.size(), "Size after 3 addFirst operations");

        // Order should be 30, 20, 10
        assertEquals(30, list.get(0), "get(0)");
        assertEquals(20, list.get(1), "get(1)");
        assertEquals(10, list.get(2), "get(2)");
    }

    private static void testAddLastOperations() {
        LinkedList<String> list = new LinkedList<>();
        list.addLast("A");
        assertEquals(1, list.size(), "Size after first addLast");
        assertEquals("A", list.get(0), "Element 0 after first addLast");

        list.addLast("B");
        list.addLast("C");
        assertEquals(3, list.size(), "Size after 3 addLast operations");

        // Order should be A, B, C
        assertEquals("A", list.get(0), "get(0)");
        assertEquals("B", list.get(1), "get(1)");
        assertEquals("C", list.get(2), "get(2)");
    }

    private static void testSingleElementRemoval() {
        LinkedList<String> list = new LinkedList<>();
        list.addFirst("SOLO");
        assertEquals("SOLO", list.removeFirst(), "removeFirst on 1-element list");
        assertEquals(0, list.size(), "Size after removeFirst on 1-element list");
        assertTrue(list.isEmpty(), "isEmpty after removeFirst");

        list.addLast("SOLO2");
        assertEquals("SOLO2", list.removeLast(), "removeLast on 1-element list");
        assertEquals(0, list.size(), "Size after removeLast on 1-element list");
        assertTrue(list.isEmpty(), "isEmpty after removeLast");

        list.addFirst("SOLO3");
        assertEquals("SOLO3", list.remove(0), "remove(0) on 1-element list");
        assertEquals(0, list.size(), "Size after remove(0) on 1-element list");
        assertTrue(list.isEmpty(), "isEmpty after remove(0)");
    }

    private static void testRemoveFirstAndLastMultiNode() {
        LinkedList<String> list = new LinkedList<>();
        list.addLast("A");
        list.addLast("B");
        list.addLast("C");

        assertEquals("A", list.removeFirst(), "removeFirst returns head");
        assertEquals(2, list.size(), "Size after removeFirst");
        assertEquals("B", list.get(0), "New head after removeFirst");

        assertEquals("C", list.removeLast(), "removeLast returns tail");
        assertEquals(1, list.size(), "Size after removeLast");
        assertEquals("B", list.get(0), "Remaining element");

        assertEquals("B", list.removeFirst(), "remove last remaining node");
        assertEquals(0, list.size(), "Size should be 0");
        assertTrue(list.isEmpty(), "List should be empty");
    }

    private static void testRemoveMiddleNode() {
        LinkedList<String> list = new LinkedList<>();
        list.addLast("A");
        list.addLast("B");
        list.addLast("C");
        list.addLast("D");

        assertEquals("B", list.remove(1), "remove middle index 1");
        assertEquals(3, list.size(), "Size after removing index 1");
        assertEquals("A", list.get(0), "Index 0");
        assertEquals("C", list.get(1), "Index 1 should now be C");
        assertEquals("D", list.get(2), "Index 2 should now be D");

        assertEquals("C", list.remove(1), "remove middle index 1 again");
        assertEquals(2, list.size(), "Size after removing index 1");
        assertEquals("A", list.get(0), "Head remained A");
        assertEquals("D", list.get(1), "Tail remained D");
    }

    private static void testSetAndGet() {
        LinkedList<String> list = new LinkedList<>();
        list.addLast("A");
        list.addLast("B");
        list.addLast("C");

        String old = list.set(1, "MODIFIED");
        assertEquals("B", old, "set should return previous value");
        assertEquals("MODIFIED", list.get(1), "get should return modified value");
        assertEquals("A", list.get(0), "other elements unaffected");
        assertEquals("C", list.get(2), "other elements unaffected");
    }

    private static void testInvalidIndices() {
        LinkedList<String> list = new LinkedList<>();
        list.addLast("A");
        list.addLast("B");

        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1), "Negative index get");
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(2), "index == size get");
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(5), "index > size get");

        assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, "X"), "Negative index set");
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(2, "X"), "index == size set");

        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1), "Negative index remove");
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(2), "index == size remove");
    }

    private static void testContainsNullAndDuplicates() {
        LinkedList<String> list = new LinkedList<>();
        list.addLast("A");
        list.addLast(null);
        list.addLast("A");

        assertEquals(3, list.size(), "Size with null and duplicate");
        assertTrue(list.contains("A"), "contains('A')");
        assertTrue(list.contains(null), "contains(null)");
        assertFalse(list.contains("MISSING"), "contains('MISSING')");
    }

    private static void testClearAndReuse() {
        LinkedList<String> list = new LinkedList<>();
        list.addLast("A");
        list.addLast("B");
        list.clear();

        assertEquals(0, list.size(), "Size after clear");
        assertTrue(list.isEmpty(), "isEmpty after clear");
        assertFalse(list.contains("A"), "contains after clear");

        list.addFirst("REUSED_HEAD");
        list.addLast("REUSED_TAIL");
        assertEquals(2, list.size(), "Size after reuse");
        assertEquals("REUSED_HEAD", list.get(0), "Head after reuse");
        assertEquals("REUSED_TAIL", list.get(1), "Tail after reuse");
    }
}
