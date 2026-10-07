package engine.models;

import engine.correlation.KillChainStage;

/**
 * Immutable model representing a meaningful node in the reconstructed attack graph.
 *
 * <p>Each AttackNode captures an adversary action or compromised state at a specific
 * network asset and point in time, associated with an attack kill-chain stage and
 * underlying evidence.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class AttackNode {

    private final String id;
    private final String source;
    private final String target;
    private final KillChainStage stage;
    private final String timestamp;
    private final SignatureMatch signatureMatch;

    /**
     * Constructs an AttackNode with explicit attribute references.
     *
     * @param id             unique node identifier (e.g., "NODE-001", "AN-HOST-014-01")
     * @param source         source network entity or IP address
     * @param target         target host or compromised endpoint
     * @param stage          associated kill-chain stage
     * @param timestamp      event timestamp
     * @param signatureMatch underlying signature match evidence (may be null if synthetic node)
     * @throws IllegalArgumentException if id or stage is null or blank
     */
    public AttackNode(String id, String source, String target, KillChainStage stage, String timestamp, SignatureMatch signatureMatch) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("AttackNode id must not be null or empty");
        }
        if (stage == null) {
            throw new IllegalArgumentException("AttackNode stage must not be null");
        }

        this.id = id.trim();
        this.source = (source != null) ? source.trim() : "";
        this.target = (target != null) ? target.trim() : "";
        this.stage = stage;
        this.timestamp = (timestamp != null) ? timestamp.trim() : "";
        this.signatureMatch = signatureMatch;
    }

    /**
     * Constructs an AttackNode directly from a SignatureMatch evidence record.
     *
     * @param id             unique node identifier
     * @param signatureMatch underlying signature match evidence
     * @param stage          associated kill-chain stage
     * @throws IllegalArgumentException if required fields are null
     */
    public AttackNode(String id, SignatureMatch signatureMatch, KillChainStage stage) {
        this(
            id,
            (signatureMatch != null && signatureMatch.getLogEntry() != null) ? signatureMatch.getLogEntry().getSourceIp() : "",
            (signatureMatch != null && signatureMatch.getLogEntry() != null) ? signatureMatch.getLogEntry().getTargetHost() : "",
            stage,
            (signatureMatch != null && signatureMatch.getLogEntry() != null) ? signatureMatch.getLogEntry().getTimestamp() : "",
            signatureMatch
        );
    }

    /**
     * Constructs an AttackNode without explicit signature match evidence.
     *
     * @param id        unique node identifier
     * @param source    source entity/IP
     * @param target    target host
     * @param stage     associated kill-chain stage
     * @param timestamp event timestamp
     */
    public AttackNode(String id, String source, String target, KillChainStage stage, String timestamp) {
        this(id, source, target, stage, timestamp, null);
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public KillChainStage getStage() {
        return stage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public SignatureMatch getSignatureMatch() {
        return signatureMatch;
    }

    /**
     * Returns true if this attack node is backed by an explicit Phase 3 signature match.
     *
     * @return true if signatureMatch is non-null
     */
    public boolean hasEvidence() {
        return signatureMatch != null;
    }

    /**
     * Retrieves the matched pattern string from underlying evidence, or null if no evidence.
     *
     * @return matched pattern value or null
     */
    public String getMatchedPattern() {
        return signatureMatch != null ? signatureMatch.getMatchedValue() : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AttackNode other = (AttackNode) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "AttackNode{" +
                "id='" + id + '\'' +
                ", target='" + target + '\'' +
                ", stage=" + stage.getName() +
                ", time='" + timestamp + '\'' +
                (hasEvidence() ? ", ioc=" + signatureMatch.getIoc().getId() : "") +
                '}';
    }
}
