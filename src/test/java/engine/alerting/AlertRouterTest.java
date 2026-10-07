package engine.alerting;

import engine.datastructures.DynamicArray;

/**
 * Unit and priority routing tests for AlertRouter in Phase 7.
 */
public class AlertRouterTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("Running AlertRouterTest...");

        testSpecificationExampleOrdering();
        testSeverityDominatesDepth();
        testDepthTieBreaker();
        testArrivalSequenceTieBreaker();
        testDrainAlertsPreservesOrder();
        testEmptyRouterValidation();
        testRouteAllMultiple();

        System.out.printf("AlertRouterTest Summary: Total=%d, Passed=%d, Failed=%d%n",
                testsRun, testsPassed, testsFailed);

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void testSpecificationExampleOrdering() {
        startTest("testSpecificationExampleOrdering");
        // Spec Example:
        // Alert A severity 3 depth 2
        // Alert B severity 5 depth 1
        // Alert C severity 5 depth 4
        // Alert D severity 2 depth 8
        // Expected order: C, B, A, D
        AlertRouter router = new AlertRouter();
        Alert aA = new Alert("A", 3, 2, "HOST-A", "Msg A", 1L);
        Alert aB = new Alert("B", 5, 1, "HOST-B", "Msg B", 2L);
        Alert aC = new Alert("C", 5, 4, "HOST-C", "Msg C", 3L);
        Alert aD = new Alert("D", 2, 8, "HOST-D", "Msg D", 4L);

        router.routeAlert(aA);
        router.routeAlert(aB);
        router.routeAlert(aC);
        router.routeAlert(aD);

        assertEquals(4, router.getPendingAlertCount(), "Pending count is 4");
        assertEquals("C", router.getNextAlert().getAlertId(), "First is C (sev 5, depth 4)");
        assertEquals("B", router.getNextAlert().getAlertId(), "Second is B (sev 5, depth 1)");
        assertEquals("A", router.getNextAlert().getAlertId(), "Third is A (sev 3, depth 2)");
        assertEquals("D", router.getNextAlert().getAlertId(), "Fourth is D (sev 2, depth 8)");
        assertFalse(router.hasPendingAlerts(), "No alerts remaining");
        passTest();
    }

    private static void testSeverityDominatesDepth() {
        startTest("testSeverityDominatesDepth");
        AlertRouter router = new AlertRouter();
        Alert highSevShallow = new Alert("ALT-HIGH", 5, 1, "HOST-1", "High Severity", 1L);
        Alert lowSevDeep = new Alert("ALT-LOW", 1, 10, "HOST-2", "Low Severity", 2L);

        router.routeAlert(lowSevDeep);
        router.routeAlert(highSevShallow);

        assertEquals("ALT-HIGH", router.getNextAlert().getAlertId(), "Higher severity takes priority over depth");
        assertEquals("ALT-LOW", router.getNextAlert().getAlertId(), "Lower severity is second");
        passTest();
    }

    private static void testDepthTieBreaker() {
        startTest("testDepthTieBreaker");
        AlertRouter router = new AlertRouter();
        Alert a1 = new Alert("A1", 4, 2, "H1", "Depth 2", 1L);
        Alert a2 = new Alert("A2", 4, 7, "H2", "Depth 7", 2L);
        Alert a3 = new Alert("A3", 4, 5, "H3", "Depth 5", 3L);

        router.routeAlert(a1);
        router.routeAlert(a2);
        router.routeAlert(a3);

        assertEquals("A2", router.getNextAlert().getAlertId(), "Depth 7 wins on same severity 4");
        assertEquals("A3", router.getNextAlert().getAlertId(), "Depth 5 is next");
        assertEquals("A1", router.getNextAlert().getAlertId(), "Depth 2 is last");
        passTest();
    }

    private static void testArrivalSequenceTieBreaker() {
        startTest("testArrivalSequenceTieBreaker");
        AlertRouter router = new AlertRouter();
        // Identical severity (4) and identical depth (3)
        Alert first = new Alert("FIRST", 4, 3, "H1", "Arrived first", 100L);
        Alert second = new Alert("SECOND", 4, 3, "H2", "Arrived second", 101L);
        Alert third = new Alert("THIRD", 4, 3, "H3", "Arrived third", 102L);

        router.routeAlert(third);
        router.routeAlert(first);
        router.routeAlert(second);

        assertEquals("FIRST", router.getNextAlert().getAlertId(), "Earlier sequence 100 wins");
        assertEquals("SECOND", router.getNextAlert().getAlertId(), "Sequence 101 is next");
        assertEquals("THIRD", router.getNextAlert().getAlertId(), "Sequence 102 is last");
        passTest();
    }

    private static void testDrainAlertsPreservesOrder() {
        startTest("testDrainAlertsPreservesOrder");
        AlertRouter router = new AlertRouter();
        router.routeAlert(new Alert("A1", 1, 1, "H", "M", 1L));
        router.routeAlert(new Alert("A3", 5, 1, "H", "M", 2L));
        router.routeAlert(new Alert("A2", 3, 1, "H", "M", 3L));

        DynamicArray<Alert> drained = router.drainAlerts();
        assertEquals(3, drained.size(), "Drained count is 3");
        assertEquals("A3", drained.get(0).getAlertId(), "Top alert is A3");
        assertEquals("A2", drained.get(1).getAlertId(), "Second alert is A2");
        assertEquals("A1", drained.get(2).getAlertId(), "Third alert is A1");
        assertEquals(0, router.getPendingAlertCount(), "Router is empty after drain");
        passTest();
    }

    private static void testEmptyRouterValidation() {
        startTest("testEmptyRouterValidation");
        AlertRouter router = new AlertRouter();
        assertFalse(router.hasPendingAlerts(), "Empty router has no pending alerts");
        assertEquals(0, router.getPendingAlertCount(), "0 pending alerts");

        boolean threwNext = false;
        try {
            router.getNextAlert();
        } catch (IllegalStateException e) {
            threwNext = true;
        }
        assertTrue(threwNext, "getNextAlert throws on empty");

        boolean threwPeek = false;
        try {
            router.peekNextAlert();
        } catch (IllegalStateException e) {
            threwPeek = true;
        }
        assertTrue(threwPeek, "peekNextAlert throws on empty");

        boolean threwNull = false;
        try {
            router.routeAlert(null);
        } catch (IllegalArgumentException e) {
            threwNull = true;
        }
        assertTrue(threwNull, "routeAlert(null) throws IllegalArgumentException");
        passTest();
    }

    private static void testRouteAllMultiple() {
        startTest("testRouteAllMultiple");
        AlertRouter router = new AlertRouter();
        DynamicArray<Alert> batch = new DynamicArray<>();
        batch.add(new Alert("B1", 2, 1, "H", "M", 1L));
        batch.add(new Alert("B2", 4, 1, "H", "M", 2L));
        batch.add(new Alert("B3", 3, 1, "H", "M", 3L));

        router.routeAll(batch);
        assertEquals(3, router.getPendingAlertCount(), "All 3 batch items routed");
        assertEquals("B2", router.peekNextAlert().getAlertId(), "Peek is B2");
        assertEquals("B2", router.getNextAlert().getAlertId(), "Extract is B2");
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
