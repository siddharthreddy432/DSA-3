package engine.alerting;

import engine.datastructures.DynamicArray;

/**
 * Unit and workflow tests for AlertManager in Phase 7.
 */
public class AlertManagerTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("Running AlertManagerTest...");

        testEmptyManager();
        testCreateAndRegisterAlerts();
        testManagerPendingCount();
        testPriorityDispatching();
        testClearAndReset();
        testNullAlertRejection();

        System.out.printf("AlertManagerTest Summary: Total=%d, Passed=%d, Failed=%d%n",
                testsRun, testsPassed, testsFailed);

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void testEmptyManager() {
        startTest("testEmptyManager");
        AlertManager manager = new AlertManager();
        assertEquals(0, manager.getPendingAlertCount(), "0 pending alerts initially");
        assertFalse(manager.hasPendingAlerts(), "No alerts pending");

        boolean threwNext = false;
        try {
            manager.getNextAlert();
        } catch (IllegalStateException e) {
            threwNext = true;
        }
        assertTrue(threwNext, "getNextAlert throws on empty manager");

        boolean threwPeek = false;
        try {
            manager.peekNextAlert();
        } catch (IllegalStateException e) {
            threwPeek = true;
        }
        assertTrue(threwPeek, "peekNextAlert throws on empty manager");
        passTest();
    }

    private static void testCreateAndRegisterAlerts() {
        startTest("testCreateAndRegisterAlerts");
        AlertManager manager = new AlertManager();
        Alert a1 = manager.createAndRegisterAlert("ALT-001", 3, 2, "HOST-A", "Exploit attempt");
        Alert a2 = manager.createAndRegisterAlert("ALT-002", 5, 4, "HOST-B", "Data exfiltration");

        assertEquals(2, manager.getPendingAlertCount(), "2 alerts registered");
        assertTrue(manager.hasPendingAlerts(), "Has pending alerts");
        assertEquals("ALT-002", manager.peekNextAlert().getAlertId(), "Peek shows higher severity ALT-002");
        passTest();
    }

    private static void testManagerPendingCount() {
        startTest("testManagerPendingCount");
        AlertManager manager = new AlertManager();
        for (int i = 1; i <= 5; i++) {
            manager.createAndRegisterAlert("A-" + i, 2, i, "SRC", "Msg " + i);
            assertEquals(i, manager.getPendingAlertCount(), "Pending count increments");
        }

        manager.getNextAlert();
        assertEquals(4, manager.getPendingAlertCount(), "Pending count decrements after getNextAlert");

        DynamicArray<Alert> remaining = manager.drainAllAlerts();
        assertEquals(4, remaining.size(), "4 alerts drained");
        assertEquals(0, manager.getPendingAlertCount(), "0 alerts pending after drain");
        assertFalse(manager.hasPendingAlerts(), "No pending alerts");
        passTest();
    }

    private static void testPriorityDispatching() {
        startTest("testPriorityDispatching");
        AlertManager manager = new AlertManager();
        // Register in mixed order
        manager.createAndRegisterAlert("LOW", 1, 1, "H1", "Low Priority");
        manager.createAndRegisterAlert("CRITICAL", 5, 3, "H2", "Critical Priority");
        manager.createAndRegisterAlert("HIGH", 4, 2, "H3", "High Priority");
        manager.createAndRegisterAlert("CRITICAL_DEEPER", 5, 5, "H4", "Critical deeper attack");

        // CRITICAL_DEEPER (sev 5, depth 5) > CRITICAL (sev 5, depth 3) > HIGH (sev 4) > LOW (sev 1)
        assertEquals("CRITICAL_DEEPER", manager.getNextAlert().getAlertId(), "Highest priority dispatched first");
        assertEquals("CRITICAL", manager.getNextAlert().getAlertId(), "Second highest dispatched");
        assertEquals("HIGH", manager.getNextAlert().getAlertId(), "Third highest dispatched");
        assertEquals("LOW", manager.getNextAlert().getAlertId(), "Lowest dispatched last");
        passTest();
    }

    private static void testClearAndReset() {
        startTest("testClearAndReset");
        AlertManager manager = new AlertManager();
        manager.createAndRegisterAlert("A1", 3, 1, "H", "M");
        manager.createAndRegisterAlert("A2", 4, 1, "H", "M");
        assertEquals(2, manager.getPendingAlertCount(), "2 pending alerts");

        manager.clear();
        assertEquals(0, manager.getPendingAlertCount(), "0 pending after clear");
        assertFalse(manager.hasPendingAlerts(), "No alerts pending after clear");

        // Verify manager can be reused cleanly
        manager.createAndRegisterAlert("A3", 5, 1, "H", "M");
        assertEquals(1, manager.getPendingAlertCount(), "1 alert after reuse");
        assertEquals("A3", manager.getNextAlert().getAlertId(), "A3 dispatched");
        passTest();
    }

    private static void testNullAlertRejection() {
        startTest("testNullAlertRejection");
        AlertManager manager = new AlertManager();
        boolean threw = false;
        try {
            manager.registerAlert(null);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw, "Null alert must be rejected");
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
