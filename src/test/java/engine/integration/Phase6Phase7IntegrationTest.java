package engine.integration;

import engine.alerting.Alert;
import engine.alerting.AlertManager;
import engine.alerting.AlertRouter;
import engine.correlation.KillChainStage;
import engine.datastructures.DynamicArray;
import engine.graph.DirectedGraph;
import engine.graph.GraphNode;
import engine.models.AttackNode;
import engine.monitoring.MonitoringPlacement;
import engine.monitoring.MonitoringPoint;
import engine.monitoring.MonitoringResult;

/**
 * End-to-end integration test demonstrating the pipeline across Phase 4, Phase 6, and Phase 7:
 * <pre>
 * Attack Graph (Phase 4)
 *       ↓
 * Monitoring Placement (Phase 6)
 *       ↓
 * Sensor Alerts Generated
 *       ↓
 * Priority Scheduling via Custom Binary Heap (Phase 7)
 *       ↓
 * Ranked Security Alert Dispatch
 * </pre>
 */
public class Phase6Phase7IntegrationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("Running Phase6Phase7IntegrationTest...");

        testEndToEndMonitoringToAlertPriorityPipeline();
        testMultiBranchMonitoringAlertPrioritization();

        System.out.printf("Phase6Phase7IntegrationTest Summary: Total=%d, Passed=%d, Failed=%d%n",
                testsRun, testsPassed, testsFailed);

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void testEndToEndMonitoringToAlertPriorityPipeline() {
        startTest("testEndToEndMonitoringToAlertPriorityPipeline");

        // 1. Build Phase 4 Directed Attack Graph
        // Ingress (Recon) -> Pivot (Lateral) -> Database Server (Exfiltration)
        DirectedGraph<AttackNode> attackGraph = new DirectedGraph<>();

        AttackNode n1 = new AttackNode("INGRESS-01", "192.168.1.5", "WEB-SRV",
                KillChainStage.RECONNAISSANCE, "2026-03-30T10:00:00", null);
        AttackNode n2 = new AttackNode("PIVOT-01", "192.168.1.5", "APP-SRV",
                KillChainStage.LATERAL_MOVEMENT, "2026-03-30T10:15:00", null);
        AttackNode n3 = new AttackNode("EXFIL-01", "192.168.1.5", "DB-CORE",
                KillChainStage.EXFILTRATION, "2026-03-30T10:30:00", null);

        attackGraph.addNode(n1.getId(), n1);
        attackGraph.addNode(n2.getId(), n2);
        attackGraph.addNode(n3.getId(), n3);
        attackGraph.addEdge(n1.getId(), n2.getId(), "LATERAL_PIVOT");
        attackGraph.addEdge(n2.getId(), n3.getId(), "DATA_ACCESS");

        // 2. Execute Phase 6 Monitoring Placement (Greedy Set Cover)
        // Hub/pivot PIVOT-01 is incident to both edges and covers 100% of the attack path!
        MonitoringResult<AttackNode> monitoringResult = MonitoringPlacement.computeGreedySetCover(attackGraph);
        assertTrue(monitoringResult.getSelectedCount() > 0, "Sensors placed");
        assertEquals(2, monitoringResult.getTotalGraphEdges(), "Total edges is 2");
        assertEquals(2, monitoringResult.getCoveredEdgesCount(), "All edges covered");
        assertEquals(1.0, monitoringResult.getCoverageRatio(), "100% path coverage");

        // 3. Generate alerts at monitored sensor locations
        AlertManager alertManager = new AlertManager();
        DynamicArray<MonitoringPoint<AttackNode>> points = monitoringResult.getMonitoringPoints();

        for (int i = 0; i < points.size(); i++) {
            MonitoringPoint<AttackNode> mp = points.get(i);
            AttackNode node = mp.getNode();

            // Severity mapped to Kill Chain stage order (EXFIL=7, LATERAL=4, RECON=1)
            int severity = node.getStage().getOrder();
            int depth = mp.getNodeIndex() + 1;

            alertManager.createAndRegisterAlert(
                    "ALERT-" + node.getId(),
                    severity,
                    depth,
                    node.getTarget(),
                    "Suspicious activity detected at monitored checkpoint " + node.getId()
            );
        }

        // Add additional unmonitored baseline event to verify priority domination
        alertManager.createAndRegisterAlert("ALERT-LOW", 1, 1, "EXTERNAL-NET", "Low reconnaissance scan");

        // 4. Retrieve alerts in Phase 7 priority order
        assertTrue(alertManager.hasPendingAlerts(), "Alerts present in manager");
        Alert topAlert = alertManager.getNextAlert();

        // The top alert must be the highest severity/depth alert from the monitored checkpoints
        assertTrue(topAlert.getSeverity() > 1, "Top alert is high severity");
        assertTrue(alertManager.hasPendingAlerts(), "Lower priority alert remains");

        Alert lastAlert = alertManager.getNextAlert();
        assertEquals("ALERT-LOW", lastAlert.getAlertId(), "Low severity alert dispatched last");
        assertFalse(alertManager.hasPendingAlerts(), "All alerts routed");

        passTest();
    }

    private static void testMultiBranchMonitoringAlertPrioritization() {
        startTest("testMultiBranchMonitoringAlertPrioritization");

        // Multi-branch graph: Ingress branching to Branch A (deep, low severity) and Branch B (shallow, high severity)
        DirectedGraph<String> graph = new DirectedGraph<>();
        graph.addNode("ENTRY");
        graph.addNode("BRANCH_A1");
        graph.addNode("BRANCH_A2");
        graph.addNode("BRANCH_B1");

        graph.addEdge("ENTRY", "BRANCH_A1", "STEP_A1");
        graph.addEdge("BRANCH_A1", "BRANCH_A2", "STEP_A2");
        graph.addEdge("ENTRY", "BRANCH_B1", "STEP_B1");

        MonitoringResult<String> cover = MonitoringPlacement.computeVertexCover(graph);
        assertEquals(3, cover.getTotalGraphEdges(), "Total edges is 3");
        assertEquals(3, cover.getCoveredEdgesCount(), "All edges covered");

        AlertRouter router = new AlertRouter();
        // Route alerts for the branches
        router.routeAlert(new Alert("ALT-DEEP-MED", 3, 5, "BRANCH_A2", "Deep medium severity", 1L));
        router.routeAlert(new Alert("ALT-SHALLOW-CRIT", 5, 1, "BRANCH_B1", "Shallow critical exploit", 2L));
        router.routeAlert(new Alert("ALT-ENTRY-LOW", 1, 1, "ENTRY", "Entry probe", 3L));

        // Priority verification:
        // 1: ALT-SHALLOW-CRIT (sev 5 dominates)
        // 2: ALT-DEEP-MED (sev 3)
        // 3: ALT-ENTRY-LOW (sev 1)
        assertEquals("ALT-SHALLOW-CRIT", router.getNextAlert().getAlertId(), "Severity 5 wins");
        assertEquals("ALT-DEEP-MED", router.getNextAlert().getAlertId(), "Severity 3 is second");
        assertEquals("ALT-ENTRY-LOW", router.getNextAlert().getAlertId(), "Severity 1 is third");

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

    private static void assertEquals(double expected, double actual, String msg) {
        if (Math.abs(expected - actual) > 0.0001) {
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
