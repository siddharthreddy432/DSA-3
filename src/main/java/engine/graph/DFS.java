package engine.graph;

import engine.datastructures.DynamicArray;
import engine.datastructures.Stack;

/**
 * Depth-First Search (DFS) implementation built from first principles.
 *
 * <p>Requirements:
 * <ul>
 *   <li>Iterative DFS using custom {@link Stack} to eliminate call-stack overflow risk</li>
 *   <li>Visited tracking using index-based primitive arrays without java.util collections</li>
 *   <li>Deterministic traversal ordering matching edge insertion order</li>
 *   <li>Support starting from a designated node or full graph traversal</li>
 * </ul>
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class DFS {

    private DFS() {
        // Prevent instantiation of static utility class
    }

    /**
     * Executes iterative DFS starting from the specified node identifier.
     *
     * @param <T>     node payload type
     * @param graph   directed graph to traverse
     * @param startId starting node identifier
     * @return DynamicArray of GraphNode instances in DFS traversal order
     */
    public static <T> DynamicArray<GraphNode<T>> traverse(DirectedGraph<T> graph, String startId) {
        if (graph == null || startId == null) {
            return new DynamicArray<>();
        }
        GraphNode<T> startNode = graph.getNode(startId);
        if (startNode == null) {
            return new DynamicArray<>();
        }
        return traverse(graph, startNode);
    }

    /**
     * Executes iterative DFS starting from the specified GraphNode.
     *
     * @param <T>       node payload type
     * @param graph     directed graph to traverse
     * @param startNode starting GraphNode
     * @return DynamicArray of GraphNode instances in DFS traversal order
     */
    public static <T> DynamicArray<GraphNode<T>> traverse(DirectedGraph<T> graph, GraphNode<T> startNode) {
        DynamicArray<GraphNode<T>> order = new DynamicArray<>();
        if (graph == null || startNode == null || graph.getNodeCount() == 0) {
            return order;
        }

        int nodeCount = graph.getNodeCount();
        boolean[] visited = new boolean[nodeCount];
        Stack<GraphNode<T>> stack = new Stack<>();

        stack.push(startNode);

        while (!stack.isEmpty()) {
            GraphNode<T> current = stack.pop();
            int idx = current.getIndex();

            // Guard against stale stack entries from multiple discovery paths
            if (idx >= 0 && idx < nodeCount) {
                if (visited[idx]) {
                    continue;
                }
                visited[idx] = true;
            }

            order.add(current);

            // Push neighbors in reverse order so that first neighbor is popped first,
            // preserving deterministic left-to-right DFS traversal order.
            DynamicArray<GraphNode<T>> neighbors = current.getNeighbors();
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                GraphNode<T> neighbor = neighbors.get(i);
                int nIdx = neighbor.getIndex();
                if (nIdx >= 0 && nIdx < nodeCount && !visited[nIdx]) {
                    stack.push(neighbor);
                }
            }
        }

        return order;
    }

    /**
     * Traverses the entire graph across all connected/disconnected components via DFS.
     *
     * @param <T>   node payload type
     * @param graph directed graph to traverse
     * @return DynamicArray of all GraphNodes visited across all components
     */
    public static <T> DynamicArray<GraphNode<T>> traverseAll(DirectedGraph<T> graph) {
        DynamicArray<GraphNode<T>> order = new DynamicArray<>();
        if (graph == null || graph.getNodeCount() == 0) {
            return order;
        }

        int nodeCount = graph.getNodeCount();
        boolean[] visited = new boolean[nodeCount];
        DynamicArray<GraphNode<T>> allNodes = graph.getNodes();

        for (int i = 0; i < allNodes.size(); i++) {
            GraphNode<T> candidate = allNodes.get(i);
            int idx = candidate.getIndex();
            if (idx >= 0 && idx < nodeCount && !visited[idx]) {
                Stack<GraphNode<T>> stack = new Stack<>();
                stack.push(candidate);

                while (!stack.isEmpty()) {
                    GraphNode<T> current = stack.pop();
                    int cIdx = current.getIndex();
                    if (cIdx >= 0 && cIdx < nodeCount) {
                        if (visited[cIdx]) {
                            continue;
                        }
                        visited[cIdx] = true;
                    }
                    order.add(current);

                    DynamicArray<GraphNode<T>> neighbors = current.getNeighbors();
                    for (int j = neighbors.size() - 1; j >= 0; j--) {
                        GraphNode<T> neighbor = neighbors.get(j);
                        int nIdx = neighbor.getIndex();
                        if (nIdx >= 0 && nIdx < nodeCount && !visited[nIdx]) {
                            stack.push(neighbor);
                        }
                    }
                }
            }
        }

        return order;
    }

    /**
     * Tests directed reachability from sourceId to targetId using DFS.
     *
     * @param <T>      node payload type
     * @param graph    directed graph
     * @param sourceId starting node ID
     * @param targetId target destination node ID
     * @return true if targetId is reachable from sourceId
     */
    public static <T> boolean isReachable(DirectedGraph<T> graph, String sourceId, String targetId) {
        if (graph == null || sourceId == null || targetId == null) {
            return false;
        }
        if (sourceId.equals(targetId)) {
            return graph.containsNode(sourceId);
        }

        GraphNode<T> startNode = graph.getNode(sourceId);
        if (startNode == null || !graph.containsNode(targetId)) {
            return false;
        }

        int nodeCount = graph.getNodeCount();
        boolean[] visited = new boolean[nodeCount];
        Stack<GraphNode<T>> stack = new Stack<>();
        stack.push(startNode);

        while (!stack.isEmpty()) {
            GraphNode<T> current = stack.pop();
            int idx = current.getIndex();
            if (idx >= 0 && idx < nodeCount) {
                if (visited[idx]) {
                    continue;
                }
                visited[idx] = true;
            }

            if (current.getId().equals(targetId)) {
                return true;
            }

            DynamicArray<GraphNode<T>> neighbors = current.getNeighbors();
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                GraphNode<T> neighbor = neighbors.get(i);
                int nIdx = neighbor.getIndex();
                if (nIdx >= 0 && nIdx < nodeCount && !visited[nIdx]) {
                    stack.push(neighbor);
                }
            }
        }

        return false;
    }
}
