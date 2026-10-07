package engine.ingestion;

import engine.datastructures.DynamicArray;
import engine.models.LogEntry;

/**
 * Standalone unit test suite for {@link LogParser}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test runners.
 */
public class LogParserTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running LogParserTest...");

        runTest("testValidLogParsing", LogParserTest::testValidLogParsing);
        runTest("testMalformedLogMissingFields", LogParserTest::testMalformedLogMissingFields);
        runTest("testMalformedLogExtraFields", LogParserTest::testMalformedLogExtraFields);
        runTest("testEmptyOrNullLineHandling", LogParserTest::testEmptyOrNullLineHandling);
        runTest("testSpecialCharactersInPayload", LogParserTest::testSpecialCharactersInPayload);
        runTest("testBatchParsingSkipsMalformedRecords", LogParserTest::testBatchParsingSkipsMalformedRecords);

        System.out.println("LogParserTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void testValidLogParsing() {
        String line = "2026-09-22T10:43:02|10.0.14.23|HOST-014|EXEC|powershell -enc SGVsbG8=";
        LogEntry entry = LogParser.parseLine(line);

        assertEquals("2026-09-22T10:43:02", entry.getTimestamp(), "Timestamp parsed correctly");
        assertEquals("10.0.14.23", entry.getSourceIp(), "Source IP parsed correctly");
        assertEquals("HOST-014", entry.getTargetHost(), "Target host parsed correctly");
        assertEquals("EXEC", entry.getEventType(), "Event type parsed correctly");
        assertEquals("powershell -enc SGVsbG8=", entry.getPayload(), "Payload parsed correctly");
    }

    private static void testMalformedLogMissingFields() {
        // Only 4 fields instead of 5
        String line = "2026-09-22T10:43:02|10.0.14.23|HOST-014|EXEC";
        assertThrows(IllegalArgumentException.class, () -> LogParser.parseLine(line),
                "Missing field should trigger IllegalArgumentException");
    }

    private static void testMalformedLogExtraFields() {
        // 6 fields instead of 5
        String line = "2026-09-22T10:43:02|10.0.14.23|HOST-014|EXEC|payload|EXTRA_FIELD";
        assertThrows(IllegalArgumentException.class, () -> LogParser.parseLine(line),
                "Extra field should trigger IllegalArgumentException");
    }

    private static void testEmptyOrNullLineHandling() {
        assertThrows(IllegalArgumentException.class, () -> LogParser.parseLine(null), "Null line throws");
        assertThrows(IllegalArgumentException.class, () -> LogParser.parseLine(""), "Empty line throws");
        assertThrows(IllegalArgumentException.class, () -> LogParser.parseLine("   "), "Whitespace line throws");
    }

    private static void testSpecialCharactersInPayload() {
        String line = "2026-09-22T10:45:17|10.0.14.23|HOST-022|LATERAL|net use \\\\HOST-022\\IPC$ /u:domain\\admin Password123";
        LogEntry entry = LogParser.parseLine(line);

        assertEquals("HOST-022", entry.getTargetHost(), "Target host matches");
        assertEquals("LATERAL", entry.getEventType(), "Event type matches");
        assertEquals("net use \\\\HOST-022\\IPC$ /u:domain\\admin Password123", entry.getPayload(), "Payload matches special chars");
    }

    private static void testBatchParsingSkipsMalformedRecords() {
        String multiLine =
                "# Telemetry stream comment\n" +
                "2026-09-22T10:40:01|10.0.14.23|HOST-014|AUTH|user admin logged in\n" +
                "\n" +
                "MALFORMED_LINE_WITHOUT_PIPES\n" +
                "2026-09-22T10:43:02|10.0.14.23|HOST-014|EXEC|powershell -enc SGVsbG8=\n" +
                "INCOMPLETE|10.0.14.23|HOST-014\n";

        DynamicArray<LogEntry> entries = LogParser.parseContent(multiLine);
        assertEquals(2, entries.size(), "Batch parsing should parse exactly 2 valid records and skip comments/malformed lines");
        assertEquals("AUTH", entries.get(0).getEventType(), "First record is AUTH");
        assertEquals("EXEC", entries.get(1).getEventType(), "Second record is EXEC");
    }

    // ---- Helper test runners and assertions ----

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
                throw new AssertionError("Assertion failed: Expected " + expected.getName() + " but caught " + t.getClass().getName() + " for " + message, t);
            }
        }
    }
}
