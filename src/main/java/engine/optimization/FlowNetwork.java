package engine.optimization;

import engine.datastructures.DynamicArray;
import engine.graph.DirectedGraph;
import engine.graph.GraphEdge;
import engine.graph.GraphNode;

/**
 * First-principles flow network representation supporting integer capacities,
 * residual graph maintenance, and conversion from Phase 4 directed graphs.
 *
 * <p>Separation of Concerns:
 * <ul>
 *   <li>Phase 4 graphs represent topological connectivity and threat causality.</li>
 *   <li>FlowNetwork represents capacity constraints and residual networks for optimization.</li>
 *   <li>Real network bandwidth does NOT exist in log telemetry; synthetic capacities are used
 *       for structural bottleneck and flow analysis.</li>
 * </ul>
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class FlowNetwork {

    private final DynamicArray<String> vertices;
    private final DynamicArray<FlowEdge> originalEdges;
    private final DynamicArray<DynamicArray<Integer>> adj;

    private int[][] capacity;
    private int[][] flow;

    private static final int INITIAL_CAPACITY = 16;

    /**
     * Constructs an empty FlowNetwork.
     */
    public FlowNetwork() {
        this(INITIAL_CAPACITY);
    }

    /**
     * Constructs an empty FlowNetwork with expected vertex capacity.
     *
     * @param initialVertexCapacity expected vertex count
     */
    public FlowNetwork(int initialVertexCapacity) {
        int cap = initialVertexCapacity > 0 ? initialVertexCapacity : INITIAL_CAPACITY;
        this.vertices = new DynamicArray<>(cap);
        this.originalEdges = new DynamicArray<>();
        this.adj = new DynamicArray<>(cap);
        this.capacity = new int[cap][cap];
        this.flow = new int[cap][cap];
    }

    /**
     * Adds a vertex to the flow network if not already present.
     *
     * @param vertexId unique vertex identifier
     * @return 0-based integer index of the vertex
     */
    public int addVertex(String vertexId) {
        if (vertexId == null || vertexId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vertex identifier must not be null or empty");
        }
        String cleanId = vertexId.trim();
        int existingIndex = getVertexIndex(cleanId);
        if (existingIndex != -1) {
            return existingIndex;
        }

        int newIndex = vertices.size();
        ensureMatrixCapacity(newIndex + 1);

        vertices.add(cleanId);
        adj.add(new DynamicArray<>());
        return newIndex;
    }

    /**
     * Adds a directed edge with integer capacity between two vertices.
     * Auto-registers vertices if not already present.
     *
     * @param from     source vertex identifier
     * @param to       destination vertex identifier
     * @param capValue integer capacity
     * @throws IllegalArgumentException if vertices are null or capacity is negative
     */
    public void addEdge(String from, String to, int capValue) {
        if (capValue < 0) {
            throw new IllegalArgumentException("Edge capacity must be non-negative: " + capValue);
        }

        int u = addVertex(from);
        int v = addVertex(to);

        // Record forward capacity (accumulate if parallel edges exist)
        capacity[u][v] += capValue;

        // Register adjacency in residual graph (both forward and backward directions for residual traversal)
        DynamicArray<Integer> adjU = adj.get(u);
        if (!containsNeighbor(adjU, v)) {
            adjU.add(v);
        }

        DynamicArray<Integer> adjV = adj.get(v);
        if (!containsNeighbor(adjV, u)) {
            adjV.add(u);
        }

        originalEdges.add(new FlowEdge(from, to, capValue));
    }

    private boolean containsNeighbor(DynamicArray<Integer> neighbors, int target) {
        for (int i = 0; i < neighbors.size(); i++) {
            if (neighbors.get(i) == target) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the 0-based integer index for a vertex identifier.
     *
     * @param vertexId vertex identifier
     * @return index, or -1 if not found
     */
    public int getVertexIndex(String vertexId) {
        if (vertexId == null) return -1;
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).equals(vertexId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retrieves the vertex identifier for a given index.
     *
     * @param index 0-based vertex index
     * @return vertex identifier
     */
    public String getVertexId(int index) {
        return vertices.get(index);
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getEdgeCount() {
        return originalEdges.size();
    }

    public boolean containsVertex(String vertexId) {
        return getVertexIndex(vertexId) != -1;
    }

    public DynamicArray<String> getVertices() {
        return vertices;
    }

    public DynamicArray<FlowEdge> getOriginalEdges() {
        return originalEdges;
    }

    public DynamicArray<Integer> getNeighbors(int vertexIndex) {
        return adj.get(vertexIndex);
    }

    public int getCapacity(int u, int v) {
        return capacity[u][v];
    }

    public int getFlow(int u, int v) {
        return flow[u][v];
    }

    /**
     * Calculates residual capacity along directed edge u -&gt; v.
     *
     * @param u source vertex index
     * @param v destination vertex index
     * @return residual capacity
     */
    public int getResidualCapacity(int u, int v) {
        return capacity[u][v] - flow[u][v];
    }

    /**
     * Augments flow along edge u -&gt; v by delta, and symmetrically updates reverse flow v -&gt; u.
     *
     * @param u     source vertex index
     * @param v     destination vertex index
     * @param delta flow augmentation amount
     */
    public void augmentFlow(int u, int v, int delta) {
        flow[u][v] += delta;
        flow[v][u] -= delta;
    }

    /**
     * Resets all flow values in the network to zero while keeping capacities intact.
     */
    public void resetFlows() {
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                flow[i][j] = 0;
            }
        }
    }

    private void ensureMatrixCapacity(int requiredSize) {
        int currentSize = capacity.length;
        if (requiredSize > currentSize) {
            int newSize = Math.max(requiredSize, currentSize * 2);
            int[][] newCapacity = new int[newSize][newSize];
            int[][] newFlow = new int[newSize][newSize];

            for (int i = 0; i < currentSize; i++) {
                for (int j = 0; j < currentSize; j++) {
                    newCapacity[i][j] = capacity[i][j];
                    newFlow[i][j] = flow[i][j];
                }
            }
            this.capacity = newCapacity;
            this.flow = newFlow;
        }
    }

    /**
     * Factory method creating a FlowNetwork projection from a Phase 4 DirectedGraph.
     * Assigns a uniform synthetic integer capacity to each directed edge.
     *
     * @param graph           Phase 4 directed attack graph
     * @param defaultCapacity synthetic capacity to assign to each edge (e.g., 1 or 10)
     * @return populated FlowNetwork
     */
    public static FlowNetwork fromDirectedGraph(DirectedGraph<?> graph, int defaultCapacity) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph must not be null");
        }
        if (defaultCapacity < 0) {
            throw new IllegalArgumentException("Default capacity must be non-negative: " + defaultCapacity);
        }

        FlowNetwork network = new FlowNetwork(graph.getNodeCount());

        DynamicArray<?> nodes = graph.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            GraphNode<?> node = (GraphNode<?>) nodes.get(i);
            network.addVertex(node.getId());
        }

        DynamicArray<?> edges = graph.getEdges();
        for (int i = 0; i < edges.size(); i++) {
            GraphEdge<?> edge = (GraphEdge<?>) edges.get(i);
            network.addEdge(edge.getSource().getId(), edge.getDestination().getId(), defaultCapacity);
        }

        return network;
    }
}
