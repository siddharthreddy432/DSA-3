package engine.models;

/**
 * Immutable model representing a directed relationship between two attack nodes.
 *
 * <p>Direction is strictly preserved: source node -&gt; target node.
 * Stores the causal relationship and optional corroborating evidence.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 * Does NOT contain future optimization attributes (capacity, flow, cut status, etc.).
 */
public class AttackEdge {

    private final String id;
    private final AttackNode source;
    private final AttackNode target;
    private final String relationship;
    private final SignatureMatch evidence;

    /**
     * Constructs an AttackEdge without explicit signature evidence.
     *
     * @param id           unique edge identifier (e.g., "EDGE-001")
     * @param source       originating AttackNode
     * @param target       destination AttackNode
     * @param relationship causal descriptor (e.g., "INTRA_HOST_ESCALATION", "LATERAL_PIVOT")
     * @throws IllegalArgumentException if id, source, target, or relationship is null/blank
     */
    public AttackEdge(String id, AttackNode source, AttackNode target, String relationship) {
        this(id, source, target, relationship, null);
    }

    /**
     * Constructs an AttackEdge with explicit signature evidence.
     *
     * @param id           unique edge identifier
     * @param source       originating AttackNode
     * @param target       destination AttackNode
     * @param relationship causal descriptor
     * @param evidence     underlying signature match evidence (may be null)
     * @throws IllegalArgumentException if id, source, target, or relationship is null/blank
     */
    public AttackEdge(String id, AttackNode source, AttackNode target, String relationship, SignatureMatch evidence) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("AttackEdge id must not be null or empty");
        }
        if (source == null) {
            throw new IllegalArgumentException("AttackEdge source node must not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("AttackEdge target node must not be null");
        }
        if (relationship == null || relationship.trim().isEmpty()) {
            throw new IllegalArgumentException("AttackEdge relationship must not be null or empty");
        }

        this.id = id.trim();
        this.source = source;
        this.target = target;
        this.relationship = relationship.trim();
        this.evidence = evidence;
    }

    public String getId() {
        return id;
    }

    public AttackNode getSource() {
        return source;
    }

    public AttackNode getTarget() {
        return target;
    }

    public String getRelationship() {
        return relationship;
    }

    public SignatureMatch getEvidence() {
        return evidence;
    }

    public boolean hasEvidence() {
        return evidence != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AttackEdge other = (AttackEdge) obj;
        return id.equals(other.id) &&
               source.equals(other.source) &&
               target.equals(other.target) &&
               relationship.equals(other.relationship);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + relationship.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AttackEdge{" +
                "id='" + id + '\'' +
                ", " + source.getId() + " -> " + target.getId() +
                ", rel='" + relationship + '\'' +
                '}';
    }
}
