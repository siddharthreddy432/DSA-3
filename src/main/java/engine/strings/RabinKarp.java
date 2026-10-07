package engine.strings;

import engine.datastructures.DynamicArray;

/**
 * Rabin-Karp exact string matching algorithm implemented from first principles.
 *
 * <p>Architectural constraints:
 * <ul>
 *   <li>Zero usage of {@code java.util} collections or helper libraries.</li>
 *   <li>Zero usage of {@code String.indexOf()}, {@code String.contains()}, or regex.</li>
 *   <li>Explicit polynomial rolling hash sliding window.</li>
 *   <li>Mandatory character-by-character collision verification upon hash equality.</li>
 * </ul>
 *
 * <p>Complexity:
 * <ul>
 *   <li>Average: O(n + m) time, O(1) auxiliary space</li>
 *   <li>Worst-case (adversarial hash collisions): O(nm) time, O(1) space</li>
 * </ul>
 */
public class RabinKarp {

    /**
     * Radix / base size corresponding to extended ASCII / byte character space.
     */
    public static final int BASE = 256;

    /**
     * Large prime modulus to minimize hash collisions while preventing 64-bit overflow.
     */
    public static final long MODULUS = 1000000007L;

    private RabinKarp() {
        // Prevent instantiation of static utility class
    }

    /**
     * Computes the polynomial hash of a substring {@code s[start .. start + length - 1]}.
     *
     * <p>Formula: H = (sum_{j=0}^{length-1} s[start+j] * BASE^(length - 1 - j)) mod MODULUS.
     *
     * @param s      the source string
     * @param start  the starting character offset
     * @param length the number of characters in the window
     * @return the computed hash in [0, MODULUS - 1]
     */
    public static long computeHash(String s, int start, int length) {
        if (s == null || start < 0 || length <= 0 || start + length > s.length()) {
            return 0L;
        }

        long hash = 0L;
        for (int i = 0; i < length; i++) {
            hash = (hash * BASE + s.charAt(start + i)) % MODULUS;
        }
        return hash;
    }

    /**
     * Searches for all occurrences of pattern in text using the Rabin-Karp rolling hash algorithm.
     *
     * <p>Upon matching hash values, explicit character-by-character verification is performed
     * to eliminate false positives from hash collisions.
     *
     * @param text    the text to search within
     * @param pattern the pattern to search for
     * @return an array of 0-based start indices of all occurrences (including overlapping matches);
     *         returns an empty array if no match is found or if inputs are invalid
     */
    public static int[] search(String text, String pattern) {
        if (text == null || pattern == null || pattern.length() == 0 || text.length() < pattern.length()) {
            return new int[0];
        }

        int n = text.length();
        int m = pattern.length();

        // Compute highest power: h = BASE^(m - 1) % MODULUS
        long h = 1L;
        for (int i = 0; i < m - 1; i++) {
            h = (h * BASE) % MODULUS;
        }

        long patternHash = computeHash(pattern, 0, m);
        long textHash = computeHash(text, 0, m);

        DynamicArray<Integer> matches = new DynamicArray<>();

        for (int i = 0; i <= n - m; i++) {
            // Check hash equivalence first
            if (patternHash == textHash) {
                // Mandatory collision verification: verify characters one by one
                boolean match = true;
                for (int j = 0; j < m; j++) {
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    matches.add(i);
                }
            }

            // Slide window: compute next hash using rolling update
            if (i < n - m) {
                long removedTerm = (text.charAt(i) * h) % MODULUS;
                long intermediate = (textHash - removedTerm) % MODULUS;
                if (intermediate < 0) {
                    intermediate += MODULUS;
                }
                textHash = (intermediate * BASE + text.charAt(i + m)) % MODULUS;
            }
        }

        return toIntArray(matches);
    }

    /**
     * Helper to convert custom DynamicArray of Integers to primitive int array.
     */
    private static int[] toIntArray(DynamicArray<Integer> list) {
        int size = list.size();
        int[] result = new int[size];
        for (int k = 0; k < size; k++) {
            result[k] = list.get(k);
        }
        return result;
    }
}
