package engine.strings;

import engine.datastructures.DynamicArray;

/**
 * Knuth-Morris-Pratt (KMP) exact string matching algorithm implemented from first principles.
 *
 * <p>Architectural constraints:
 * <ul>
 *   <li>Zero usage of {@code java.util} collections or helper libraries.</li>
 *   <li>Zero usage of {@code String.indexOf()}, {@code String.contains()}, or regex.</li>
 *   <li>Full support for overlapping matches, repeated prefixes, and edge cases.</li>
 * </ul>
 *
 * <p>Complexity:
 * <ul>
 *   <li>Preprocessing (LPS): O(m) time, O(m) auxiliary space where m = pattern.length()</li>
 *   <li>Search: O(n) time where n = text.length()</li>
 *   <li>Total: O(n + m) time, O(m) space</li>
 * </ul>
 */
public class KMP {

    private KMP() {
        // Prevent instantiation of static utility class
    }

    /**
     * Constructs the Longest Proper Prefix which is also Suffix (LPS) table for the given pattern.
     *
     * <p>For each index {@code i}, {@code lps[i]} stores the length of the longest proper prefix
     * of {@code pattern[0..i]} that is also a suffix of {@code pattern[0..i]}.
     *
     * @param pattern the search pattern
     * @return an integer array containing the LPS values, or an empty array if pattern is null/empty
     */
    public static int[] buildLPS(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            return new int[0];
        }

        int m = pattern.length();
        int[] lps = new int[m];
        lps[0] = 0;

        int len = 0;
        int i = 1;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }

        return lps;
    }

    /**
     * Searches for all occurrences of the pattern within the text using the KMP algorithm.
     *
     * @param text    the text to search within
     * @param pattern the pattern to search for
     * @return an array of 0-based start indices where the pattern occurs, including overlapping matches;
     *         returns an empty array if no match is found or if inputs are null/empty/invalid
     */
    public static int[] search(String text, String pattern) {
        if (text == null || pattern == null || pattern.length() == 0 || text.length() < pattern.length()) {
            return new int[0];
        }

        int n = text.length();
        int m = pattern.length();
        int[] lps = buildLPS(pattern);

        DynamicArray<Integer> matches = new DynamicArray<>();
        int i = 0; // index in text
        int j = 0; // index in pattern

        while (i < n) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }

            if (j == m) {
                // Full match found at index (i - j)
                matches.add(i - j);
                // Shift pattern according to LPS to find overlapping matches
                j = lps[j - 1];
            } else if (i < n && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
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
