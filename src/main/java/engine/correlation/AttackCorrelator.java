package engine.correlation;

import engine.datastructures.DynamicArray;
import engine.graph.DirectedGraph;
import engine.graph.GraphNode;
import engine.graph.PathFinder;
import engine.models.AttackEdge;
import engine.models.AttackNode;
import engine.models.AttackPath;
import engine.models.SignatureMatch;

/**
 * Reconstructs directed attack graphs and correlates Phase 3 evidence into multi-stage attack paths.
 *
 * <p>Separation of Concerns:
 * <ul>
 *   <li>Phase 3 detects evidence (via string pattern matching).</li>
 *   <li>Phase 4 correlates that evidence into nodes, edges, kill-chain stages, and attack paths.</li>
 * </ul>
 *
 * <p>Correlation Signals Utilized:
 * <ul>
 *   <li>Temporal ordering: events sorted chronologically to preserve causality</li>
 *   <li>Spatial continuity: intra-host event progressions targeting identical hosts</li>
 *   <li>Lateral movement transitions: adversary pivoting across network boundaries</li>
 *   <li>Kill-chain progression: multi-stage attack advancement</li>
 * </ul>
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 *
 * <p>Documented Limitations of Current Telemetry Models:
 * <ul>
 *   <li>Network Topology: Phase 3 models lack network topology maps or subnet masks;
 *       host transitions are inferred from source IP and target host fields.</li>
 *   <li>Process Lineage: Telemetry contains payloads but no PID/PPID process tree references.</li>
 *   <li>Exact Matches: Correlates verified Phase 3 matches without probabilistic heuristics.</li>
 * </ul>
 */
public class AttackCorrelator {

    private final KillChain killChain;
    private final DynamicArray<AttackNode> correlatedNodes;
    private final DynamicArray<AttackEdge> correlatedEdges;
    private final DirectedGraph<AttackNode> attackGraph;

    /**
     * Constructs an AttackCorrelator with default canonical kill-chain stages.
     */
    public AttackCorrelator() {
        this(KillChain.createDefault());
    }

    /**
     * Constructs an AttackCorrelator with a custom KillChain configuration.
     *
     * @param killChain custom kill-chain schema
     */
    public AttackCorrelator(KillChain killChain) {
        if (killChain == null) {
            throw new IllegalArgumentException("KillChain must not be null");
        }
        this.killChain = killChain;
        this.correlatedNodes = new DynamicArray<>();
        this.correlatedEdges = new DynamicArray<>();
        this.attackGraph = new DirectedGraph<>();
    }

    /**
     * Correlates an array of Phase 3 SignatureMatch evidence items into a directed attack graph.
     *
     * @param matches dynamic array of detected signature matches
     * @return populated DirectedGraph containing correlated AttackNodes and directed AttackEdges
     */
    public DirectedGraph<AttackNode> correlate(DynamicArray<SignatureMatch> matches) {
        clear();

        if (matches == null || matches.isEmpty()) {
            return attackGraph;
        }

        // 1. Sort matches chronologically using first-principles insertion sort
        DynamicArray<SignatureMatch> sortedMatches = sortMatchesChronologically(matches);

        // 2. Generate AttackNodes for each evidence item
        for (int i = 0; i < sortedMatches.size(); i++) {
            SignatureMatch match = sortedMatches.get(i);
            KillChainStage stage = killChain.determineStage(match);
            String nodeId = "AN-" + (i + 1);

            AttackNode node = new AttackNode(
                nodeId,
                match.getLogEntry().getSourceIp(),
                match.getLogEntry().getTargetHost(),
                stage,
                match.getLogEntry().getTimestamp(),
                match
            );

            correlatedNodes.add(node);
            killChain.addNode(node);
            attackGraph.addNode(node.getId(), node);
        }

        // 3. Correlate edges based on temporal, spatial, and lateral signals
        correlateEdges();

        return attackGraph;
    }

