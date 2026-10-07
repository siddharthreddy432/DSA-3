package engine.monitoring;

import engine.datastructures.DynamicArray;
import engine.graph.DirectedGraph;
import engine.graph.GraphEdge;
import engine.graph.GraphNode;

/**
 * First-principles monitoring placement optimizer for attack graphs.
 * <p>
 * Provides polynomial-time deterministic approximation algorithms:
 * <ol>
 *   <li><b>Vertex Cover (2-Approximation):</b> Iteratively selects uncovered edges
 *       and includes both incident vertices until all edges are covered.</li>
 *   <li><b>Greedy Set Cover Approximation:</b> Greedily selects candidate vertices
 *       that maximize newly covered incident edges until all edges are covered.</li>
 * </ol>
 * <p>
 * <b>Important Caveat:</b>
 * Monitoring placement uses deterministic approximation algorithms. It does not guarantee
 * the globally minimum monitoring set on arbitrary graphs, as Minimum Vertex Cover
 * and Set Cover are NP-hard.
 * <p>
 * Does not mutate the underlying Phase 4 {@link DirectedGraph}.
 */
public class MonitoringPlacement {

    private static final String CAVEAT_NOTE =
            "Monitoring placement uses deterministic approximation algorithms. " +
            "It does not guarantee the globally minimum monitoring set on arbitrary graphs.";

    /**
     * Internal representation of an undirected edge projection.
     */
    private static class UndirectedEdge {
        final int u;
        final int v;

        UndirectedEdge(int u, int v) {
            this.u = u;
            this.v = v;
        }
    }

    /**
     * Computes a 2-approximation Vertex Cover for monitoring placement.
     * <p>
     * Every directed edge {@code u -> v} is projected into an undirected edge {@code u -- v}.
     * While uncovered edges remain, the algorithm selects the next uncovered edge and adds
     * both endpoints to the monitoring set, marking all incident edges as covered.
     *
     * @param <T> the domain node payload type
     * @param graph the Phase 4 directed attack graph
     * @return the monitoring placement result
     * @throws IllegalArgumentException if graph is null
     */
    public static <T> MonitoringResult<T> computeVertexCover(DirectedGraph<T> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }

        int nodeCount = graph.getNodeCount();
        if (nodeCount == 0) {
            return new MonitoringResult<>(new DynamicArray<>(), 0, 0, 0, 0, 1.0,
                    "2-Approximation Vertex Cover", CAVEAT_NOTE);
        }

        DynamicArray<UndirectedEdge> edges = extractUndirectedEdges(graph);
        int totalEdges = edges.size();

        if (totalEdges == 0) {
            return new MonitoringResult<>(new DynamicArray<>(), 0, nodeCount, 0, 0, 1.0,
                    "2-Approximation Vertex Cover", CAVEAT_NOTE);
        }

        boolean[] edgeCovered = new boolean[totalEdges];
        boolean[] inCover = new boolean[nodeCount];
        int coveredCount = 0;

        // Deterministic 2-approximation: pick uncovered edge, add both endpoints
        for (int i = 0; i < totalEdges; i++) {
            if (!edgeCovered[i]) {
                UndirectedEdge edge = edges.get(i);
                int u = edge.u;
                int v = edge.v;

                inCover[u] = true;
                inCover[v] = true;

                // Mark all edges incident to either u or v as covered
                for (int j = 0; j < totalEdges; j++) {
                    if (!edgeCovered[j]) {
                        UndirectedEdge e = edges.get(j);
                        if (e.u == u || e.v == u || e.u == v || e.v == v) {
                            edgeCovered[j] = true;
                            coveredCount++;
                        }
                    }
                }
            }
        }

        // Build result monitoring points
        DynamicArray<MonitoringPoint<T>> points = new DynamicArray<>();
        DynamicArray<GraphNode<T>> allNodes = graph.getNodes();

        for (int i = 0; i < nodeCount; i++) {
            if (inCover[i]) {
                GraphNode<T> gNode = allNodes.get(i);
                int incident = countIncidentEdges(i, edges);
                @SuppressWarnings("unchecked")
                T payload = (gNode.getData() != null) ? gNode.getData() : (T) gNode.getId();
                points.add(new MonitoringPoint<>(payload, i, "VERTEX_COVER_2_APPROX", incident));
            }
        }

