package engine.models;

/**
 * Immutable model representing a parsed security log event.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class LogEntry {

    private final String timestamp;
    private final String sourceIp;
    private final String targetHost;
    private final String eventType;
    private final String payload;

    /**
     * Constructs a LogEntry instance.
     *
     * @param timestamp   ISO-8601 or deterministic timestamp (e.g., "2026-09-22T10:43:02")
     * @param sourceIp    source IPv4 or network identifier (e.g., "10.0.14.23")
     * @param targetHost  destination hostname or endpoint (e.g., "HOST-014")
     * @param eventType   log category or event type (e.g., "AUTH", "EXEC", "LATERAL")
     * @param payload     raw command, query, or event body to be inspected
     * @throws IllegalArgumentException if any mandatory field is null
     */
    public LogEntry(String timestamp, String sourceIp, String targetHost, String eventType, String payload) {
        if (timestamp == null || timestamp.trim().isEmpty()) {
            throw new IllegalArgumentException("LogEntry timestamp must not be null or empty");
        }
        if (sourceIp == null || sourceIp.trim().isEmpty()) {
            throw new IllegalArgumentException("LogEntry sourceIp must not be null or empty");
        }
        if (targetHost == null || targetHost.trim().isEmpty()) {
            throw new IllegalArgumentException("LogEntry targetHost must not be null or empty");
        }
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("LogEntry eventType must not be null or empty");
        }
        if (payload == null) {
            throw new IllegalArgumentException("LogEntry payload must not be null");
        }

        this.timestamp = timestamp.trim();
        this.sourceIp = sourceIp.trim();
        this.targetHost = targetHost.trim();
        this.eventType = eventType.trim();
        this.payload = payload;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LogEntry other = (LogEntry) obj;
        return timestamp.equals(other.timestamp) &&
               sourceIp.equals(other.sourceIp) &&
               targetHost.equals(other.targetHost) &&
               eventType.equals(other.eventType) &&
               payload.equals(other.payload);
    }

    @Override
    public int hashCode() {
        int result = timestamp.hashCode();
        result = 31 * result + sourceIp.hashCode();
        result = 31 * result + targetHost.hashCode();
        result = 31 * result + eventType.hashCode();
        result = 31 * result + payload.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "timestamp='" + timestamp + '\'' +
                ", sourceIp='" + sourceIp + '\'' +
                ", targetHost='" + targetHost + '\'' +
                ", eventType='" + eventType + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
