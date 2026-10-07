package engine.graph;

import engine.datastructures.DynamicArray;

/**
 * Custom graph node representation maintaining outgoing directed edges.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 * All outgoing adjacency connections are maintained using custom {@link DynamicArray}.
 *
 * @param <T> the type of user payload/data stored within the node
 */
public class GraphNode<T> {

    private final String id;
    private T data;
    private int index;
    private final DynamicArray<GraphEdge<T>> outgoingEdges;

    /**
     * Constructs a GraphNode with specified identifier.
     *
     * @param id unique node identifier
     * @throws IllegalArgumentException if id is null or blank
     */
    public GraphNode(String id) {
        this(id, null);
    }

    /**
     * Constructs a GraphNode with specified identifier and associated data.
     *
     * @param id   unique node identifier
     * @param data associated domain data (e.g., AttackNode)
     * @throws IllegalArgumentException if id is null or blank
     */
    public GraphNode(String id, T data) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("GraphNode id must not be null or empty");
        }
        this.id = id.trim();
        this.data = data;
        this.index = -1;
        this.outgoingEdges = new DynamicArray<>();
    }

    public String getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Adds an outgoing directed edge from this node.
     *
     * @param edge the directed edge to add
     */
    public void addEdge(GraphEdge<T> edge) {
        if (edge != null) {
            outgoingEdges.add(edge);
        }
    }

    /**
     * Returns the dynamic array of outgoing directed edges.
     *
     * @return dynamic array of outgoing edges
     */
    public DynamicArray<GraphEdge<T>> getOutgoingEdges() {
        return outgoingEdges;
    }

    /**
     * Returns the dynamic array of destination neighbor nodes.
     *
     * @return dynamic array of adjacent neighbor nodes
     */
    public DynamicArray<GraphNode<T>> getNeighbors() {
        DynamicArray<GraphNode<T>> neighbors = new DynamicArray<>(outgoingEdges.size());
        for (int i = 0; i < outgoingEdges.size(); i++) {
            neighbors.add(outgoingEdges.get(i).getDestination());
        }
        return neighbors;
    }

    /**
     * Returns the out-degree (number of outgoing edges) of this node.
     *
     * @return out-degree
     */
    public int getOutDegree() {
        return outgoingEdges.size();
    }

    /**
     * Checks if this node has an outgoing directed edge to the specified destination node ID.
     *
     * @param destinationId target node identifier
     * @return true if outgoing edge exists
     */
    public boolean hasEdgeTo(String destinationId) {
        if (destinationId == null) return false;
        for (int i = 0; i < outgoingEdges.size(); i++) {
            if (outgoingEdges.get(i).getDestination().getId().equals(destinationId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GraphNode<?> other = (GraphNode<?>) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "GraphNode{" +
                "id='" + id + '\'' +
                ", outDegree=" + outgoingEdges.size() +
                '}';
    }
}
