package engine.models;

import engine.datastructures.DynamicArray;

/**
 * Model representing an ordered sequence of AttackNodes forming a continuous attack path.
 *
 * <p>Preserves exact sequential traversal order from initial ingress node to target sink.
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class AttackPath {

    private final DynamicArray<AttackNode> nodes;
    private final DynamicArray<AttackEdge> edges;

    /**
     * Constructs an empty AttackPath.
     */
    public AttackPath() {
        this.nodes = new DynamicArray<>();
        this.edges = new DynamicArray<>();
    }

    /**
     * Constructs an AttackPath with pre-populated nodes.
     *
     * @param nodes dynamic array of ordered AttackNodes
     */
    public AttackPath(DynamicArray<AttackNode> nodes) {
        this.nodes = new DynamicArray<>();
        this.edges = new DynamicArray<>();
        if (nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                this.nodes.add(nodes.get(i));
            }
        }
    }

    /**
     * Constructs an AttackPath with pre-populated nodes and edges.
     *
     * @param nodes dynamic array of ordered AttackNodes
     * @param edges dynamic array of ordered AttackEdges
     */
    public AttackPath(DynamicArray<AttackNode> nodes, DynamicArray<AttackEdge> edges) {
        this(nodes);
        if (edges != null) {
            for (int i = 0; i < edges.size(); i++) {
                this.edges.add(edges.get(i));
            }
        }
    }

    /**
     * Appends an AttackNode to the end of this path.
     *
     * @param node attack node to append
     */
    public void addNode(AttackNode node) {
        if (node != null) {
            nodes.add(node);
        }
    }

    /**
     * Appends an AttackEdge connecting the preceding node to the current node.
     *
     * @param edge attack edge to append
     */
    public void addEdge(AttackEdge edge) {
        if (edge != null) {
            edges.add(edge);
        }
    }

    /**
     * Returns the node at the specified index in the path sequence.
     *
     * @param index 0-based index
     * @return AttackNode at index
     */
    public AttackNode getNode(int index) {
        return nodes.get(index);
    }

    /**
     * Returns the edge at the specified index in the path sequence.
     *
     * @param index 0-based index
     * @return AttackEdge at index
     */
    public AttackEdge getEdge(int index) {
        return edges.get(index);
    }

    /**
     * Returns the number of nodes in this path.
     *
     * @return node count
     */
    public int nodeCount() {
        return nodes.size();
    }

    /**
     * Returns the number of edges in this path.
     *
     * @return edge count
     */
    public int edgeCount() {
        return edges.size();
    }

    /**
     * Returns the number of hops (edges) in this path.
     * If the path has N nodes, hop length is N - 1 (or 0 if empty).
     *
     * @return hop length
     */
    public int length() {
        return nodes.size() > 1 ? nodes.size() - 1 : 0;
    }

    /**
     * Returns the initial entry/source node of this attack path.
     *
     * @return first AttackNode, or null if path is empty
     */
    public AttackNode getStartNode() {
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    /**
     * Returns the final target/destination node of this attack path.
     *
     * @return last AttackNode, or null if path is empty
     */
    public AttackNode getEndNode() {
        return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
    }

    /**
     * Checks if this path contains no nodes.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * Returns true if this path contains the specified node by ID.
     *
     * @param nodeId node identifier
     * @return true if node is present in path
     */
    public boolean containsNode(String nodeId) {
        if (nodeId == null) return false;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId().equals(nodeId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this path contains the specified AttackNode.
     *
     * @param node AttackNode to check
     * @return true if node is present
     */
    public boolean containsNode(AttackNode node) {
        if (node == null) return false;
        return containsNode(node.getId());
    }

    /**
     * Returns the underlying dynamic array of ordered AttackNodes.
     *
     * @return dynamic array of nodes
     */
    public DynamicArray<AttackNode> getNodes() {
        return nodes;
    }

    /**
     * Returns the underlying dynamic array of ordered AttackEdges.
     *
     * @return dynamic array of edges
     */
    public DynamicArray<AttackEdge> getEdges() {
        return edges;
    }

    /**
     * Formats this path as a human-readable arrow-delimited sequence of node IDs.
     *
     * @return path string representation (e.g., "NODE-1 -> NODE-2 -> NODE-3")
     */
    public String toPathString() {
        if (nodes.isEmpty()) {
            return "[EMPTY PATH]";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            sb.append(nodes.get(i).getId());
            if (i < nodes.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AttackPath other = (AttackPath) obj;
        if (nodes.size() != other.nodes.size()) return false;
        for (int i = 0; i < nodes.size(); i++) {
            if (!nodes.get(i).equals(other.nodes.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (int i = 0; i < nodes.size(); i++) {
            hash = 31 * hash + nodes.get(i).hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        return "AttackPath{" +
                "length=" + length() +
                ", path=" + toPathString() +
                '}';
    }
}
