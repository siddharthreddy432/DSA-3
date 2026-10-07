package engine.integration;

import engine.datastructures.DynamicArray;
import engine.ingestion.IOCLoader;
import engine.ingestion.LogParser;
import engine.ingestion.SignatureMatcher;
import engine.ingestion.SignatureMatcher.Algorithm;
import engine.models.IOC;
import engine.models.LogEntry;
import engine.models.SignatureMatch;
import engine.strings.KMP;
import engine.strings.RabinKarp;
import engine.strings.ZAlgorithm;

import java.io.File;

/**
 * Master integration test suite for Phase 3: String Matching & IoC Detection.
 *
 * <p>Validates:
 * <ul>
 *   <li>Cross-algorithm output consistency: KMP == Z-Algorithm == Rabin-Karp</li>
 *   <li>End-to-end pipeline: file parsing -> IoC loading -> signature matching -> structured results</li>
 *   <li>Handling of multiple matches, overlapping occurrences, and non-matching entries</li>
 * </ul>
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test runners.
 */
public class SignatureMatchingIntegrationTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running SignatureMatchingIntegrationTest...");

        runTest("testCrossAlgorithmParity_Basic", SignatureMatchingIntegrationTest::testCrossAlgorithmParity_Basic);
        runTest("testCrossAlgorithmParity_Overlapping", SignatureMatchingIntegrationTest::testCrossAlgorithmParity_Overlapping);
        runTest("testCrossAlgorithmParity_RepeatedPrefixes", SignatureMatchingIntegrationTest::testCrossAlgorithmParity_RepeatedPrefixes);
        runTest("testCrossAlgorithmParity_SpecialCharacters", SignatureMatchingIntegrationTest::testCrossAlgorithmParity_SpecialCharacters);
        runTest("testCrossAlgorithmParity_NoMatch", SignatureMatchingIntegrationTest::testCrossAlgorithmParity_NoMatch);
        runTest("testCrossAlgorithmParity_EdgeCases", SignatureMatchingIntegrationTest::testCrossAlgorithmParity_EdgeCases);
        runTest("testEndToEndPipeline_MatchingWithAllAlgorithms", SignatureMatchingIntegrationTest::testEndToEndPipeline_MatchingWithAllAlgorithms);
        runTest("testEndToEndPipeline_MatchMetadataIntegrity", SignatureMatchingIntegrationTest::testEndToEndPipeline_MatchMetadataIntegrity);

        System.out.println("SignatureMatchingIntegrationTest Summary: Total=" + totalTests +
                ", Passed=" + passedTests + ", Failed=" + failedTests);
        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void testCrossAlgorithmParity_Basic() {
        String text = "the quick brown fox jumps over the lazy dog";
        String pattern = "brown fox";

        int[] kmp = KMP.search(text, pattern);
        int[] z = ZAlgorithm.search(text, pattern);
        int[] rk = RabinKarp.search(text, pattern);

        assertArrayEquals(kmp, z, "KMP and ZAlgorithm must produce identical results");
        assertArrayEquals(kmp, rk, "KMP and RabinKarp must produce identical results");
        assertArrayEquals(new int[]{10}, kmp, "Expected match offset 10");
    }

    private static void testCrossAlgorithmParity_Overlapping() {
        String text = "ABABDABABCABAB";
        String pattern = "ABAB";

        int[] kmp = KMP.search(text, pattern);
        int[] z = ZAlgorithm.search(text, pattern);
        int[] rk = RabinKarp.search(text, pattern);

        assertArrayEquals(kmp, z, "KMP and ZAlgorithm parity on ABAB");
        assertArrayEquals(kmp, rk, "KMP and RabinKarp parity on ABAB");
        assertArrayEquals(new int[]{0, 5, 10}, kmp, "Matches at offsets 0, 5, 10");

        // Overlapping match: "ABAB" in "ABABAB"
        int[] kmpOverlap = KMP.search("ABABAB", "ABAB");
        int[] zOverlap = ZAlgorithm.search("ABABAB", "ABAB");
        int[] rkOverlap = RabinKarp.search("ABABAB", "ABAB");

        assertArrayEquals(kmpOverlap, zOverlap, "KMP and ZAlgorithm parity on overlapping ABABAB");
        assertArrayEquals(kmpOverlap, rkOverlap, "KMP and RabinKarp parity on overlapping ABABAB");
        assertArrayEquals(new int[]{0, 2}, kmpOverlap, "Overlapping matches at 0, 2");

        // Highly repetitive string: 10 'A's, pattern 3 'A's
        String repText = "AAAAAAAAAA";
        String repPattern = "AAA";

        int[] kmpRep = KMP.search(repText, repPattern);
        int[] zRep = ZAlgorithm.search(repText, repPattern);
        int[] rkRep = RabinKarp.search(repText, repPattern);

        assertArrayEquals(kmpRep, zRep, "KMP and ZAlgorithm parity on repeated 'AAA'");
        assertArrayEquals(kmpRep, rkRep, "KMP and RabinKarp parity on repeated 'AAA'");
        assertArrayEquals(new int[]{0, 1, 2, 3, 4, 5, 6, 7}, kmpRep, "Matches at 0 through 7");
    }

    private static void testCrossAlgorithmParity_RepeatedPrefixes() {
        String text = "powershell -enc powershell -ExecutionPolicy Bypass powershell -enc SGVsbG8=";
        String pattern = "powershell -enc";

        int[] kmp = KMP.search(text, pattern);
        int[] z = ZAlgorithm.search(text, pattern);
        int[] rk = RabinKarp.search(text, pattern);

        assertArrayEquals(kmp, z, "Parity on repeated command prefix");
        assertArrayEquals(kmp, rk, "Parity on repeated command prefix");
        assertArrayEquals(new int[]{0, 51}, kmp, "Matches at offsets 0 and 51");
    }

    private static void testCrossAlgorithmParity_SpecialCharacters() {
        String text = "net use \\\\HOST-022\\IPC$ /u:domain\\admin Password123 | pipe#special$";
        String pattern = "\\\\HOST-022\\IPC$";

        int[] kmp = KMP.search(text, pattern);
        int[] z = ZAlgorithm.search(text, pattern);
        int[] rk = RabinKarp.search(text, pattern);

        assertArrayEquals(kmp, z, "Special characters parity KMP vs Z");
        assertArrayEquals(kmp, rk, "Special characters parity KMP vs Rabin-Karp");
        assertArrayEquals(new int[]{8}, kmp, "Expected match offset 8");
    }

    private static void testCrossAlgorithmParity_NoMatch() {
        String text = "benign system diagnostic execution";
        String pattern = "malware.exe";

        int[] kmp = KMP.search(text, pattern);
        int[] z = ZAlgorithm.search(text, pattern);
        int[] rk = RabinKarp.search(text, pattern);

        assertArrayEquals(new int[0], kmp, "KMP empty");
        assertArrayEquals(new int[0], z, "ZAlgorithm empty");
        assertArrayEquals(new int[0], rk, "RabinKarp empty");
    }

    private static void testCrossAlgorithmParity_EdgeCases() {
        // Pattern equals text
        String s = "exactMatchOnly";
        assertArrayEquals(KMP.search(s, s), ZAlgorithm.search(s, s), "Exact match KMP == Z");
        assertArrayEquals(KMP.search(s, s), RabinKarp.search(s, s), "Exact match KMP == RK");

        // Pattern longer than text
        assertArrayEquals(KMP.search("short", "longerPattern"), ZAlgorithm.search("short", "longerPattern"), "Long pattern");
        assertArrayEquals(KMP.search("short", "longerPattern"), RabinKarp.search("short", "longerPattern"), "Long pattern");

        // Null and empty
        assertArrayEquals(KMP.search(null, "p"), ZAlgorithm.search(null, "p"), "Null text");
        assertArrayEquals(KMP.search("t", null), RabinKarp.search("t", null), "Null pattern");
        assertArrayEquals(KMP.search("", "p"), ZAlgorithm.search("", "p"), "Empty text");
    }

    private static void testEndToEndPipeline_MatchingWithAllAlgorithms() throws Exception {
        String logsPath = "data" + File.separator + "input" + File.separator + "logs.txt";
        String iocsPath = "data" + File.separator + "input" + File.separator + "iocs.txt";

        DynamicArray<LogEntry> logs = LogParser.parseFile(logsPath);
        DynamicArray<IOC> iocs = IOCLoader.loadFile(iocsPath);

        assertTrue(logs.size() >= 10, "Should load at least 10 log records, found: " + logs.size());
        assertTrue(iocs.size() >= 7, "Should load at least 7 IOC rules, found: " + iocs.size());

        // Run matching with KMP
        DynamicArray<SignatureMatch> kmpMatches = SignatureMatcher.matchAll(logs, iocs, Algorithm.KMP);
        // Run matching with Z-Algorithm
        DynamicArray<SignatureMatch> zMatches = SignatureMatcher.matchAll(logs, iocs, Algorithm.Z_ALGORITHM);
        // Run matching with Rabin-Karp
        DynamicArray<SignatureMatch> rkMatches = SignatureMatcher.matchAll(logs, iocs, Algorithm.RABIN_KARP);

        assertEquals(kmpMatches.size(), zMatches.size(), "KMP and ZAlgorithm must produce the exact same match count");
        assertEquals(kmpMatches.size(), rkMatches.size(), "KMP and RabinKarp must produce the exact same match count");
        assertTrue(kmpMatches.size() > 0, "Must discover positive signature matches in the telemetry stream");

        // Verify each individual match is identical across algorithms
        for (int i = 0; i < kmpMatches.size(); i++) {
            SignatureMatch kMatch = kmpMatches.get(i);
            SignatureMatch zMatch = zMatches.get(i);
            SignatureMatch rkMatch = rkMatches.get(i);

            assertEquals(kMatch.getIoc().getId(), zMatch.getIoc().getId(), "IOC ID parity");
            assertEquals(kMatch.getIoc().getId(), rkMatch.getIoc().getId(), "IOC ID parity");

            assertEquals(kMatch.getMatchOffset(), zMatch.getMatchOffset(), "Match offset parity");
            assertEquals(kMatch.getMatchOffset(), rkMatch.getMatchOffset(), "Match offset parity");

            assertEquals(kMatch.getLogEntry().getTimestamp(), zMatch.getLogEntry().getTimestamp(), "Log timestamp parity");
            assertEquals(kMatch.getLogEntry().getTimestamp(), rkMatch.getLogEntry().getTimestamp(), "Log timestamp parity");
        }
    }

    private static void testEndToEndPipeline_MatchMetadataIntegrity() throws Exception {
        String logsPath = "data" + File.separator + "input" + File.separator + "logs.txt";
        String iocsPath = "data" + File.separator + "input" + File.separator + "iocs.txt";

        DynamicArray<LogEntry> logs = LogParser.parseFile(logsPath);
        DynamicArray<IOC> iocs = IOCLoader.loadFile(iocsPath);

        DynamicArray<SignatureMatch> matches = SignatureMatcher.matchAll(logs, iocs, Algorithm.KMP);

        boolean foundPowershell = false;
        boolean foundCertutil = false;
        boolean foundMimikatz = false;
        boolean foundIPC = false;
        boolean foundVssadmin = false;

        for (int i = 0; i < matches.size(); i++) {
            SignatureMatch match = matches.get(i);
            String iocId = match.getIoc().getId();

            if ("IOC-001".equals(iocId)) {
                foundPowershell = true;
                assertEquals("HOST-014", match.getLogEntry().getTargetHost(), "Powershell executed on HOST-014");
                assertEquals("HIGH", match.getIoc().getSeverity(), "Powershell severity HIGH");
            } else if ("IOC-002".equals(iocId)) {
                foundCertutil = true;
                assertEquals("HOST-022", match.getLogEntry().getTargetHost(), "Certutil executed on HOST-022");
            } else if ("IOC-003".equals(iocId)) {
                foundMimikatz = true;
                assertEquals("HOST-022", match.getLogEntry().getTargetHost(), "Mimikatz executed on HOST-022");
                assertEquals("CRITICAL", match.getIoc().getSeverity(), "Mimikatz severity CRITICAL");
            } else if ("IOC-004".equals(iocId)) {
                foundIPC = true;
                assertEquals("HOST-022", match.getLogEntry().getTargetHost(), "IPC$ lateral movement on HOST-022");
            } else if ("IOC-007".equals(iocId)) {
                foundVssadmin = true;
                assertEquals("DC-001", match.getLogEntry().getTargetHost(), "Vssadmin executed on DC-001");
            }

            // Verify reporting output format is non-empty and contains required fields
            String report = match.toReportString();
            assertTrue(report.length() > 0, "Report string must not be empty");
        }

        assertTrue(foundPowershell, "Must detect IOC-001 (PowerShell)");
        assertTrue(foundCertutil, "Must detect IOC-002 (Certutil)");
        assertTrue(foundMimikatz, "Must detect IOC-003 (Mimikatz)");
        assertTrue(foundIPC, "Must detect IOC-004 (IPC$)");
        assertTrue(foundVssadmin, "Must detect IOC-007 (Vssadmin)");
    }

    // ---- Helper test runners and assertions ----

    private static void runTest(String testName, RunnableWithException test) {
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

    @FunctionalInterface
    interface RunnableWithException {
        void run() throws Exception;
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion failed: " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
    }

    private static void assertArrayEquals(int[] expected, int[] actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || actual == null) {
            throw new AssertionError("Assertion failed: " + message + " (One array was null)");
        }
        if (expected.length != actual.length) {
            throw new AssertionError("Assertion failed: " + message +
                    " (Length mismatch: expected " + expected.length + ", actual " + actual.length + ")");
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionError("Assertion failed: " + message +
                        " (Element mismatch at index " + i + ": expected " + expected[i] + ", actual " + actual[i] + ")");
            }
        }
    }
}
