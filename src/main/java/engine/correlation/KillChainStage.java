package engine.correlation;

/**
 * Clean, minimal model representing a distinct stage in the Cyber Kill-Chain.
 *
 * <p>Supports deterministic integer ordering, comparison, and association with attack nodes.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class KillChainStage implements Comparable<KillChainStage> {

    // Canonical Kill-Chain Stages in standard tactical order
    public static final KillChainStage RECONNAISSANCE =
            new KillChainStage(1, "RECONNAISSANCE", "Initial probing, scanning, and authentication attempts");
    public static final KillChainStage EXPLOITATION =
            new KillChainStage(2, "EXPLOITATION", "Execution of unauthorized payloads and exploit tools");
    public static final KillChainStage PRIVILEGE_ESCALATION =
            new KillChainStage(3, "PRIVILEGE_ESCALATION", "Elevating privileges and credential harvesting");
    public static final KillChainStage LATERAL_MOVEMENT =
            new KillChainStage(4, "LATERAL_MOVEMENT", "Network pivoting and remote service access");
    public static final KillChainStage DEFENSE_EVASION =
            new KillChainStage(5, "DEFENSE_EVASION", "Disabling security controls and covering tracks");
    public static final KillChainStage COMMAND_AND_CONTROL =
            new KillChainStage(6, "COMMAND_AND_CONTROL", "Establishing persistence and beaconing channels");
    public static final KillChainStage EXFILTRATION =
            new KillChainStage(7, "EXFILTRATION", "Data staging, collection, and unauthorized egress");

    private final int order;
    private final String name;
    private final String description;

    /**
     * Constructs a KillChainStage with specified order and name.
     *
     * @param order integer indicating chronological/tactical stage order (1, 2, ...)
     * @param name  stage name identifier
     */
    public KillChainStage(int order, String name) {
        this(order, name, "");
    }

    /**
     * Constructs a KillChainStage with order, name, and description.
     *
     * @param order       integer indicating tactical sequence
     * @param name        stage name identifier
     * @param description contextual explanation
     */
    public KillChainStage(int order, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("KillChainStage name must not be null or empty");
        }
        this.order = order;
        this.name = name.trim().toUpperCase();
        this.description = (description != null) ? description.trim() : "";
    }

    public int getOrder() {
        return order;
    }

    /**
     * Alias for {@link #getOrder()} matching stage number terminology.
     *
     * @return stage sequence number
     */
    public int getStageNumber() {
        return order;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Checks if this stage precedes the other stage in tactical ordering.
     *
     * @param other other stage
     * @return true if this.order &lt; other.order
     */
    public boolean isBefore(KillChainStage other) {
        return other != null && this.order < other.order;
    }

    /**
     * Checks if this stage succeeds the other stage in tactical ordering.
     *
     * @param other other stage
     * @return true if this.order &gt; other.order
     */
    public boolean isAfter(KillChainStage other) {
        return other != null && this.order > other.order;
    }

    @Override
    public int compareTo(KillChainStage other) {
        if (other == null) return 1;
        return Integer.compare(this.order, other.order);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        KillChainStage other = (KillChainStage) obj;
        return order == other.order && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        int result = order;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "KillChainStage{" +
                "order=" + order +
                ", name='" + name + '\'' +
                '}';
    }
}
