package engine.correlation;

import engine.datastructures.DynamicArray;
import engine.models.AttackNode;
import engine.models.IOC;
import engine.models.LogEntry;
import engine.models.SignatureMatch;

/**
 * Standalone test suite for {@link KillChain} and {@link KillChainStage}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test libraries.
 */
public class KillChainTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running KillChainTest...");

        runTest("testCanonicalStageOrdering", KillChainTest::testCanonicalStageOrdering);
        runTest("testStageComparisons", KillChainTest::testStageComparisons);
        runTest("testStageLookups", KillChainTest::testStageLookups);
        runTest("testCustomStageInsertionOrdering", KillChainTest::testCustomStageInsertionOrdering);
        runTest("testNodeAssociationWithStages", KillChainTest::testNodeAssociationWithStages);
        runTest("testDetermineStageFromEvidence", KillChainTest::testDetermineStageFromEvidence);

        System.out.println("KillChainTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
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

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion failed: " + message + " (expected: " + expected + ", got: " + actual + ")");
    }

    private static void testCanonicalStageOrdering() {
        KillChain killChain = KillChain.createDefault();
        DynamicArray<KillChainStage> stages = killChain.getStages();
        assertEquals(7, stages.size(), "Canonical stages count must be 7");

        assertEquals(1, stages.get(0).getOrder(), "Stage 1 order");
        assertEquals("RECONNAISSANCE", stages.get(0).getName(), "Stage 1 name");

        assertEquals(2, stages.get(1).getOrder(), "Stage 2 order");
        assertEquals("EXPLOITATION", stages.get(1).getName(), "Stage 2 name");

        assertEquals(3, stages.get(2).getOrder(), "Stage 3 order");
        assertEquals("PRIVILEGE_ESCALATION", stages.get(2).getName(), "Stage 3 name");

        assertEquals(4, stages.get(3).getOrder(), "Stage 4 order");
        assertEquals("LATERAL_MOVEMENT", stages.get(3).getName(), "Stage 4 name");

        assertEquals(5, stages.get(4).getOrder(), "Stage 5 order");
        assertEquals("DEFENSE_EVASION", stages.get(4).getName(), "Stage 5 name");

        assertEquals(6, stages.get(5).getOrder(), "Stage 6 order");
        assertEquals("COMMAND_AND_CONTROL", stages.get(5).getName(), "Stage 6 name");

        assertEquals(7, stages.get(6).getOrder(), "Stage 7 order");
        assertEquals("EXFILTRATION", stages.get(6).getName(), "Stage 7 name");
    }

    private static void testStageComparisons() {
        KillChainStage s1 = KillChainStage.RECONNAISSANCE;
        KillChainStage s4 = KillChainStage.LATERAL_MOVEMENT;
        KillChainStage s7 = KillChainStage.EXFILTRATION;

        assertTrue(s1.isBefore(s4), "Recon is before Lateral Movement");
        assertTrue(s7.isAfter(s4), "Exfiltration is after Lateral Movement");
        assertFalse(s4.isBefore(s1), "Lateral Movement is NOT before Recon");
        assertTrue(s1.compareTo(s4) < 0, "compareTo s1 and s4 should be negative");
        assertTrue(s7.compareTo(s4) > 0, "compareTo s7 and s4 should be positive");
    }

    private static void testStageLookups() {
        KillChain killChain = KillChain.createDefault();

        KillChainStage foundOrder4 = killChain.getStage(4);
        assertEquals("LATERAL_MOVEMENT", foundOrder4.getName(), "Stage 4 should be LATERAL_MOVEMENT");

        KillChainStage foundByName = killChain.getStageByName("defense_evasion");
        assertEquals(5, foundByName.getOrder(), "defense_evasion should have order 5");

        KillChainStage missing = killChain.getStage(99);
        assertEquals(null, missing, "Missing stage order should return null");
    }

    private static void testCustomStageInsertionOrdering() {
        KillChain customChain = new KillChain();
        customChain.clear();
        // Insert out of order: 3, 1, 2
        KillChainStage custom3 = new KillChainStage(3, "OBJECTIVES");
        KillChainStage custom1 = new KillChainStage(1, "INFILTRATION");
        KillChainStage custom2 = new KillChainStage(2, "PROPAGATION");

        customChain.addStage(custom3);
        customChain.addStage(custom1);
        customChain.addStage(custom2);

        DynamicArray<KillChainStage> stages = customChain.getStages();
        // The stages list will have the canonical 7 plus the inserted ones, or in sorted order
        for (int i = 0; i < stages.size() - 1; i++) {
            assertTrue(stages.get(i).getOrder() <= stages.get(i + 1).getOrder(),
                    "Stages must maintain ascending order");
        }
    }

    private static void testNodeAssociationWithStages() {
        KillChain killChain = KillChain.createDefault();
        AttackNode n1 = new AttackNode("AN-1", "10.0.1.1", "HOST-1", KillChainStage.RECONNAISSANCE, "10:00:00");
        AttackNode n2 = new AttackNode("AN-2", "10.0.1.1", "HOST-2", KillChainStage.LATERAL_MOVEMENT, "10:05:00");
        AttackNode n3 = new AttackNode("AN-3", "10.0.1.1", "HOST-2", KillChainStage.LATERAL_MOVEMENT, "10:06:00");

        killChain.addNode(n1);
        killChain.addNode(n2);
        killChain.addNode(n3);

        assertEquals(3, killChain.getTotalNodeCount(), "Total nodes should be 3");

        DynamicArray<AttackNode> reconNodes = killChain.getNodesForStage(KillChainStage.RECONNAISSANCE);
        assertEquals(1, reconNodes.size(), "Recon stage should have 1 node");
        assertEquals("AN-1", reconNodes.get(0).getId(), "Recon node ID");

        DynamicArray<AttackNode> lateralNodes = killChain.getNodesForStage(KillChainStage.LATERAL_MOVEMENT);
        assertEquals(2, lateralNodes.size(), "Lateral movement stage should have 2 nodes");
        assertEquals("AN-2", lateralNodes.get(0).getId(), "First lateral node ID");
        assertEquals("AN-3", lateralNodes.get(1).getId(), "Second lateral node ID");

        DynamicArray<AttackNode> emptyStageNodes = killChain.getNodesForStage(KillChainStage.EXFILTRATION);
        assertEquals(0, emptyStageNodes.size(), "Exfiltration stage should have 0 nodes");
    }

    private static void testDetermineStageFromEvidence() {
        KillChain killChain = KillChain.createDefault();

        LogEntry authLog = new LogEntry("2026-09-22T10:40:01", "10.0.14.23", "HOST-014", "AUTH", "admin login");
        IOC authIoc = new IOC("IOC-100", "COMMAND", "login", "desc", "LOW");
        SignatureMatch authMatch = new SignatureMatch(authIoc, authLog, "KMP", 0, "login");
        assertEquals(KillChainStage.RECONNAISSANCE, killChain.determineStage(authMatch), "AUTH event maps to RECONNAISSANCE");

        LogEntry latLog = new LogEntry("2026-09-22T10:45:17", "10.0.14.23", "HOST-022", "LATERAL", "net use IPC$");
        IOC latIoc = new IOC("IOC-004", "PROTOCOL", "IPC$", "desc", "MEDIUM");
        SignatureMatch latMatch = new SignatureMatch(latIoc, latLog, "KMP", 8, "IPC$");
        assertEquals(KillChainStage.LATERAL_MOVEMENT, killChain.determineStage(latMatch), "LATERAL event maps to LATERAL_MOVEMENT");

        LogEntry privLog = new LogEntry("2026-09-22T10:49:05", "10.0.14.23", "HOST-022", "TOOL", "mimikatz");
        IOC privIoc = new IOC("IOC-003", "TOOL", "mimikatz", "desc", "CRITICAL");
        SignatureMatch privMatch = new SignatureMatch(privIoc, privLog, "KMP", 0, "mimikatz");
        assertEquals(KillChainStage.PRIVILEGE_ESCALATION, killChain.determineStage(privMatch), "mimikatz maps to PRIVILEGE_ESCALATION");

        LogEntry defLog = new LogEntry("2026-09-22T10:51:22", "10.0.14.23", "DC-001", "DEF_EVASION", "vssadmin");
        IOC defIoc = new IOC("IOC-007", "PATTERN", "vssadmin", "desc", "CRITICAL");
        SignatureMatch defMatch = new SignatureMatch(defIoc, defLog, "KMP", 0, "vssadmin");
        assertEquals(KillChainStage.DEFENSE_EVASION, killChain.determineStage(defMatch), "DEF_EVASION maps to DEFENSE_EVASION");

        LogEntry c2Log = new LogEntry("2026-09-22T10:53:40", "10.0.14.23", "FS-002", "C2", "cobaltstrike");
        IOC c2Ioc = new IOC("IOC-005", "FILE", "cobaltstrike", "desc", "CRITICAL");
        SignatureMatch c2Match = new SignatureMatch(c2Ioc, c2Log, "KMP", 0, "cobaltstrike");
        assertEquals(KillChainStage.COMMAND_AND_CONTROL, killChain.determineStage(c2Match), "C2 maps to COMMAND_AND_CONTROL");
    }
}
