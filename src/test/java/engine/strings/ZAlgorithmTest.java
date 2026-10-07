package engine.strings;

/**
 * Standalone unit test suite for {@link ZAlgorithm}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test runners.
 */
public class ZAlgorithmTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running ZAlgorithmTest...");

        runTest("testZArrayConstruction", ZAlgorithmTest::testZArrayConstruction);
        runTest("testBasicMatch", ZAlgorithmTest::testBasicMatch);
        runTest("testNoMatch", ZAlgorithmTest::testNoMatch);
        runTest("testMultipleMatches", ZAlgorithmTest::testMultipleMatches);
        runTest("testOverlappingMatches", ZAlgorithmTest::testOverlappingMatches);
        runTest("testPatternAtBeginning", ZAlgorithmTest::testPatternAtBeginning);
        runTest("testPatternAtEnd", ZAlgorithmTest::testPatternAtEnd);
        runTest("testPatternEqualsText", ZAlgorithmTest::testPatternEqualsText);
        runTest("testPatternLongerThanText", ZAlgorithmTest::testPatternLongerThanText);
        runTest("testEmptyOrNullInputs", ZAlgorithmTest::testEmptyOrNullInputs);
        runTest("testSingleCharacter", ZAlgorithmTest::testSingleCharacter);
        runTest("testRepeatedCharacters", ZAlgorithmTest::testRepeatedCharacters);
        runTest("testSeparatorCollisionSafety", ZAlgorithmTest::testSeparatorCollisionSafety);
        runTest("testSpecialCharactersAndWhitespace", ZAlgorithmTest::testSpecialCharactersAndWhitespace);

        System.out.println("ZAlgorithmTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void testZArrayConstruction() {
        // String: "aabxaabxcaabxaabxay"
        // For "aabxaabxcaabxaabxay":
        // Z[0]=0, Z[1]=1, Z[2]=0, Z[3]=0, Z[4]=4, ...
        int[] z1 = ZAlgorithm.buildZ("aaaaa");
        int[] expected1 = new int[]{0, 4, 3, 2, 1};
        assertArrayEquals(expected1, z1, "Z-array for aaaaa");

        int[] z2 = ZAlgorithm.buildZ("abacaba");
        int[] expected2 = new int[]{0, 0, 1, 0, 3, 0, 1};
        assertArrayEquals(expected2, z2, "Z-array for abacaba");

        int[] zEmpty = ZAlgorithm.buildZ("");
        assertArrayEquals(new int[0], zEmpty, "Z-array for empty string");
    }

    private static void testBasicMatch() {
        String text = "the quick brown fox jumps over the lazy dog";
        String pattern = "brown fox";
        int[] matches = ZAlgorithm.search(text, pattern);
        assertArrayEquals(new int[]{10}, matches, "Basic match at offset 10");
    }

    private static void testNoMatch() {
        String text = "powershell execution blocked";
        String pattern = "mimikatz";
        int[] matches = ZAlgorithm.search(text, pattern);
        assertArrayEquals(new int[0], matches, "No match should return empty array");
    }

    private static void testMultipleMatches() {
        String text = "cat and dog and cat and bird and cat";
        String pattern = "cat";
        int[] matches = ZAlgorithm.search(text, pattern);
        assertArrayEquals(new int[]{0, 16, 33}, matches, "Multiple matches at offsets 0, 16, 33");
    }

    private static void testOverlappingMatches() {
        String text = "ABABDABABCABAB";
        String pattern = "ABAB";
        int[] matches = ZAlgorithm.search(text, pattern);
        assertArrayEquals(new int[]{0, 5, 10}, matches, "Matches at 0, 5, 10");

        // Overlapping match: "ABAB" in "ABABAB" -> 0, 2
        int[] overlapMatches = ZAlgorithm.search("ABABAB", "ABAB");
        assertArrayEquals(new int[]{0, 2}, overlapMatches, "Overlapping matches at 0, 2");

        int[] matches2 = ZAlgorithm.search("AAAAA", "AAA");
        assertArrayEquals(new int[]{0, 1, 2}, matches2, "Overlapping identical characters at 0, 1, 2");
    }

    private static void testPatternAtBeginning() {
        String text = "powershell -enc SGVsbG8=";
        String pattern = "powershell";
        int[] matches = ZAlgorithm.search(text, pattern);
        assertArrayEquals(new int[]{0}, matches, "Pattern at beginning");
    }

    private static void testPatternAtEnd() {
        String text = "execution halted with code 0";
        String pattern = "code 0";
        int[] matches = ZAlgorithm.search(text, pattern);
        assertArrayEquals(new int[]{22}, matches, "Pattern at end");
    }

    private static void testPatternEqualsText() {
        String text = "certutil -urlcache";
        String pattern = "certutil -urlcache";
        int[] matches = ZAlgorithm.search(text, pattern);
        assertArrayEquals(new int[]{0}, matches, "Pattern exactly equals text");
    }

    private static void testPatternLongerThanText() {
        String text = "short";
        String pattern = "longer than text";
        int[] matches = ZAlgorithm.search(text, pattern);
        assertArrayEquals(new int[0], matches, "Pattern longer than text returns empty");
    }

    private static void testEmptyOrNullInputs() {
        assertArrayEquals(new int[0], ZAlgorithm.search(null, "pattern"), "Null text");
        assertArrayEquals(new int[0], ZAlgorithm.search("text", null), "Null pattern");
        assertArrayEquals(new int[0], ZAlgorithm.search("", "pattern"), "Empty text");
        assertArrayEquals(new int[0], ZAlgorithm.search("text", ""), "Empty pattern");
        assertArrayEquals(new int[0], ZAlgorithm.search("", ""), "Empty text and pattern");
    }

    private static void testSingleCharacter() {
        String text = "abracadabra";
        String pattern = "a";
        int[] matches = ZAlgorithm.search(text, pattern);
        assertArrayEquals(new int[]{0, 3, 5, 7, 10}, matches, "Single character occurrences of 'a'");

        int[] singleCharMatch = ZAlgorithm.search("x", "x");
        assertArrayEquals(new int[]{0}, singleCharMatch, "Single character exact match");

        int[] singleCharMismatch = ZAlgorithm.search("x", "y");
        assertArrayEquals(new int[0], singleCharMismatch, "Single character mismatch");
    }

    private static void testRepeatedCharacters() {
        String text = "baaaaaaaaaab";
        String pattern = "aaaa";
        // Offsets: 1, 2, 3, 4, 5, 6, 7
        int[] matches = ZAlgorithm.search(text, pattern);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7}, matches, "Long repeated character sequences");
    }

    private static void testSeparatorCollisionSafety() {
        // Many naive Z-algorithm implementations concatenate pattern + "#" + text.
        // If text or pattern itself contains '#', '$', '\0', or '|', naive implementations fail.
        String text = "abc#def#pattern#with#separators#and#pattern#end";
        String pattern = "pattern#with#separators";
        int[] matches = ZAlgorithm.search(text, pattern);
        assertArrayEquals(new int[]{8}, matches, "Separator '#' in text and pattern handled safely without collision");

        String text2 = "payload|with|pipes|and|symbols$#@!payload|with|pipes";
        String pattern2 = "payload|with|pipes";
        int[] matches2 = ZAlgorithm.search(text2, pattern2);
        assertArrayEquals(new int[]{0, 34}, matches2, "Special delimiters in payload handled safely");
    }

    private static void testSpecialCharactersAndWhitespace() {
        String text = "cmd.exe /c \"whoami /priv\" && echo $PATH | grep -i admin";
        String pattern = "/c \"whoami /priv\" && echo $PATH |";
        int[] matches = ZAlgorithm.search(text, pattern);
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
