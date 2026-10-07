package engine.alerting;

/**
 * Minimal immutable security alert model for Phase 7 priority routing.
 * <p>
 * Captures alert identification, severity level, attack traversal depth,
 * originating source/host, message text, and monotonic insertion sequence.
 */
public class Alert {

    private final String alertId;
    private final int severity;
    private final int attackDepth;
    private final String sourceReference;
    private final String message;
    private final long insertionSequence;

    /**
     * Constructs an Alert.
     *
     * @param alertId unique alert identifier
     * @param severity severity level (higher integer indicates higher severity)
     * @param attackDepth attack progression depth in the kill chain (higher indicates deeper penetration)
     * @param sourceReference source host, IP, or node identifier
     * @param message descriptive alert summary
     * @param insertionSequence monotonic sequence number for stable tie-breaking
     */
    public Alert(String alertId, int severity, int attackDepth, String sourceReference, String message, long insertionSequence) {
        if (alertId == null || alertId.trim().isEmpty()) {
            throw new IllegalArgumentException("Alert ID must not be null or empty");
        }
        this.alertId = alertId.trim();
        this.severity = severity;
        this.attackDepth = attackDepth;
        this.sourceReference = (sourceReference != null) ? sourceReference.trim() : "";
        this.message = (message != null) ? message.trim() : "";
        this.insertionSequence = insertionSequence;
    }

    public String getAlertId() {
        return alertId;
    }

    public int getSeverity() {
        return severity;
    }

    public int getAttackDepth() {
        return attackDepth;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public String getMessage() {
        return message;
    }

    public long getInsertionSequence() {
        return insertionSequence;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Alert other = (Alert) obj;
        return alertId.equals(other.alertId);
    }

    @Override
    public int hashCode() {
        return alertId.hashCode();
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id='" + alertId + '\'' +
                ", sev=" + severity +
                ", depth=" + attackDepth +
                ", seq=" + insertionSequence +
                ", src='" + sourceReference + '\'' +
                ", msg='" + message + '\'' +
                '}';
    }
}
