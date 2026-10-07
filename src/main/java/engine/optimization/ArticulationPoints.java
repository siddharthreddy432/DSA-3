package engine.optimization;

import engine.datastructures.DynamicArray;
import engine.graph.DirectedGraph;
import engine.graph.GraphEdge;
import engine.graph.GraphNode;

/**
 * Articulation Point (Cut Vertex) detection using Tarjan's algorithm from first principles.
 *
 * <p>Theoretical Domain Interpretation:
 * <ul>
 *   <li>Articulation points are mathematically defined on <em>undirected</em> graphs.</li>
 *   <li>The Phase 4 attack graph is <em>directed</em>, representing adversary causal progression.</li>
 *   <li><strong>Phase 5 Undirected Connectivity Projection:</strong> For articulation point analysis,
 *       Phase 5 projects every directed edge $u \to v$ into an undirected relationship $u - v$.
 *       This models network structural survivability: an articulation point is a critical host
 *       or bridge node whose removal disconnects the underlying network topology, partitioning
 *       the adversary's ingress routes from internal assets or target databases.</li>
 *   <li>The Phase 4 {@link DirectedGraph} semantics and directionality remain strictly unaltered.</li>
 * </ul>
 *
 * <p>Tarjan DFS Asymptotics:
 * <ul>
 *   <li>Time Complexity: $O(V + E)$ where $V$ is vertex count and $E$ is edge count in the projection.</li>
 *   <li>Space Complexity: $O(V + E)$ for undirected adjacency lists and DFS discovery/low arrays.</li>
 * </ul>
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class ArticulationPoints {

    private ArticulationPoints() {
        // Prevent instantiation of static utility class
    }

    /**
     * Identifies all articulation points (cut vertices) in the given directed graph
     * by constructing its undirected connectivity projection.
     *
     * @param directedGraph Phase 4 directed attack graph
     * @return DynamicArray of vertex IDs identified as articulation points
     */
    public static DynamicArray<String> findArticulationPoints(DirectedGraph<?> directedGraph) {
        if (directedGraph == null || directedGraph.getNodeCount() == 0) {
            return new DynamicArray<>();
        }

        int n = directedGraph.getNodeCount();
        DynamicArray<?> nodes = directedGraph.getNodes();

        // 1. Map vertices to contiguous indices
        DynamicArray<String> vertexIds = new DynamicArray<>(n);
        for (int i = 0; i < n; i++) {
            GraphNode<?> node = (GraphNode<?>) nodes.get(i);
            vertexIds.add(node.getId());
        }

        // 2. Build undirected adjacency projection: for each directed edge u -> v, add u - v
        DynamicArray<DynamicArray<Integer>> adj = new DynamicArray<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new DynamicArray<>());
        }

        DynamicArray<?> edges = directedGraph.getEdges();
        for (int i = 0; i < edges.size(); i++) {
            GraphEdge<?> edge = (GraphEdge<?>) edges.get(i);
            int u = getIndex(vertexIds, edge.getSource().getId());
            int v = getIndex(vertexIds, edge.getDestination().getId());

            if (u != -1 && v != -1 && u != v) {
                // Add u -> v if not present
                if (!containsNeighbor(adj.get(u), v)) {
                    adj.get(u).add(v);
                }
                // Add v -> u for undirected projection
                if (!containsNeighbor(adj.get(v), u)) {
                    adj.get(v).add(u);
                }
            }
        }

        // 3. Tarjan's DFS state
        int[] disc = new int[n];
        int[] low = new int[n];
        int[] parent = new int[n];
        boolean[] visited = new boolean[n];
        boolean[] isArticulationPoint = new boolean[n];

        for (int i = 0; i < n; i++) {
            parent[i] = -1;
            disc[i] = -1;
            low[i] = -1;
        }

        int[] time = new int[]{0};

        // Traverse all connected/disconnected components
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                tarjanDFS(i, adj, disc, low, parent, visited, isArticulationPoint, time);
            }
        }

        // 4. Collect identified articulation points in deterministic vertex order
        DynamicArray<String> result = new DynamicArray<>();
        for (int i = 0; i < n; i++) {
            if (isArticulationPoint[i]) {
                result.add(vertexIds.get(i));
            }
        }

        return result;
    }

    /**
     * Tarjan low-link DFS exploration.
     */
    private static void tarjanDFS(
            int u,
            DynamicArray<DynamicArray<Integer>> adj,
            int[] disc,
            int[] low,
            int[] parent,
            boolean[] visited,
            boolean[] isAP,
            int[] time) {

        visited[u] = true;
        time[0]++;
        disc[u] = time[0];
        low[u] = time[0];

        int children = 0;
        DynamicArray<Integer> neighbors = adj.get(u);

        for (int i = 0; i < neighbors.size(); i++) {
            int v = neighbors.get(i);

            if (v == parent[u]) {
                // Ignore back-edge to direct parent in DFS tree
                continue;
            }

            if (visited[v]) {
                // Back-edge to an ancestor
                if (disc[v] < low[u]) {
                    low[u] = disc[v];
                }
            } else {
                // Tree-edge to an unvisited child
                children++;
                parent[v] = u;

                tarjanDFS(v, adj, disc, low, parent, visited, isAP, time);

                // Update low-link upon child return
                if (low[v] < low[u]) {
                    low[u] = low[v];
                }

                // Condition 1: Root of DFS tree is an articulation point iff it has >= 2 children
                if (parent[u] == -1 && children > 1) {
                    isAP[u] = true;
                }

                // Condition 2: Non-root vertex is an articulation point iff low[child] >= disc[u]
                if (parent[u] != -1 && low[v] >= disc[u]) {
                    isAP[u] = true;
                }
            }
        }
    }

    private static int getIndex(DynamicArray<String> vertexIds, String id) {
        if (id == null) return -1;
        for (int i = 0; i < vertexIds.size(); i++) {
            if (vertexIds.get(i).equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean containsNeighbor(DynamicArray<Integer> neighbors, int val) {
        for (int i = 0; i < neighbors.size(); i++) {
            if (neighbors.get(i) == val) {
                return true;
            }
        }
        return false;
    }
}
