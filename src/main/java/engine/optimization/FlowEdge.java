package engine.optimization;

/**
 * Model representing an edge in a flow network with integer capacity and flow.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class FlowEdge {

    private final String from;
    private final String to;
    private final int capacity;
    private int flow;

    /**
     * Constructs a FlowEdge with specified capacity and initial flow of 0.
     *
     * @param from     source vertex identifier
     * @param to       destination vertex identifier
     * @param capacity maximum integer capacity (must be non-negative)
     * @throws IllegalArgumentException if vertices are null/blank or capacity is negative
     */
    public FlowEdge(String from, String to, int capacity) {
        this(from, to, capacity, 0);
    }

    /**
     * Constructs a FlowEdge with specified capacity and initial flow.
     *
     * @param from     source vertex identifier
     * @param to       destination vertex identifier
     * @param capacity maximum integer capacity (must be non-negative)
     * @param flow     initial flow
     * @throws IllegalArgumentException if vertices are null/blank, capacity is negative, or flow invalid
     */
    public FlowEdge(String from, String to, int capacity, int flow) {
        if (from == null || from.trim().isEmpty()) {
            throw new IllegalArgumentException("Source vertex must not be null or empty");
        }
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination vertex must not be null or empty");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative: " + capacity);
        }

        this.from = from.trim();
        this.to = to.trim();
        this.capacity = capacity;
        this.flow = flow;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getFlow() {
        return flow;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }

    /**
     * Returns remaining forward residual capacity before edge saturation.
     *
     * @return capacity - flow
     */
    public int getResidualCapacity() {
        return capacity - flow;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FlowEdge other = (FlowEdge) obj;
        return capacity == other.capacity &&
               from.equals(other.from) &&
               to.equals(other.to);
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + capacity;
        return result;
    }

    @Override
    public String toString() {
        return "FlowEdge{" + from + " -> " + to + " [" + flow + "/" + capacity + "]}";
    }
}
