package engine.monitoring;

/**
 * Represents a single designated monitoring or sensor location in the graph.
 * <p>
 * Theoretical interpretation: This represents an algorithmic placement location
 * on a graph vertex (e.g. host or entity) where monitoring telemetry can be gathered.
 * It does not imply physical sensor installation or packet capture hardware.
 *
 * @param <T> the domain node payload type
 */
public class MonitoringPoint<T> {

    private final T node;
    private final int nodeIndex;
    private final String placementReason;
    private final int coveredIncidentEdges;

    /**
     * Constructs a MonitoringPoint.
     *
     * @param node the vertex payload
     * @param nodeIndex the stable graph index of the vertex
     * @param placementReason algorithmic justification (e.g., VERTEX_COVER_2_APPROX)
     * @param coveredIncidentEdges number of incident edges covered by this placement
     */
    public MonitoringPoint(T node, int nodeIndex, String placementReason, int coveredIncidentEdges) {
        this.node = node;
        this.nodeIndex = nodeIndex;
        this.placementReason = placementReason;
        this.coveredIncidentEdges = coveredIncidentEdges;
    }

    public T getNode() {
        return node;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public String getPlacementReason() {
        return placementReason;
    }

    public int getCoveredIncidentEdges() {
        return coveredIncidentEdges;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MonitoringPoint<?> other = (MonitoringPoint<?>) obj;
        if (nodeIndex != other.nodeIndex) return false;
        return node == null ? other.node == null : node.equals(other.node);
    }

    @Override
    public int hashCode() {
        return nodeIndex * 31 + (node == null ? 0 : node.hashCode());
    }

    @Override
    public String toString() {
        return "MonitoringPoint{" +
                "node=" + node +
                ", index=" + nodeIndex +
                ", reason='" + placementReason + '\'' +
                ", incidentEdges=" + coveredIncidentEdges +
                '}';
    }
}
