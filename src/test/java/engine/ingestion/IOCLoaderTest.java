package engine.ingestion;

import engine.datastructures.DynamicArray;
import engine.models.IOC;

/**
 * Standalone unit test suite for {@link IOCLoader}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test runners.
 */
public class IOCLoaderTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running IOCLoaderTest...");

        runTest("testValidIOCParsing", IOCLoaderTest::testValidIOCParsing);
        runTest("testMalformedIOCMissingFields", IOCLoaderTest::testMalformedIOCMissingFields);
        runTest("testMalformedIOCExtraFields", IOCLoaderTest::testMalformedIOCExtraFields);
        runTest("testEmptyOrNullLineHandling", IOCLoaderTest::testEmptyOrNullLineHandling);
        runTest("testWhitespaceAndSpecialChars", IOCLoaderTest::testWhitespaceAndSpecialChars);
        runTest("testBatchLoadingSkipsMalformedRecords", IOCLoaderTest::testBatchLoadingSkipsMalformedRecords);

        System.out.println("IOCLoaderTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void testValidIOCParsing() {
        String line = "IOC-001|COMMAND|powershell -enc|Suspicious encoded PowerShell|HIGH";
        IOC ioc = IOCLoader.parseLine(line);

        assertEquals("IOC-001", ioc.getId(), "IOC ID parsed correctly");
        assertEquals("COMMAND", ioc.getType(), "IOC Type parsed correctly");
        assertEquals("powershell -enc", ioc.getValue(), "IOC Value parsed correctly");
        assertEquals("Suspicious encoded PowerShell", ioc.getDescription(), "IOC Description parsed correctly");
        assertEquals("HIGH", ioc.getSeverity(), "IOC Severity parsed correctly");
    }

    private static void testMalformedIOCMissingFields() {
        // Only 3 fields
        String line = "IOC-001|COMMAND|powershell -enc";
        assertThrows(IllegalArgumentException.class, () -> IOCLoader.parseLine(line),
                "Missing fields in IOC line must throw IllegalArgumentException");
    }

    private static void testMalformedIOCExtraFields() {
        // 6 fields
        String line = "IOC-001|COMMAND|powershell -enc|description|HIGH|EXTRA";
        assertThrows(IllegalArgumentException.class, () -> IOCLoader.parseLine(line),
                "Extra fields in IOC line must throw IllegalArgumentException");
    }

    private static void testEmptyOrNullLineHandling() {
        assertThrows(IllegalArgumentException.class, () -> IOCLoader.parseLine(null), "Null line throws");
        assertThrows(IllegalArgumentException.class, () -> IOCLoader.parseLine(""), "Empty line throws");
        assertThrows(IllegalArgumentException.class, () -> IOCLoader.parseLine("   "), "Whitespace line throws");
    }

    private static void testWhitespaceAndSpecialChars() {
        String line = " IOC-004 | PROTOCOL | IPC$ | Remote SMB named pipe | MEDIUM ";
        IOC ioc = IOCLoader.parseLine(line);

        assertEquals("IOC-004", ioc.getId(), "Trimmed ID");
        assertEquals("PROTOCOL", ioc.getType(), "Trimmed type");
        assertEquals("IPC$", ioc.getValue(), "Pattern value");
        assertEquals("MEDIUM", ioc.getSeverity(), "Severity");
    }

    private static void testBatchLoadingSkipsMalformedRecords() {
        String content =
                "# IoC signature rules\n" +
                "IOC-001|COMMAND|powershell -enc|Suspicious encoded PowerShell|HIGH\n" +
                "\n" +
                "NOT_VALID_RECORD\n" +
                "IOC-003|TOOL|mimikatz|Credential dumping tool indicator|CRITICAL\n" +
                "IOC-999|INCOMPLETE\n";

        DynamicArray<IOC> iocs = IOCLoader.loadContent(content);
        assertEquals(2, iocs.size(), "Batch loading must parse 2 valid IOCs, skipping comments and malformed lines");
        assertEquals("IOC-001", iocs.get(0).getId(), "First IOC is IOC-001");
        assertEquals("IOC-003", iocs.get(1).getId(), "Second IOC is IOC-003");
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
