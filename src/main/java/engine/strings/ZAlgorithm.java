package engine.strings;

import engine.datastructures.DynamicArray;

/**
 * Z-Algorithm exact string matching implemented from first principles.
 *
 * <p>Architectural constraints:
 * <ul>
 *   <li>Zero usage of {@code java.util} collections or helper libraries.</li>
 *   <li>Zero usage of {@code String.indexOf()}, {@code String.contains()}, or regex.</li>
 *   <li>Separator-free search: uses direct two-phase Z-box window matching to completely
 *       avoid delimiter collisions.</li>
 * </ul>
 *
 * <p>Complexity:
 * <ul>
 *   <li>Pattern Z-table construction: O(m) time, O(m) auxiliary space where m = pattern.length()</li>
 *   <li>Search: O(n) time where n = text.length()</li>
 *   <li>Total: O(n + m) time, O(m) space</li>
 * </ul>
 */
public class ZAlgorithm {

    private ZAlgorithm() {
        // Prevent instantiation of static utility class
    }

    /**
     * Constructs the Z-array for the given string.
     *
     * <p>For string {@code s}, {@code Z[i]} is the length of the longest substring starting
     * from {@code s[i]} that matches a prefix of {@code s}. By standard convention, {@code Z[0] = 0}.
     *
     * @param str the input string
     * @return the Z-array, or an empty array if input is null or empty
     */
    public static int[] buildZ(String str) {
        if (str == null || str.length() == 0) {
            return new int[0];
        }

        int len = str.length();
        int[] z = new int[len];
        z[0] = 0;

        int l = 0;
        int r = 0;

        for (int i = 1; i < len; i++) {
            if (i > r) {
                l = i;
                r = i;
                while (r < len && str.charAt(r - l) == str.charAt(r)) {
                    r++;
                }
                z[i] = r - l;
                r--;
            } else {
                int k = i - l;
                if (z[k] < r - i + 1) {
                    z[i] = z[k];
                } else {
                    l = i;
                    while (r < len && str.charAt(r - l) == str.charAt(r)) {
                        r++;
                    }
                    z[i] = r - l;
                    r--;
                }
            }
        }

        return z;
    }

    /**
     * Searches for all occurrences of pattern in text using a separator-free two-phase Z-box.
     *
     * <p>Instead of concatenating pattern and text with a delimiter (which risks collision
     * if the delimiter appears in payload data), this implementation computes the pattern's
     * Z-array and maintains a dynamic Z-box [L, R] over the text.
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
        int[] zPattern = buildZ(pattern);

        DynamicArray<Integer> matches = new DynamicArray<>();
        int l = 0;
        int r = -1;

        for (int i = 0; i <= n - m; i++) {
            if (i > r) {
                int matchLen = 0;
                while (matchLen < m && (i + matchLen) < n && text.charAt(i + matchLen) == pattern.charAt(matchLen)) {
                    matchLen++;
                }
                if (matchLen > 0) {
                    l = i;
                    r = i + matchLen - 1;
                }
                if (matchLen == m) {
                    matches.add(i);
                }
            } else {
                int k = i - l;
                int rem = r - i + 1;

                if (zPattern[k] < rem) {
                    // Match length at i is strictly zPattern[k] < rem <= m, so cannot be a full match
                    // No expansion possible beyond i + zPattern[k]
                } else {
                    // First 'rem' characters match pattern[0..rem-1]. Extend beyond r:
                    int matchLen = rem;
                    while (matchLen < m && (i + matchLen) < n && text.charAt(i + matchLen) == pattern.charAt(matchLen)) {
                        matchLen++;
                    }
                    l = i;
                    r = i + matchLen - 1;
                    if (matchLen == m) {
                        matches.add(i);
                    }
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
