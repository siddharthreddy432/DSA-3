package engine.ingestion;

import engine.datastructures.DynamicArray;
import engine.models.IOC;
import engine.models.LogEntry;
import engine.models.SignatureMatch;
import engine.strings.KMP;
import engine.strings.RabinKarp;
import engine.strings.ZAlgorithm;

/**
 * Correlates parsed security logs against Indicators of Compromise (IoCs)
 * using first-principles string matching algorithms.
 *
 * <p>Supported algorithms:
 * <ul>
 *   <li>{@link Algorithm#KMP} (Knuth-Morris-Pratt)</li>
 *   <li>{@link Algorithm#Z_ALGORITHM} (Z-Algorithm)</li>
 *   <li>{@link Algorithm#RABIN_KARP} (Rabin-Karp Rolling Hash)</li>
 * </ul>
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class SignatureMatcher {

    /**
     * Enumeration of supported Phase 3 string matching algorithms.
     */
    public enum Algorithm {
        KMP,
        Z_ALGORITHM,
        RABIN_KARP
    }

    private SignatureMatcher() {
        // Prevent instantiation of static utility class
    }

    /**
     * Executes the selected string matching algorithm on the given text and pattern.
     *
     * @param text      text string to search
     * @param pattern   pattern to find
     * @param algorithm matching algorithm to execute
     * @return array of 0-based match start offsets
     */
    public static int[] search(String text, String pattern, Algorithm algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm must not be null");
        }

        switch (algorithm) {
            case KMP:
                return KMP.search(text, pattern);
            case Z_ALGORITHM:
                return ZAlgorithm.search(text, pattern);
            case RABIN_KARP:
                return RabinKarp.search(text, pattern);
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }

    /**
     * Correlates a single LogEntry against a single IOC using the specified algorithm.
     *
     * @param log       log entry whose payload will be searched
     * @param ioc       indicator of compromise defining pattern and metadata
     * @param algorithm matching algorithm to use
     * @return DynamicArray of SignatureMatch instances for every match found
     */
    public static DynamicArray<SignatureMatch> match(LogEntry log, IOC ioc, Algorithm algorithm) {
        DynamicArray<SignatureMatch> matches = new DynamicArray<>();
        if (log == null || ioc == null || algorithm == null) {
            return matches;
        }

        String payload = log.getPayload();
        String pattern = ioc.getValue();
        int[] offsets = search(payload, pattern, algorithm);

        String algoName = algorithm.name();
        for (int i = 0; i < offsets.length; i++) {
            matches.add(new SignatureMatch(ioc, log, algoName, offsets[i], pattern));
        }

        return matches;
    }

    /**
     * Correlates an entire array of LogEntry records against an array of IOCs.
     *
     * @param logs      dynamic array of parsed log entries
     * @param iocs      dynamic array of IOC indicators
     * @param algorithm matching algorithm to use
     * @return DynamicArray of all discovered SignatureMatch instances
     */
    public static DynamicArray<SignatureMatch> matchAll(
            DynamicArray<LogEntry> logs,
            DynamicArray<IOC> iocs,
            Algorithm algorithm) {

        DynamicArray<SignatureMatch> allMatches = new DynamicArray<>();
        if (logs == null || iocs == null || algorithm == null) {
            return allMatches;
        }

        int logCount = logs.size();
        int iocCount = iocs.size();

        for (int i = 0; i < logCount; i++) {
            LogEntry log = logs.get(i);
            for (int j = 0; j < iocCount; j++) {
                IOC ioc = iocs.get(j);
                DynamicArray<SignatureMatch> logIocMatches = match(log, ioc, algorithm);
                for (int k = 0; k < logIocMatches.size(); k++) {
                    allMatches.add(logIocMatches.get(k));
                }
            }
        }

        return allMatches;
    }
}
