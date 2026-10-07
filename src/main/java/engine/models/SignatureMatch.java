package engine.models;

/**
 * Immutable model representing an exact signature match discovered in a log payload.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class SignatureMatch {

    private final IOC ioc;
    private final LogEntry logEntry;
    private final String algorithmUsed;
    private final int matchOffset;
    private final String matchedValue;

    /**
     * Constructs a SignatureMatch instance.
     *
     * @param ioc           the matched Indicator of Compromise definition
     * @param logEntry      the security log entry containing the match
     * @param algorithmUsed identifier of the string matching algorithm used ("KMP", "Z-ALGORITHM", "RABIN-KARP")
     * @param matchOffset   0-based character index in the log payload where the match begins
     * @param matchedValue  the string pattern that was matched
     * @throws IllegalArgumentException if required parameters are null or offset is negative
     */
    public SignatureMatch(IOC ioc, LogEntry logEntry, String algorithmUsed, int matchOffset, String matchedValue) {
        if (ioc == null) {
            throw new IllegalArgumentException("IOC must not be null");
        }
        if (logEntry == null) {
            throw new IllegalArgumentException("LogEntry must not be null");
        }
        if (algorithmUsed == null || algorithmUsed.trim().isEmpty()) {
            throw new IllegalArgumentException("Algorithm name must not be null or empty");
        }
        if (matchOffset < 0) {
            throw new IllegalArgumentException("Match offset cannot be negative: " + matchOffset);
        }
        if (matchedValue == null) {
            throw new IllegalArgumentException("Matched value must not be null");
        }

        this.ioc = ioc;
        this.logEntry = logEntry;
        this.algorithmUsed = algorithmUsed.trim();
        this.matchOffset = matchOffset;
        this.matchedValue = matchedValue;
    }

    public IOC getIoc() {
        return ioc;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }

    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    public int getMatchOffset() {
        return matchOffset;
    }

    public String getMatchedValue() {
        return matchedValue;
    }

    /**
     * Formats this signature match conceptually matching Phase 3 reporting requirements.
     *
     * @return multi-line formatted summary
     */
    public String toReportString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIGNATURE MATCH\n");
        sb.append("IOC: ").append(ioc.getId()).append("\n");
        sb.append("TYPE: ").append(ioc.getType()).append("\n");
        sb.append("VALUE: ").append(matchedValue).append("\n");
        sb.append("LOG: ").append(logEntry.getTimestamp()).append("\n");
        sb.append("HOST: ").append(logEntry.getTargetHost()).append("\n");
        sb.append("OFFSET: ").append(matchOffset).append("\n");
        sb.append("ALGORITHM: ").append(algorithmUsed).append("\n");
        sb.append("SEVERITY: ").append(ioc.getSeverity()).append("\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SignatureMatch other = (SignatureMatch) obj;
        return matchOffset == other.matchOffset &&
               ioc.equals(other.ioc) &&
               logEntry.equals(other.logEntry) &&
               algorithmUsed.equals(other.algorithmUsed) &&
               matchedValue.equals(other.matchedValue);
    }

    @Override
    public int hashCode() {
        int result = ioc.hashCode();
        result = 31 * result + logEntry.hashCode();
        result = 31 * result + algorithmUsed.hashCode();
        result = 31 * result + matchOffset;
        result = 31 * result + matchedValue.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SignatureMatch{" +
                "ioc=" + ioc.getId() +
                ", host=" + logEntry.getTargetHost() +
                ", algorithm='" + algorithmUsed + '\'' +
                ", offset=" + matchOffset +
                ", value='" + matchedValue + '\'' +
                '}';
    }
}
