package engine.datastructures;

/**
 * Standalone test suite for {@link DynamicArray}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 * Self-contained assertions verify boundary conditions, indexing, resizing, and operations.
 */
public class DynamicArrayTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running DynamicArrayTest...");

        runTest("testEmptyArray", DynamicArrayTest::testEmptyArray);
        runTest("testSingleElement", DynamicArrayTest::testSingleElement);
        runTest("testMultipleElementsAppend", DynamicArrayTest::testMultipleElementsAppend);
        runTest("testDynamicResizing", DynamicArrayTest::testDynamicResizing);
        runTest("testAddAtIndex", DynamicArrayTest::testAddAtIndex);
        runTest("testRemoveOperations", DynamicArrayTest::testRemoveOperations);
        runTest("testInvalidIndexHandling", DynamicArrayTest::testInvalidIndexHandling);
        runTest("testNullAndDuplicateSupport", DynamicArrayTest::testNullAndDuplicateSupport);
        runTest("testClearAndReuse", DynamicArrayTest::testClearAndReuse);

        System.out.println("DynamicArrayTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void testEmptyArray() {
        DynamicArray<String> array = new DynamicArray<>();
        assertEquals(0, array.size(), "Initial size should be 0");
        assertTrue(array.isEmpty(), "Array should be empty initially");
        assertFalse(array.contains("X"), "Empty array should not contain element");

        assertThrows(IndexOutOfBoundsException.class, () -> array.get(0), "get(0) on empty array");
        assertThrows(IndexOutOfBoundsException.class, () -> array.remove(0), "remove(0) on empty array");
        assertThrows(IndexOutOfBoundsException.class, () -> array.set(0, "val"), "set(0) on empty array");
    }

    private static void testSingleElement() {
        DynamicArray<String> array = new DynamicArray<>();
        array.add("ALPHA");
        assertEquals(1, array.size(), "Size after 1 add should be 1");
        assertFalse(array.isEmpty(), "Array should not be empty");
        assertEquals("ALPHA", array.get(0), "get(0) should return added element");
        assertTrue(array.contains("ALPHA"), "contains should return true for added element");
        assertFalse(array.contains("BETA"), "contains should return false for missing element");

        String old = array.set(0, "OMEGA");
        assertEquals("ALPHA", old, "set should return previous element");
        assertEquals("OMEGA", array.get(0), "get(0) should reflect updated element");

        String removed = array.remove(0);
        assertEquals("OMEGA", removed, "remove should return removed element");
        assertEquals(0, array.size(), "Size after remove should be 0");
        assertTrue(array.isEmpty(), "Array should be empty after removing only element");
    }

    private static void testMultipleElementsAppend() {
        DynamicArray<Integer> array = new DynamicArray<>();
        for (int i = 0; i < 5; i++) {
            array.add(i * 10);
        }
        assertEquals(5, array.size(), "Size should be 5");
        for (int i = 0; i < 5; i++) {
            assertEquals(i * 10, array.get(i), "get(" + i + ") value check");
        }
    }

    private static void testDynamicResizing() {
        DynamicArray<Integer> array = new DynamicArray<>(2);
        assertEquals(2, array.getCapacity(), "Initial capacity should be 2");

        for (int i = 0; i < 50; i++) {
            array.add(i);
        }

        assertEquals(50, array.size(), "Size should be 50 after 50 insertions");
        assertTrue(array.getCapacity() >= 50, "Capacity should have grown to accommodate 50 items");

        for (int i = 0; i < 50; i++) {
            assertEquals(i, array.get(i), "Element at index " + i + " preserved during resize");
        }
    }

    private static void testAddAtIndex() {
        DynamicArray<String> array = new DynamicArray<>();
        array.add("B");
        array.add("D");

        // Insert at index 0 (prepend)
        array.add(0, "A");
        assertEquals(3, array.size(), "Size after prepend");
        assertEquals("A", array.get(0), "Index 0");
        assertEquals("B", array.get(1), "Index 1");
        assertEquals("D", array.get(2), "Index 2");

        // Insert in middle
        array.add(2, "C");
        assertEquals(4, array.size(), "Size after middle insert");
        assertEquals("A", array.get(0), "Index 0");
        assertEquals("B", array.get(1), "Index 1");
        assertEquals("C", array.get(2), "Index 2");
        assertEquals("D", array.get(3), "Index 3");

        // Insert at size (append)
        array.add(4, "E");
        assertEquals(5, array.size(), "Size after append via index");
        assertEquals("E", array.get(4), "Index 4");
    }

    private static void testRemoveOperations() {
        DynamicArray<String> array = new DynamicArray<>();
        array.add("A");
        array.add("B");
        array.add("C");
        array.add("D");
        array.add("E");

        // Remove middle
        String removedMiddle = array.remove(2); // "C"
        assertEquals("C", removedMiddle, "Removed middle element");
        assertEquals(4, array.size(), "Size after removing middle");
        assertEquals("D", array.get(2), "Element shifted left to index 2");

        // Remove first
        String removedFirst = array.remove(0); // "A"
        assertEquals("A", removedFirst, "Removed first element");
        assertEquals(3, array.size(), "Size after removing first");
        assertEquals("B", array.get(0), "Index 0 after removing first");

        // Remove last
        String removedLast = array.remove(array.size() - 1); // "E"
        assertEquals("E", removedLast, "Removed last element");
        assertEquals(2, array.size(), "Size after removing last");
        assertEquals("B", array.get(0), "Remaining index 0");
        assertEquals("D", array.get(1), "Remaining index 1");
    }

    private static void testInvalidIndexHandling() {
        DynamicArray<String> array = new DynamicArray<>();
        array.add("ONE");
        array.add("TWO");

        assertThrows(IndexOutOfBoundsException.class, () -> array.get(-1), "Negative index get");
        assertThrows(IndexOutOfBoundsException.class, () -> array.get(2), "index == size get");
        assertThrows(IndexOutOfBoundsException.class, () -> array.get(10), "index > size get");

        assertThrows(IndexOutOfBoundsException.class, () -> array.set(-1, "X"), "Negative index set");
        assertThrows(IndexOutOfBoundsException.class, () -> array.set(2, "X"), "index == size set");

        assertThrows(IndexOutOfBoundsException.class, () -> array.remove(-1), "Negative index remove");
        assertThrows(IndexOutOfBoundsException.class, () -> array.remove(2), "index == size remove");

        assertThrows(IndexOutOfBoundsException.class, () -> array.add(-1, "X"), "Negative index add");
        assertThrows(IndexOutOfBoundsException.class, () -> array.add(3, "X"), "index > size add");
    }

    private static void testNullAndDuplicateSupport() {
        DynamicArray<String> array = new DynamicArray<>();
        array.add("A");
        array.add(null);
        array.add("A"); // duplicate

        assertEquals(3, array.size(), "Size with null and duplicates");
        assertTrue(array.contains(null), "Array should contain null");
        assertTrue(array.contains("A"), "Array should contain 'A'");
        assertEquals(null, array.get(1), "Index 1 should be null");
    }

    private static void testClearAndReuse() {
        DynamicArray<String> array = new DynamicArray<>();
        array.add("FIRST");
        array.add("SECOND");
        array.clear();

        assertEquals(0, array.size(), "Size after clear");
        assertTrue(array.isEmpty(), "isEmpty after clear");
        assertFalse(array.contains("FIRST"), "contains after clear");

        array.add("NEW_FIRST");
        assertEquals(1, array.size(), "Size after reuse");
        assertEquals("NEW_FIRST", array.get(0), "get(0) after reuse");
    }
}
