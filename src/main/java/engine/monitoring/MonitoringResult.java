package engine.monitoring;

import engine.datastructures.DynamicArray;

/**
 * Encapsulates the algorithmic output of monitoring sensor placement.
 * <p>
 * Reports the selected monitoring points, coverage metrics, and algorithm metadata.
 *
 * @param <T> the domain node payload type
 */
public class MonitoringResult<T> {

    private final DynamicArray<MonitoringPoint<T>> monitoringPoints;
    private final int selectedCount;
    private final int totalGraphNodes;
    private final int totalGraphEdges;
    private final int coveredEdgesCount;
    private final double coverageRatio;
    private final String approximationMethod;
    private final String notes;

    /**
     * Constructs a MonitoringResult.
     *
     * @param monitoringPoints array of selected monitoring points
     * @param selectedCount total monitoring points selected
     * @param totalGraphNodes total vertices in the original graph
     * @param totalGraphEdges total edges in the graph representation
     * @param coveredEdgesCount number of edges covered by the selected vertices
     * @param coverageRatio proportion of covered edges in [0.0, 1.0]
     * @param approximationMethod name of the approximation heuristic used
     * @param notes theoretical caveats or notes
     */
    public MonitoringResult(DynamicArray<MonitoringPoint<T>> monitoringPoints,
                            int selectedCount,
                            int totalGraphNodes,
                            int totalGraphEdges,
                            int coveredEdgesCount,
                            double coverageRatio,
                            String approximationMethod,
                            String notes) {
        this.monitoringPoints = monitoringPoints;
        this.selectedCount = selectedCount;
        this.totalGraphNodes = totalGraphNodes;
        this.totalGraphEdges = totalGraphEdges;
        this.coveredEdgesCount = coveredEdgesCount;
        this.coverageRatio = coverageRatio;
        this.approximationMethod = approximationMethod;
        this.notes = notes;
    }

    public DynamicArray<MonitoringPoint<T>> getMonitoringPoints() {
        return monitoringPoints;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public int getTotalGraphNodes() {
        return totalGraphNodes;
    }

    public int getTotalGraphEdges() {
        return totalGraphEdges;
    }

    public int getCoveredEdgesCount() {
        return coveredEdgesCount;
    }

    public double getCoverageRatio() {
        return coverageRatio;
    }

    public String getApproximationMethod() {
        return approximationMethod;
    }

    public String getNotes() {
        return notes;
    }

    /**
     * Checks if a particular node is selected as a monitoring point.
     *
     * @param node the domain node
     * @return true if the node is in the monitoring set
     */
    public boolean containsMonitoringPoint(T node) {
        if (node == null || monitoringPoints == null) {
            return false;
        }
        for (int i = 0; i < monitoringPoints.size(); i++) {
            MonitoringPoint<T> mp = monitoringPoints.get(i);
            if (mp != null && node.equals(mp.getNode())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MonitoringResult{" +
                "selectedCount=" + selectedCount +
                ", totalNodes=" + totalGraphNodes +
                ", totalEdges=" + totalGraphEdges +
                ", coveredEdges=" + coveredEdgesCount +
                ", coverageRatio=" + coverageRatio +
                ", method='" + approximationMethod + '\'' +
                '}';
    }
}
