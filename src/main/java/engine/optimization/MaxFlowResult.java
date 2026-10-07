package engine.optimization;

/**
 * Immutable result container for Maximum Flow computation.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class MaxFlowResult {

    private final int maxFlow;
    private final String source;
    private final String sink;
    private final FlowNetwork network;

    /**
     * Constructs a MaxFlowResult instance.
     *
     * @param maxFlow maximum flow throughput
     * @param source  source vertex identifier
     * @param sink    sink vertex identifier
     * @param network underlying flow network containing saturated flows
     */
    public MaxFlowResult(int maxFlow, String source, String sink, FlowNetwork network) {
        this.maxFlow = maxFlow;
        this.source = source;
        this.sink = sink;
        this.network = network;
    }

    public int getMaxFlow() {
        return maxFlow;
    }

    public String getSource() {
        return source;
    }

    public String getSink() {
        return sink;
    }

    public FlowNetwork getFlowNetwork() {
        return network;
    }

    /**
     * Retrieves the final routed flow along edge from -&gt; to.
     *
     * @param from source vertex ID
     * @param to   destination vertex ID
     * @return flow value, or 0 if vertices not found
     */
    public int getFlow(String from, String to) {
        if (network == null) return 0;
        int u = network.getVertexIndex(from);
        int v = network.getVertexIndex(to);
        if (u == -1 || v == -1) return 0;
        return network.getFlow(u, v);
    }

    /**
     * Retrieves the capacity along edge from -&gt; to.
     *
     * @param from source vertex ID
     * @param to   destination vertex ID
     * @return capacity value, or 0 if vertices not found
     */
    public int getCapacity(String from, String to) {
        if (network == null) return 0;
        int u = network.getVertexIndex(from);
        int v = network.getVertexIndex(to);
        if (u == -1 || v == -1) return 0;
        return network.getCapacity(u, v);
    }

    @Override
    public String toString() {
        return "MaxFlowResult{" +
                "source='" + source + '\'' +
                ", sink='" + sink + '\'' +
                ", maxFlow=" + maxFlow +
                '}';
    }
}
