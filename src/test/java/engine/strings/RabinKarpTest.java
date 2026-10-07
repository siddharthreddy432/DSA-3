package engine.strings;

/**
 * Standalone unit test suite for {@link RabinKarp}.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party test runners.
 */
public class RabinKarpTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running RabinKarpTest...");

        runTest("testBasicMatch", RabinKarpTest::testBasicMatch);
        runTest("testNoMatch", RabinKarpTest::testNoMatch);
        runTest("testMultipleMatches", RabinKarpTest::testMultipleMatches);
        runTest("testOverlappingMatches", RabinKarpTest::testOverlappingMatches);
        runTest("testPatternAtBeginning", RabinKarpTest::testPatternAtBeginning);
        runTest("testPatternAtEnd", RabinKarpTest::testPatternAtEnd);
        runTest("testPatternEqualsText", RabinKarpTest::testPatternEqualsText);
        runTest("testPatternLongerThanText", RabinKarpTest::testPatternLongerThanText);
        runTest("testEmptyOrNullInputs", RabinKarpTest::testEmptyOrNullInputs);
        runTest("testSingleCharacter", RabinKarpTest::testSingleCharacter);
        runTest("testSpecialCharactersAndWhitespace", RabinKarpTest::testSpecialCharactersAndWhitespace);
        runTest("testRollingHashBehavior", RabinKarpTest::testRollingHashBehavior);
        runTest("testHashCollisionVerification", RabinKarpTest::testHashCollisionVerification);

        System.out.println("RabinKarpTest Summary: Total=" + totalTests + ", Passed=" + passedTests + ", Failed=" + failedTests);
        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void testBasicMatch() {
        String text = "the quick brown fox jumps over the lazy dog";
        String pattern = "brown fox";
        int[] matches = RabinKarp.search(text, pattern);
        assertArrayEquals(new int[]{10}, matches, "Basic match at offset 10");
    }

    private static void testNoMatch() {
        String text = "powershell execution blocked";
        String pattern = "mimikatz";
        int[] matches = RabinKarp.search(text, pattern);
        assertArrayEquals(new int[0], matches, "No match should return empty array");
    }

    private static void testMultipleMatches() {
        String text = "cat and dog and cat and bird and cat";
        String pattern = "cat";
        int[] matches = RabinKarp.search(text, pattern);
        assertArrayEquals(new int[]{0, 16, 33}, matches, "Multiple matches at offsets 0, 16, 33");
    }

    private static void testOverlappingMatches() {
        String text = "ABABDABABCABAB";
        String pattern = "ABAB";
        int[] matches = RabinKarp.search(text, pattern);
        assertArrayEquals(new int[]{0, 5, 10}, matches, "Matches at 0, 5, 10");

        // Overlapping match: "ABAB" in "ABABAB" -> 0, 2
        int[] overlapMatches = RabinKarp.search("ABABAB", "ABAB");
        assertArrayEquals(new int[]{0, 2}, overlapMatches, "Overlapping matches at 0, 2");

        int[] matches2 = RabinKarp.search("AAAAA", "AAA");
        assertArrayEquals(new int[]{0, 1, 2}, matches2, "Repeated identical characters at 0, 1, 2");
    }

    private static void testPatternAtBeginning() {
        String text = "powershell -enc SGVsbG8=";
        String pattern = "powershell";
        int[] matches = RabinKarp.search(text, pattern);
        assertArrayEquals(new int[]{0}, matches, "Pattern at beginning");
    }

    private static void testPatternAtEnd() {
        String text = "execution halted with code 0";
        String pattern = "code 0";
        int[] matches = RabinKarp.search(text, pattern);
        assertArrayEquals(new int[]{22}, matches, "Pattern at end");
    }

    private static void testPatternEqualsText() {
        String text = "certutil -urlcache";
        String pattern = "certutil -urlcache";
        int[] matches = RabinKarp.search(text, pattern);
        assertArrayEquals(new int[]{0}, matches, "Pattern exactly equals text");
    }

    private static void testPatternLongerThanText() {
        String text = "short";
        String pattern = "longer than text";
        int[] matches = RabinKarp.search(text, pattern);
        assertArrayEquals(new int[0], matches, "Pattern longer than text returns empty");
    }

    private static void testEmptyOrNullInputs() {
        assertArrayEquals(new int[0], RabinKarp.search(null, "pattern"), "Null text");
        assertArrayEquals(new int[0], RabinKarp.search("text", null), "Null pattern");
        assertArrayEquals(new int[0], RabinKarp.search("", "pattern"), "Empty text");
        assertArrayEquals(new int[0], RabinKarp.search("text", ""), "Empty pattern");
        assertArrayEquals(new int[0], RabinKarp.search("", ""), "Empty text and pattern");
    }

    private static void testSingleCharacter() {
        String text = "abracadabra";
        String pattern = "a";
        int[] matches = RabinKarp.search(text, pattern);
        assertArrayEquals(new int[]{0, 3, 5, 7, 10}, matches, "Single character occurrences of 'a'");

        int[] singleCharMatch = RabinKarp.search("x", "x");
        assertArrayEquals(new int[]{0}, singleCharMatch, "Single character exact match");

        int[] singleCharMismatch = RabinKarp.search("x", "y");
        assertArrayEquals(new int[0], singleCharMismatch, "Single character mismatch");
    }

    private static void testSpecialCharactersAndWhitespace() {
        String text = "cmd.exe /c \"whoami /priv\" && echo $PATH | grep -i admin";
        String pattern = "/c \"whoami /priv\" && echo $PATH |";
        int[] matches = RabinKarp.search(text, pattern);
        assertArrayEquals(new int[]{8}, matches, "Special characters and pipes in pattern");
    }

    private static void testRollingHashBehavior() {
        // Verify rolling hash consistency: hash(text, i+1, m) equals rolling computation from hash(text, i, m)
        String sample = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int m = 5;
        long h = 1L;
        for (int i = 0; i < m - 1; i++) {
            h = (h * RabinKarp.BASE) % RabinKarp.MODULUS;
        }

        long currentHash = RabinKarp.computeHash(sample, 0, m);
        for (int i = 0; i < sample.length() - m; i++) {
            long directNextHash = RabinKarp.computeHash(sample, i + 1, m);

            // Compute rolled hash using the sliding window formula
            long removed = (sample.charAt(i) * h) % RabinKarp.MODULUS;
            long intermediate = (currentHash - removed) % RabinKarp.MODULUS;
            if (intermediate < 0) intermediate += RabinKarp.MODULUS;
            long rolledNextHash = (intermediate * RabinKarp.BASE + sample.charAt(i + m)) % RabinKarp.MODULUS;

            assertEquals(directNextHash, rolledNextHash, "Rolling hash at window offset " + (i + 1));
            currentHash = rolledNextHash;
        }
    }

    private static void testHashCollisionVerification() {
        // Modulus: 1000000007. Base: 256.
        // Base-256 representation of 1000000007 is digits: [59, 154, 202, 7].
        // Therefore, string s1 = [(char)59, (char)154, (char)202, (char)72]
        // has value (1000000007 + 65) % 1000000007 = 65.
        // String s2 = [(char)0, (char)0, (char)0, (char)65]
        // has value (0 + 65) % 1000000007 = 65.
        // Both 4-character strings have IDENTICAL hash = 65 under BASE=256, MODULUS=1000000007,
        // but their character sequences are completely different.

        char[] chars1 = new char[]{59, 154, 202, 72};
        char[] chars2 = new char[]{0, 0, 0, 65};
        String s1 = new String(chars1);
        String s2 = new String(chars2);

        long h1 = RabinKarp.computeHash(s1, 0, 4);
        long h2 = RabinKarp.computeHash(s2, 0, 4);
        assertEquals(h1, h2, "Mathematical collision strings must have identical hashes");
        assertFalse(s1.equals(s2), "Collision strings must not be character-identical");

        // Now search for s2 inside a text that contains only s1
        String textContainingS1 = "prefix_" + s1 + "_suffix";
        int[] falseMatchCheck = RabinKarp.search(textContainingS1, s2);
        assertArrayEquals(new int[0], falseMatchCheck,
                "Character collision verification must reject hash-identical but character-different strings");

        // Conversely, searching for s1 in text containing s1 must succeed
        int[] trueMatchCheck = RabinKarp.search(textContainingS1, s1);
        assertArrayEquals(new int[]{7}, trueMatchCheck, "True match must succeed at offset 7");
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

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError("Assertion failed: " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
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