    /**
     * Discovers and constructs directed edges between correlated attack nodes.
     */
    private void correlateEdges() {
        int n = correlatedNodes.size();
        if (n < 2) {
            return;
        }

        // Rule 1: Connect consecutive events on the same host (Intra-Host Escalation)
        for (int i = 0; i < n; i++) {
            AttackNode a = correlatedNodes.get(i);
            for (int j = i + 1; j < n; j++) {
                AttackNode b = correlatedNodes.get(j);
                if (a.getTarget().equals(b.getTarget())) {
                    createAndAddEdge(a, b, "INTRA_HOST_ESCALATION");
                    break; // Connect only to the immediate next event on that host
                }
            }
        }

        // Rule 2: Connect lateral pivots across host boundaries
        for (int i = 0; i < n; i++) {
            AttackNode a = correlatedNodes.get(i);
            for (int j = i + 1; j < n; j++) {
                AttackNode b = correlatedNodes.get(j);
                if (!a.getTarget().equals(b.getTarget())) {
                    // Check if event B is an explicit LATERAL transition or the first event on host B
                    boolean isFirstOnB = isFirstEventOnHost(b, j);
                    boolean isLateralEvent = (b.getSignatureMatch() != null &&
                            b.getSignatureMatch().getLogEntry().getEventType().equals("LATERAL"));

                    if (isFirstOnB || isLateralEvent) {
                        createAndAddEdge(a, b, "LATERAL_PIVOT");
                        break; // Link from current pivot host to newly compromised host
                    }
                }
            }
        }

        // Rule 3: Campaign Sequence Continuity
        // Ensure adjacent chronological events belonging to the same adversary source IP form an attack thread
        for (int i = 0; i < n - 1; i++) {
            AttackNode a = correlatedNodes.get(i);
            AttackNode b = correlatedNodes.get(i + 1);
            if (a.getSource().equals(b.getSource())) {
                createAndAddEdge(a, b, "CAMPAIGN_STEP_PROGRESSION");
            }
        }
    }

    /**
     * Checks if node candidate at index pos is the first event observed on its target host.
     */
    private boolean isFirstEventOnHost(AttackNode candidate, int pos) {
        String host = candidate.getTarget();
        for (int i = 0; i < pos; i++) {
            if (correlatedNodes.get(i).getTarget().equals(host)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Safely adds a directed edge to both graph and internal edge tracking without duplicates.
     */
    private void createAndAddEdge(AttackNode source, AttackNode target, String relationship) {
        if (source.getId().equals(target.getId())) {
            return;
        }

        // Check if graph already contains directed edge source -> target
        if (attackGraph.containsEdge(source.getId(), target.getId())) {
            return;
        }

        String edgeId = "EDGE-" + source.getId() + "->" + target.getId();
        AttackEdge edge = new AttackEdge(edgeId, source, target, relationship, target.getSignatureMatch());

        attackGraph.addEdge(source.getId(), target.getId(), relationship);
        correlatedEdges.add(edge);
    }

    /**
     * In-place stable Insertion Sort sorting matches chronologically by timestamp.
     */
    private DynamicArray<SignatureMatch> sortMatchesChronologically(DynamicArray<SignatureMatch> input) {
        DynamicArray<SignatureMatch> sorted = new DynamicArray<>(input.size());
        for (int i = 0; i < input.size(); i++) {
            sorted.add(input.get(i));
        }

        for (int i = 1; i < sorted.size(); i++) {
            SignatureMatch key = sorted.get(i);
            String keyTime = (key.getLogEntry() != null) ? key.getLogEntry().getTimestamp() : "";
            int j = i - 1;

            while (j >= 0) {
                String prevTime = (sorted.get(j).getLogEntry() != null) ? sorted.get(j).getLogEntry().getTimestamp() : "";
                if (prevTime.compareTo(keyTime) > 0) {
                    sorted.set(j + 1, sorted.get(j));
                    j--;
                } else {
                    break;
                }
            }
            sorted.set(j + 1, key);
        }

        return sorted;
    }

    /**
     * Reconstructs an AttackPath between two correlated attack node identifiers.
     *
     * @param sourceNodeId starting node ID
     * @param targetNodeId destination node ID
     * @return AttackPath sequence or empty AttackPath if unreachable
     */
    public AttackPath reconstructPath(String sourceNodeId, String targetNodeId) {
        return PathFinder.findAttackPath(attackGraph, sourceNodeId, targetNodeId);
    }

    public DirectedGraph<AttackNode> getAttackGraph() {
        return attackGraph;
    }

    public DynamicArray<AttackNode> getCorrelatedNodes() {
        return correlatedNodes;
    }

    public DynamicArray<AttackEdge> getCorrelatedEdges() {
        return correlatedEdges;
    }

    public KillChain getKillChain() {
        return killChain;
    }

    /**
     * Clears graph and correlation state.
     */
    public void clear() {
        correlatedNodes.clear();
        correlatedEdges.clear();
        attackGraph.clear();
        killChain.clear();
    }
}
