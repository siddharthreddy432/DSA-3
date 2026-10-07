package engine.graph;

import engine.datastructures.DynamicArray;

/**
 * Custom directed graph implementation built from first principles.
 *
 * <p>Requirements:
 * <ul>
 *   <li>Add node</li>
 *   <li>Add directed edge (preserving direction source -&gt; destination)</li>
 *   <li>Retrieve node</li>
 *   <li>Retrieve outgoing neighbors</li>
 *   <li>Node existence check</li>
 *   <li>Edge existence check</li>
 *   <li>Node count and edge count</li>
 *   <li>Clear / reset state</li>
 *   <li>Preserve deterministic traversal order via ordered {@link DynamicArray} storage</li>
 * </ul>
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 *
 * @param <T> the type of domain data associated with nodes
 */
public class DirectedGraph<T> {

    private final DynamicArray<GraphNode<T>> nodes;
    private final DynamicArray<GraphEdge<T>> edges;

    /**
     * Constructs an empty DirectedGraph.
     */
    public DirectedGraph() {
        this.nodes = new DynamicArray<>();
        this.edges = new DynamicArray<>();
    }

    /**
     * Adds a node with the specified identifier. If the node already exists, returns the existing node.
     *
     * @param id unique node identifier
     * @return existing or newly created GraphNode
     */
    public GraphNode<T> addNode(String id) {
        return addNode(id, null);
    }