        double ratio = totalEdges == 0 ? 1.0 : (double) coveredCount / totalEdges;
        return new MonitoringResult<>(points, points.size(), nodeCount, totalEdges,
                coveredCount, ratio, "2-Approximation Vertex Cover", CAVEAT_NOTE);
    }

    /**
     * Computes a greedy Set Cover approximation for monitoring placement.
     * <p>
     * Treats each candidate vertex as a subset covering its incident edges.
     * Iteratively selects the vertex that covers the maximum number of currently
     * uncovered edges, breaking ties deterministically by lowest vertex index.
     *
     * @param <T> the domain node payload type
     * @param graph the Phase 4 directed attack graph
     * @return the monitoring placement result
     * @throws IllegalArgumentException if graph is null
     */
    public static <T> MonitoringResult<T> computeGreedySetCover(DirectedGraph<T> graph) {
        return computeGreedySetCover(graph, 1.0);
    }

    /**
     * Computes a greedy Set Cover approximation aiming for a target coverage ratio.
     *
     * @param <T> the domain node payload type
     * @param graph the Phase 4 directed attack graph
     * @param targetCoverageRatio required edge coverage ratio in [0.0, 1.0]
     * @return the monitoring placement result
     * @throws IllegalArgumentException if graph is null or ratio invalid
     */
    public static <T> MonitoringResult<T> computeGreedySetCover(DirectedGraph<T> graph, double targetCoverageRatio) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (targetCoverageRatio < 0.0 || targetCoverageRatio > 1.0) {
            throw new IllegalArgumentException("Target coverage ratio must be between 0.0 and 1.0");
        }

        int nodeCount = graph.getNodeCount();
        if (nodeCount == 0) {
            return new MonitoringResult<>(new DynamicArray<>(), 0, 0, 0, 0, 1.0,
                    "Greedy Set Cover", CAVEAT_NOTE);
        }

        DynamicArray<UndirectedEdge> edges = extractUndirectedEdges(graph);
        int totalEdges = edges.size();

        if (totalEdges == 0) {
            return new MonitoringResult<>(new DynamicArray<>(), 0, nodeCount, 0, 0, 1.0,
                    "Greedy Set Cover", CAVEAT_NOTE);
        }

        boolean[] edgeCovered = new boolean[totalEdges];
        boolean[] inCover = new boolean[nodeCount];
        int coveredCount = 0;

        int requiredCoverCount = (int) Math.ceil(targetCoverageRatio * totalEdges);

        // Iteratively pick the node covering the most uncovered edges
        while (coveredCount < requiredCoverCount) {
            int bestNode = -1;
            int bestGain = 0;

            for (int i = 0; i < nodeCount; i++) {
                if (!inCover[i]) {
                    int gain = 0;
                    for (int j = 0; j < totalEdges; j++) {
                        if (!edgeCovered[j]) {
                            UndirectedEdge e = edges.get(j);
                            if (e.u == i || e.v == i) {
                                gain++;
                            }
                        }
                    }

                    // Strict greater-than for deterministic tie-breaking (first node index wins)
                    if (gain > bestGain) {
                        bestGain = gain;
                        bestNode = i;
                    }
                }
            }

            if (bestNode == -1 || bestGain == 0) {
                // No remaining candidate can cover any more edges
                break;
            }

            inCover[bestNode] = true;
            for (int j = 0; j < totalEdges; j++) {
                if (!edgeCovered[j]) {
                    UndirectedEdge e = edges.get(j);
                    if (e.u == bestNode || e.v == bestNode) {
                        edgeCovered[j] = true;
                        coveredCount++;
                    }
                }
            }
        }

        DynamicArray<MonitoringPoint<T>> points = new DynamicArray<>();
        DynamicArray<GraphNode<T>> allNodes = graph.getNodes();

        for (int i = 0; i < nodeCount; i++) {
            if (inCover[i]) {
                GraphNode<T> gNode = allNodes.get(i);
                int incident = countIncidentEdges(i, edges);
                @SuppressWarnings("unchecked")
                T payload = (gNode.getData() != null) ? gNode.getData() : (T) gNode.getId();
                points.add(new MonitoringPoint<>(payload, i, "GREEDY_SET_COVER", incident));
            }
        }

        double ratio = totalEdges == 0 ? 1.0 : (double) coveredCount / totalEdges;
        return new MonitoringResult<>(points, points.size(), nodeCount, totalEdges,
                coveredCount, ratio, "Greedy Set Cover", CAVEAT_NOTE);
    }

    /**
     * Extracts deduplicated undirected edges from the directed graph without mutating it.
     */
    private static <T> DynamicArray<UndirectedEdge> extractUndirectedEdges(DirectedGraph<T> graph) {
        DynamicArray<UndirectedEdge> list = new DynamicArray<>();
        DynamicArray<GraphNode<T>> nodes = graph.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            GraphNode<T> uNode = nodes.get(i);
            int uIdx = uNode.getIndex();
            DynamicArray<GraphEdge<T>> outEdges = uNode.getOutgoingEdges();

            for (int j = 0; j < outEdges.size(); j++) {
                GraphEdge<T> e = outEdges.get(j);
                int vIdx = e.getDestination().getIndex();

                // Standardize undirected edge representation u <= v to deduplicate
                int min = Math.min(uIdx, vIdx);
                int max = Math.max(uIdx, vIdx);

                boolean exists = false;
                for (int k = 0; k < list.size(); k++) {
                    UndirectedEdge ue = list.get(k);
                    if (ue.u == min && ue.v == max) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    list.add(new UndirectedEdge(min, max));
                }
            }
        }

        return list;
    }

    private static int countIncidentEdges(int nodeIdx, DynamicArray<UndirectedEdge> edges) {
        int count = 0;
        for (int i = 0; i < edges.size(); i++) {
            UndirectedEdge e = edges.get(i);
            if (e.u == nodeIdx || e.v == nodeIdx) {
                count++;
            }
        }
        return count;
    }
}
