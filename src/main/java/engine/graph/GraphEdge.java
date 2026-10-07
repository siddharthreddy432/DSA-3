package engine.graph;

/**
 * Custom graph edge representation: source -&gt; destination.
 *
 * <p>Preserves directed orientation from originating node to destination node.
 * Supports optional edge label and metadata without external graph dependencies.
 *
 * @param <T> the type of data stored in connected nodes
 */
public class GraphEdge<T> {

    private final GraphNode<T> source;
    private final GraphNode<T> destination;
    private final String label;
    private final Object metadata;

    /**
     * Constructs a directed GraphEdge without label or metadata.
     *
     * @param source      source node
     * @param destination destination node
     * @throws IllegalArgumentException if source or destination is null
     */
    public GraphEdge(GraphNode<T> source, GraphNode<T> destination) {
        this(source, destination, null, null);
    }

    /**
     * Constructs a directed GraphEdge with a descriptive label.
     *
     * @param source      source node
     * @param destination destination node
     * @param label       descriptive label (e.g., relationship type)
     * @throws IllegalArgumentException if source or destination is null
     */
    public GraphEdge(GraphNode<T> source, GraphNode<T> destination, String label) {
        this(source, destination, label, null);
    }

    /**
     * Constructs a directed GraphEdge with label and metadata.
     *
     * @param source      source node
     * @param destination destination node
     * @param label       descriptive label
     * @param metadata    arbitrary attached metadata
     * @throws IllegalArgumentException if source or destination is null
     */
    public GraphEdge(GraphNode<T> source, GraphNode<T> destination, String label, Object metadata) {
        if (source == null) {
            throw new IllegalArgumentException("GraphEdge source node must not be null");
        }
        if (destination == null) {
            throw new IllegalArgumentException("GraphEdge destination node must not be null");
        }
        this.source = source;
        this.destination = destination;
        this.label = (label != null) ? label.trim() : "";
        this.metadata = metadata;
    }

    public GraphNode<T> getSource() {
        return source;
    }

    public GraphNode<T> getDestination() {
        return destination;
    }

    public String getLabel() {
        return label;
    }

    public Object getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GraphEdge<?> other = (GraphEdge<?>) obj;
        return source.equals(other.source) &&
               destination.equals(other.destination) &&
               label.equals(other.label);
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + destination.hashCode();
        result = 31 * result + label.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GraphEdge{" +
                source.getId() + " -> " + destination.getId() +
                (label.isEmpty() ? "" : " [" + label + "]") +
                '}';
    }
}