    /**
     * Adds a node with specified identifier and data payload. If node already exists, returns existing node.
     *
     * @param id   unique node identifier
     * @param data domain payload
     * @return existing or newly created GraphNode
     */
    public GraphNode<T> addNode(String id, T data) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Node id must not be null or empty");
        }
        GraphNode<T> existing = getNode(id);
        if (existing != null) {
            if (data != null && existing.getData() == null) {
                existing.setData(data);
            }
            return existing;
        }

        GraphNode<T> newNode = new GraphNode<>(id, data);
        newNode.setIndex(nodes.size());
        nodes.add(newNode);
        return newNode;
    }

    /**
     * Adds an existing GraphNode to the graph if not already present.
     *
     * @param node the GraphNode to add
     * @return true if added, false if node with same ID already exists
     */
    public boolean addNode(GraphNode<T> node) {
        if (node == null) {
            throw new IllegalArgumentException("Node must not be null");
        }
        if (containsNode(node.getId())) {
            return false;
        }
        node.setIndex(nodes.size());
        nodes.add(node);
        return true;
    }

    /**
     * Retrieves a node by its identifier.
     *
     * @param id node identifier
     * @return GraphNode if found, or null if not present
     */
    public GraphNode<T> getNode(String id) {
        if (id == null) return null;
        for (int i = 0; i < nodes.size(); i++) {
            GraphNode<T> current = nodes.get(i);
            if (current.getId().equals(id)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Checks if a node with the specified identifier exists in the graph.
     *
     * @param id node identifier
     * @return true if node exists
     */
    public boolean containsNode(String id) {
        return getNode(id) != null;
    }

    /**
     * Checks if the specified node exists in the graph.
     *
     * @param node GraphNode to check
     * @return true if node exists
     */
    public boolean containsNode(GraphNode<T> node) {
        return node != null && containsNode(node.getId());
    }

    /**
     * Adds a directed edge between sourceId and destinationId.
     * Auto-creates nodes if they do not yet exist in the graph.
     *
     * @param sourceId      origin node ID
     * @param destinationId destination node ID
     * @return created or existing GraphEdge
     */
    public GraphEdge<T> addEdge(String sourceId, String destinationId) {
        return addEdge(sourceId, destinationId, null);
    }

    /**
     * Adds a labeled directed edge between sourceId and destinationId.
     * Auto-creates nodes if they do not yet exist in the graph.
     *
     * @param sourceId      origin node ID
     * @param destinationId destination node ID
     * @param label         edge label/relationship
     * @return created GraphEdge, or null if duplicate
     */
    public GraphEdge<T> addEdge(String sourceId, String destinationId, String label) {
        GraphNode<T> src = addNode(sourceId);
        GraphNode<T> dst = addNode(destinationId);
        if (src.hasEdgeTo(dst.getId())) {
            return null;
        }
        GraphEdge<T> edge = new GraphEdge<>(src, dst, label);
        addEdge(edge);
        return edge;
    }

    /**
     * Adds a directed edge between sourceId and destinationId, returning success status.
     *
     * @param sourceId      origin node ID
     * @param destinationId destination node ID
     * @return true if added, false if duplicate
     */
    public boolean addDirectedEdge(String sourceId, String destinationId) {
        return addDirectedEdge(sourceId, destinationId, null);
    }

    /**
     * Adds a labeled directed edge between sourceId and destinationId, returning success status.
     *
     * @param sourceId      origin node ID
     * @param destinationId destination node ID
     * @param label         edge label
     * @return true if added, false if duplicate
     */
    public boolean addDirectedEdge(String sourceId, String destinationId, String label) {
        GraphNode<T> src = addNode(sourceId);
        GraphNode<T> dst = addNode(destinationId);
        return addEdge(src, dst, label);
    }

    /**
     * Adds a directed edge between two existing nodes with a label.
     *
     * @param source      origin GraphNode
     * @param destination destination GraphNode
     * @param label       edge label
     * @return true if edge was added, false if duplicate
     */
    public boolean addEdge(GraphNode<T> source, GraphNode<T> destination, String label) {
        return addEdge(new GraphEdge<>(source, destination, label));
    }

    /**
     * Adds a directed edge between two existing nodes.
     *
     * @param source      origin GraphNode
     * @param destination destination GraphNode
     * @return true if edge was added, false if duplicate
     */
    public boolean addEdge(GraphNode<T> source, GraphNode<T> destination) {
        return addEdge(source, destination, null);
    }

    /**
     * Adds a directed edge to the graph. Prevents duplicate edges between the same nodes.
     *
     * @param edge GraphEdge to add
     * @return true if edge was added, false if already present
     */
    public boolean addEdge(GraphEdge<T> edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Edge must not be null");
        }
        GraphNode<T> src = edge.getSource();
        GraphNode<T> dst = edge.getDestination();

        if (!containsNode(src.getId())) {
            addNode(src);
        }
        if (!containsNode(dst.getId())) {
            addNode(dst);
        }

        // Prevent duplicate edges
        if (src.hasEdgeTo(dst.getId())) {
            return false;
        }

        src.addEdge(edge);
        edges.add(edge);
        return true;
    }

    /**
     * Checks if a directed edge exists from sourceId to destinationId.
     *
     * @param sourceId      origin node ID
     * @param destinationId destination node ID
     * @return true if directed edge exists
     */
    public boolean containsEdge(String sourceId, String destinationId) {
        GraphNode<T> src = getNode(sourceId);
        return src != null && src.hasEdgeTo(destinationId);
    }

    /**
     * Checks if a directed edge exists from source node to destination node.
     *
     * @param source      origin GraphNode
     * @param destination destination GraphNode
     * @return true if directed edge exists
     */
    public boolean containsEdge(GraphNode<T> source, GraphNode<T> destination) {
        if (source == null || destination == null) return false;
        return containsEdge(source.getId(), destination.getId());
    }

    /**
     * Retrieves the outgoing neighbor nodes for a given node ID.
     *
     * @param id node identifier
     * @return dynamic array of adjacent destination nodes, or empty array if node not found
     */
    public DynamicArray<GraphNode<T>> getNeighbors(String id) {
        GraphNode<T> node = getNode(id);
        if (node == null) {
            return new DynamicArray<>();
        }
        return node.getNeighbors();
    }

    /**
     * Retrieves the outgoing neighbor nodes for a given GraphNode.
     *
     * @param node GraphNode
     * @return dynamic array of adjacent destination nodes
     */
    public DynamicArray<GraphNode<T>> getNeighbors(GraphNode<T> node) {
        if (node == null) {
            return new DynamicArray<>();
        }
        return node.getNeighbors();
    }

    /**
     * Retrieves all outgoing edges from the node with specified ID.
     *
     * @param id node identifier
     * @return dynamic array of outgoing edges
     */
    public DynamicArray<GraphEdge<T>> getOutgoingEdges(String id) {
        GraphNode<T> node = getNode(id);
        if (node == null) {
            return new DynamicArray<>();
        }
        return node.getOutgoingEdges();
    }

    /**
     * Retrieves all outgoing edges from the specified GraphNode.
     *
     * @param node GraphNode
     * @return dynamic array of outgoing edges
     */
    public DynamicArray<GraphEdge<T>> getOutgoingEdges(GraphNode<T> node) {
        if (node == null) {
            return new DynamicArray<>();
        }
        return node.getOutgoingEdges();
    }

    /**
     * Returns the dynamic array of all nodes in insertion order.
     *
     * @return dynamic array of nodes
     */
    public DynamicArray<GraphNode<T>> getNodes() {
        return nodes;
    }

    /**
     * Returns the dynamic array of all directed edges in insertion order.
     *
     * @return dynamic array of edges
     */
    public DynamicArray<GraphEdge<T>> getEdges() {
        return edges;
    }

    /**
     * Returns the total count of nodes in the graph.
     *
     * @return node count |V|
     */
    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * Returns the total count of directed edges in the graph.
     *
     * @return edge count |E|
     */
    public int getEdgeCount() {
        return edges.size();
    }

    /**
     * Clears all nodes and edges from this graph.
     */
    public void clear() {
        nodes.clear();
        edges.clear();
    }
}
