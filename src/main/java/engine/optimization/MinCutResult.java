package engine.optimization;

import engine.datastructures.DynamicArray;

/**
 * Immutable result container for Minimum Cut analysis.
 *
 * <p>Represents the source-sink partition $(S, T)$ and the bottleneck cut edges crossing
 * from $S$ to $T$ in the original network.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class MinCutResult {

    private final int cutValue;
    private final String source;
    private final String sink;
    private final DynamicArray<String> sourcePartition;
    private final DynamicArray<String> sinkPartition;
    private final DynamicArray<FlowEdge> cutEdges;

    /**
     * Constructs a MinCutResult instance.
     *
     * @param cutValue        total minimum cut capacity
     * @param source          source vertex identifier
     * @param sink            sink vertex identifier
     * @param sourcePartition vertices reachable from source in the residual graph ($S$)
     * @param sinkPartition   vertices unreachable from source in the residual graph ($T$)
     * @param cutEdges        directed edges crossing from $S$ to $T$ in the original network
     */
    public MinCutResult(
            int cutValue,
            String source,
            String sink,
            DynamicArray<String> sourcePartition,
            DynamicArray<String> sinkPartition,
            DynamicArray<FlowEdge> cutEdges) {
        this.cutValue = cutValue;
        this.source = source;
        this.sink = sink;
        this.sourcePartition = sourcePartition != null ? sourcePartition : new DynamicArray<>();
        this.sinkPartition = sinkPartition != null ? sinkPartition : new DynamicArray<>();
        this.cutEdges = cutEdges != null ? cutEdges : new DynamicArray<>();
    }

    public int getCutValue() {
        return cutValue;
    }

    public String getSource() {
        return source;
    }

    public String getSink() {
        return sink;
    }

    public DynamicArray<String> getSourcePartition() {
        return sourcePartition;
    }

    public DynamicArray<String> getSinkPartition() {
        return sinkPartition;
    }

    public DynamicArray<FlowEdge> getCutEdges() {
        return cutEdges;
    }

    /**
     * Checks if the specified vertex is in the source-side partition $S$.
     *
     * @param vertexId vertex identifier
     * @return true if vertex is on source side
     */
    public boolean isSourceSide(String vertexId) {
        if (vertexId == null) return false;
        for (int i = 0; i < sourcePartition.size(); i++) {
            if (sourcePartition.get(i).equals(vertexId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the specified vertex is in the sink-side partition $T$.
     *
     * @param vertexId vertex identifier
     * @return true if vertex is on sink side
     */
    public boolean isSinkSide(String vertexId) {
        if (vertexId == null) return false;
        for (int i = 0; i < sinkPartition.size(); i++) {
            if (sinkPartition.get(i).equals(vertexId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies that the minimum cut partition strictly separates source from sink.
     *
     * @return true if source is in S and sink is in T
     */
    public boolean separatesSourceAndSink() {
        return isSourceSide(source) && isSinkSide(sink);
    }

    @Override
    public String toString() {
        return "MinCutResult{" +
                "cutValue=" + cutValue +
                ", |S|=" + sourcePartition.size() +
                ", |T|=" + sinkPartition.size() +
                ", cutEdges=" + cutEdges.size() +
                '}';
    }
}
