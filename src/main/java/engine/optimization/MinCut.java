package engine.optimization;

import engine.datastructures.DynamicArray;
import engine.datastructures.Queue;

/**
 * Minimum Cut analysis implementation built from first principles.
 *
 * <p>By the Max-Flow Min-Cut Theorem, the value of the maximum flow in a network
 * equals the capacity of the minimum $s-t$ cut.
 *
 * <p>After computing maximum flow, the source-side partition $S$ is discovered by
 * reachability analysis (BFS) from source $s$ on the saturated residual graph.
 * The sink-side partition is $T = V \setminus S$. The minimum cut edges are the
 * original directed edges crossing from $S$ to $T$.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class MinCut {

    private MinCut() {
        // Prevent instantiation of static utility class
    }

    /**
     * Computes the minimum $s-t$ cut partition and cut edges in the flow network.
     *
     * @param network flow network
     * @param source  source vertex identifier
     * @param sink    sink vertex identifier
     * @return MinCutResult containing cut capacity, partitions (S, T), and cut edges
     * @throws IllegalArgumentException if source or sink is null, equal, or missing
     */
    public static MinCutResult compute(FlowNetwork network, String source, String sink) {
        // Step 1: Compute Maximum Flow via Edmonds-Karp
        MaxFlowResult maxFlowResult = MaxFlow.compute(network, source, sink);
        int cutValue = maxFlowResult.getMaxFlow();

        String srcId = source.trim();
        String snkId = sink.trim();
        int sIdx = network.getVertexIndex(srcId);
        int n = network.getVertexCount();

        // Step 2: Discover source-side partition S via reachability in the final residual graph
        boolean[] reachable = new boolean[n];
        Queue<Integer> queue = new Queue<>();

        reachable[sIdx] = true;
        queue.enqueue(sIdx);

        while (!queue.isEmpty()) {
            int u = queue.dequeue();
            DynamicArray<Integer> neighbors = network.getNeighbors(u);
            for (int i = 0; i < neighbors.size(); i++) {
                int v = neighbors.get(i);
                // Reachable if unvisited and remaining residual capacity > 0
                if (!reachable[v] && network.getResidualCapacity(u, v) > 0) {
                    reachable[v] = true;
                    queue.enqueue(v);
                }
            }
        }

        // Step 3: Populate S and T partitions
        DynamicArray<String> sourcePartition = new DynamicArray<>();
        DynamicArray<String> sinkPartition = new DynamicArray<>();

        for (int i = 0; i < n; i++) {
            String vId = network.getVertexId(i);
            if (reachable[i]) {
                sourcePartition.add(vId);
            } else {
                sinkPartition.add(vId);
            }
        }

        // Step 4: Identify original edges crossing from S to T
        DynamicArray<FlowEdge> cutEdges = new DynamicArray<>();
        DynamicArray<FlowEdge> originalEdges = network.getOriginalEdges();

        for (int i = 0; i < originalEdges.size(); i++) {
            FlowEdge edge = originalEdges.get(i);
            int uIdx = network.getVertexIndex(edge.getFrom());
            int vIdx = network.getVertexIndex(edge.getTo());

            if (uIdx != -1 && vIdx != -1 && reachable[uIdx] && !reachable[vIdx]) {
                cutEdges.add(edge);
            }
        }

        return new MinCutResult(cutValue, srcId, snkId, sourcePartition, sinkPartition, cutEdges);
    }
}
