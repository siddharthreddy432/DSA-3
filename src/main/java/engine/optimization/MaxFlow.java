package engine.optimization;

import engine.datastructures.DynamicArray;
import engine.datastructures.Queue;

/**
 * Maximum Flow computation using the Edmonds-Karp algorithm from first principles.
 *
 * <p>Edmonds-Karp implements the Ford-Fulkerson method using Breadth-First Search (BFS)
 * to discover shortest augmenting paths in the residual network, guaranteeing $O(V \cdot E^2)$
 * worst-case running time on integer capacities.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class MaxFlow {

    private MaxFlow() {
        // Prevent instantiation of static utility class
    }

    /**
     * Computes the maximum flow from source to sink in the given flow network.
     *
     * @param network flow network defining vertices, capacities, and residual links
     * @param source  source vertex identifier
     * @param sink    sink vertex identifier
     * @return MaxFlowResult containing maximum throughput and final edge flows
     * @throws IllegalArgumentException if source or sink is null, equal, or missing from network
     */
    public static MaxFlowResult compute(FlowNetwork network, String source, String sink) {
        if (network == null) {
            throw new IllegalArgumentException("FlowNetwork must not be null");
        }
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Source vertex must not be null or empty");
        }
        if (sink == null || sink.trim().isEmpty()) {
            throw new IllegalArgumentException("Sink vertex must not be null or empty");
        }

        String srcId = source.trim();
        String snkId = sink.trim();

        // Source equals sink behavior explicitly defined: distinct endpoints required
        if (srcId.equals(snkId)) {
            throw new IllegalArgumentException("Source and sink vertices must be distinct: " + srcId);
        }

        int sIdx = network.getVertexIndex(srcId);
        if (sIdx == -1) {
            throw new IllegalArgumentException("Source vertex not found in flow network: " + srcId);
        }

        int tIdx = network.getVertexIndex(snkId);
        if (tIdx == -1) {
            throw new IllegalArgumentException("Sink vertex not found in flow network: " + snkId);
        }

        // Reset existing flows before computation
        network.resetFlows();

        int n = network.getVertexCount();
        int totalFlow = 0;

        int[] parent = new int[n];

        // Repeatedly augment while an augmenting path exists in residual graph
        while (findAugmentingPath(network, sIdx, tIdx, parent)) {
            // Find bottleneck capacity along discovered augmenting path
            int bottleneck = Integer.MAX_VALUE;
            int curr = tIdx;
            while (curr != sIdx) {
                int prev = parent[curr];
                int residual = network.getResidualCapacity(prev, curr);
                if (residual < bottleneck) {
                    bottleneck = residual;
                }
                curr = prev;
            }

            // Augment flow along the path
            curr = tIdx;
            while (curr != sIdx) {
                int prev = parent[curr];
                network.augmentFlow(prev, curr, bottleneck);
                curr = prev;
            }

            totalFlow += bottleneck;
        }

        return new MaxFlowResult(totalFlow, srcId, snkId, network);
    }

    /**
     * Executes BFS in residual graph to discover shortest augmenting path from source to sink.
     *
     * @param network flow network
     * @param sIdx    source vertex index
     * @param tIdx    sink vertex index
     * @param parent  parent index array populated with augmenting path edges
     * @return true if an augmenting path with residual capacity &gt; 0 reaches sink
     */
    private static boolean findAugmentingPath(FlowNetwork network, int sIdx, int tIdx, int[] parent) {
        int n = network.getVertexCount();
        for (int i = 0; i < n; i++) {
            parent[i] = -1;
        }

        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new Queue<>();

        visited[sIdx] = true;
        queue.enqueue(sIdx);

        boolean reachedSink = false;

        while (!queue.isEmpty()) {
            int u = queue.dequeue();
            if (u == tIdx) {
                reachedSink = true;
                break;
            }

            DynamicArray<Integer> neighbors = network.getNeighbors(u);
            for (int i = 0; i < neighbors.size(); i++) {
                int v = neighbors.get(i);
                // Can traverse if unvisited and forward residual capacity > 0
                if (!visited[v] && network.getResidualCapacity(u, v) > 0) {
                    visited[v] = true;
                    parent[v] = u;
                    queue.enqueue(v);

                    if (v == tIdx) {
                        reachedSink = true;
                        break;
                    }
                }
            }

            if (reachedSink) {
                break;
            }
        }

        return reachedSink;
    }
}
