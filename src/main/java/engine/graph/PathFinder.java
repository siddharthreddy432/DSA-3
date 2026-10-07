package engine.graph;

import engine.datastructures.DynamicArray;
import engine.datastructures.Queue;
import engine.datastructures.Stack;
import engine.models.AttackEdge;
import engine.models.AttackNode;
import engine.models.AttackPath;

/**
 * Path reconstruction service built from first principles on top of {@link DirectedGraph}.
 *
 * <p>Requirements:
 * <ul>
 *   <li>Path finding from source to destination using unweighted BFS level exploration</li>
 *   <li>Explicit reconstruction of node sequence using parent-pointer backtracking and custom {@link Stack}</li>
 *   <li>Reporting empty path when no directed path exists</li>
 *   <li>Support conversion to domain-specific {@link AttackPath} model</li>
 * </ul>
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 * Strictly avoids weighted shortest-path algorithms reserved for later phases (no Dijkstra, Bellman-Ford, Floyd-Warshall).
 */
public class PathFinder {

    private PathFinder() {
        // Prevent instantiation of static utility class
    }

    /**
     * Finds a directed path from sourceId to destinationId in the graph.
     *
     * @param <T>           node payload type
     * @param graph         directed graph
     * @param sourceId      source node ID
     * @param destinationId target destination node ID
     * @return DynamicArray of GraphNode instances forming the path from source to destination;
     *         empty array if no path exists or nodes are missing.
     */
    public static <T> DynamicArray<GraphNode<T>> findPath(DirectedGraph<T> graph, String sourceId, String destinationId) {
        if (graph == null || sourceId == null || destinationId == null) {
            return new DynamicArray<>();
        }

        GraphNode<T> startNode = graph.getNode(sourceId);
        GraphNode<T> endNode = graph.getNode(destinationId);
        if (startNode == null || endNode == null) {
            return new DynamicArray<>();
        }

        return findPath(graph, startNode, endNode);
    }

    /**
     * Finds a directed path from source node to destination node.
     *
     * @param <T>         node payload type
     * @param graph       directed graph
     * @param source      source GraphNode
     * @param destination destination GraphNode
     * @return DynamicArray of GraphNodes from source to destination, or empty array if unreachable
     */
    @SuppressWarnings("unchecked")
    public static <T> DynamicArray<GraphNode<T>> findPath(
            DirectedGraph<T> graph,
            GraphNode<T> source,
            GraphNode<T> destination) {

        DynamicArray<GraphNode<T>> path = new DynamicArray<>();
        if (graph == null || source == null || destination == null) {
            return path;
        }

        // Handle trivial self-loop / identity path
        if (source.getId().equals(destination.getId())) {
            path.add(source);
            return path;
        }

        int nodeCount = graph.getNodeCount();
        boolean[] visited = new boolean[nodeCount];
        int[] parentIndex = new int[nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            parentIndex[i] = -1;
        }

        Queue<GraphNode<T>> queue = new Queue<>();
        int sIdx = source.getIndex();
        if (sIdx >= 0 && sIdx < nodeCount) {
            visited[sIdx] = true;
        }
        queue.enqueue(source);

        boolean found = false;

        while (!queue.isEmpty()) {
            GraphNode<T> current = queue.dequeue();
            if (current.getId().equals(destination.getId())) {
                found = true;
                break;
            }

            DynamicArray<GraphNode<T>> neighbors = current.getNeighbors();
            for (int i = 0; i < neighbors.size(); i++) {
                GraphNode<T> neighbor = neighbors.get(i);
                int nIdx = neighbor.getIndex();
                if (nIdx >= 0 && nIdx < nodeCount && !visited[nIdx]) {
                    visited[nIdx] = true;
                    parentIndex[nIdx] = current.getIndex();
                    queue.enqueue(neighbor);

                    if (neighbor.getId().equals(destination.getId())) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                break;
            }
        }

        if (!found) {
            return path; // empty path
        }

        // Reconstruct path by following parent indices from destination back to source
        Stack<GraphNode<T>> reverseStack = new Stack<>();
        DynamicArray<GraphNode<T>> allNodes = graph.getNodes();

        int currIdx = destination.getIndex();
        while (currIdx != -1) {
            reverseStack.push(allNodes.get(currIdx));
            if (currIdx == source.getIndex()) {
                break;
            }
            currIdx = parentIndex[currIdx];
        }

        // Pop from stack to produce forward source-to-destination path order
        while (!reverseStack.isEmpty()) {
            path.add(reverseStack.pop());
        }

        return path;
    }

    /**
     * Checks if a directed path exists between sourceId and destinationId.
     *
     * @param <T>           node payload type
     * @param graph         directed graph
     * @param sourceId      source node ID
     * @param destinationId destination node ID
     * @return true if a valid directed path exists
     */
    public static <T> boolean hasPath(DirectedGraph<T> graph, String sourceId, String destinationId) {
        DynamicArray<GraphNode<T>> path = findPath(graph, sourceId, destinationId);
        return !path.isEmpty();
    }

    /**
     * Reconstructs an end-to-end {@link AttackPath} model between two attack nodes in an attack graph.
     *
     * @param attackGraph   directed graph containing AttackNode instances
     * @param sourceId      origin attack node ID
     * @param destinationId target attack node ID
     * @return AttackPath model containing ordered nodes and reconstructed edges, or empty AttackPath if unreachable
     */
    public static AttackPath findAttackPath(DirectedGraph<AttackNode> attackGraph, String sourceId, String destinationId) {
        DynamicArray<GraphNode<AttackNode>> nodePath = findPath(attackGraph, sourceId, destinationId);
        if (nodePath.isEmpty()) {
            return new AttackPath();
        }

        AttackPath attackPath = new AttackPath();
        for (int i = 0; i < nodePath.size(); i++) {
            GraphNode<AttackNode> gNode = nodePath.get(i);
            AttackNode aNode = gNode.getData();
            if (aNode == null) {
                // If node data is null, instantiate basic representation from id
                aNode = new AttackNode(gNode.getId(), "", "", null, "");
            }
            attackPath.addNode(aNode);
        }

        // Reconstruct connecting attack edges between consecutive nodes in the path
        for (int i = 0; i < attackPath.nodeCount() - 1; i++) {
            AttackNode u = attackPath.getNode(i);
            AttackNode v = attackPath.getNode(i + 1);
            String edgeId = "EDGE-" + u.getId() + "->" + v.getId();
            AttackEdge edge = new AttackEdge(edgeId, u, v, "ATTACK_STEP");
            attackPath.addEdge(edge);
        }

        return attackPath;
    }
}
