package engine.strings;

/**
 * Standalone unit test suite for {@link KMP}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test runners.
 */
public class KMPTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running KMPTest...");

        runTest("testLPSConstruction", KMPTest::testLPSConstruction);
        runTest("testBasicMatch", KMPTest::testBasicMatch);
        runTest("testNoMatch", KMPTest::testNoMatch);
        runTest("testMultipleMatches", KMPTest::testMultipleMatches);
        runTest("testOverlappingMatches", KMPTest::testOverlappingMatches);
        runTest("testPatternAtBeginning", KMPTest::testPatternAtBeginning);
        runTest("testPatternAtEnd", KMPTest::testPatternAtEnd);
        runTest("testPatternEqualsText", KMPTest::testPatternEqualsText);
        runTest("testPatternLongerThanText", KMPTest::testPatternLongerThanText);
        runTest("testEmptyOrNullInputs", KMPTest::testEmptyOrNullInputs);
        runTest("testSingleCharacter", KMPTest::testSingleCharacter);
        runTest("testSpecialCharactersAndWhitespace", KMPTest::testSpecialCharactersAndWhitespace);

        System.out.println("KMPTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void testLPSConstruction() {
        // Pattern: ABABCABAB -> LPS: 0 0 1 2 0 1 2 3 4
        int[] lps1 = KMP.buildLPS("ABABCABAB");
        int[] expected1 = new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4};
        assertArrayEquals(expected1, lps1, "LPS for ABABCABAB");

        // Pattern: AAAA -> LPS: 0 1 2 3
        int[] lps2 = KMP.buildLPS("AAAA");
        int[] expected2 = new int[]{0, 1, 2, 3};
        assertArrayEquals(expected2, lps2, "LPS for AAAA");

        // Empty pattern
        int[] lpsEmpty = KMP.buildLPS("");
        assertArrayEquals(new int[0], lpsEmpty, "LPS for empty pattern");
    }

    private static void testBasicMatch() {
        String text = "the quick brown fox jumps over the lazy dog";
        String pattern = "brown fox";
        int[] matches = KMP.search(text, pattern);
        assertArrayEquals(new int[]{10}, matches, "Basic match at offset 10");
    }

    private static void testNoMatch() {
        String text = "powershell execution blocked";
        String pattern = "mimikatz";
        int[] matches = KMP.search(text, pattern);
        assertArrayEquals(new int[0], matches, "No match should return empty array");
    }

    private static void testMultipleMatches() {
        String text = "cat and dog and cat and bird and cat";
        String pattern = "cat";
        int[] matches = KMP.search(text, pattern);
        assertArrayEquals(new int[]{0, 16, 33}, matches, "Multiple matches at offsets 0, 16, 33");
    }

    private static void testOverlappingMatches() {
        // Multiple matches: "ABAB" in "ABABDABABCABAB" at 0, 5, 10
        String text = "ABABDABABCABAB";
        String pattern = "ABAB";
        int[] matches = KMP.search(text, pattern);
        assertArrayEquals(new int[]{0, 5, 10}, matches, "Matches at 0, 5, 10");

        // Overlapping match: "ABAB" in "ABABAB" -> 0, 2
        int[] overlapMatches = KMP.search("ABABAB", "ABAB");
        assertArrayEquals(new int[]{0, 2}, overlapMatches, "Overlapping matches at 0, 2");

        // Overlapping identical letters: "AAA" in "AAAAA" -> 0, 1, 2
        int[] matches2 = KMP.search("AAAAA", "AAA");
        assertArrayEquals(new int[]{0, 1, 2}, matches2, "Repeated overlapping matches at 0, 1, 2");
    }

    private static void testPatternAtBeginning() {
        String text = "powershell -enc SGVsbG8=";
        String pattern = "powershell";
        int[] matches = KMP.search(text, pattern);
        assertArrayEquals(new int[]{0}, matches, "Pattern at beginning");
    }

    private static void testPatternAtEnd() {
        String text = "execution halted with code 0";
        String pattern = "code 0";
        int[] matches = KMP.search(text, pattern);
        assertArrayEquals(new int[]{22}, matches, "Pattern at end");
    }

    private static void testPatternEqualsText() {
        String text = "certutil -urlcache";
        String pattern = "certutil -urlcache";
        int[] matches = KMP.search(text, pattern);
        assertArrayEquals(new int[]{0}, matches, "Pattern exactly equals text");
    }

    private static void testPatternLongerThanText() {
        String text = "short";
        String pattern = "longer than text";
        int[] matches = KMP.search(text, pattern);
        assertArrayEquals(new int[0], matches, "Pattern longer than text returns empty");
    }

    private static void testEmptyOrNullInputs() {
        assertArrayEquals(new int[0], KMP.search(null, "pattern"), "Null text");
        assertArrayEquals(new int[0], KMP.search("text", null), "Null pattern");
        assertArrayEquals(new int[0], KMP.search("", "pattern"), "Empty text");
        assertArrayEquals(new int[0], KMP.search("text", ""), "Empty pattern");
        assertArrayEquals(new int[0], KMP.search("", ""), "Empty text and empty pattern");
    }

    private static void testSingleCharacter() {
        String text = "abracadabra";
        String pattern = "a";
        int[] matches = KMP.search(text, pattern);
        assertArrayEquals(new int[]{0, 3, 5, 7, 10}, matches, "Single character occurrences of 'a'");

        int[] singleCharMatch = KMP.search("x", "x");
        assertArrayEquals(new int[]{0}, singleCharMatch, "Single character exact match");

        int[] singleCharMismatch = KMP.search("x", "y");
        assertArrayEquals(new int[0], singleCharMismatch, "Single character mismatch");
    }

    private static void testSpecialCharactersAndWhitespace() {
        String text = "cmd.exe /c \"whoami /priv\" && echo $PATH | grep -i admin";
        String pattern = "/c \"whoami /priv\" && echo $PATH |";
        int[] matches = KMP.search(text, pattern);
        assertArrayEquals(new int[]{8}, matches, "Special characters and pipes in pattern");
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

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
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
