package engine.correlation;

import engine.datastructures.DynamicArray;
import engine.models.AttackNode;
import engine.models.IOC;
import engine.models.LogEntry;
import engine.models.SignatureMatch;
import engine.strings.KMP;

/**
 * Kill-Chain structure organizing attack nodes into chronologically and tactically ordered stages.
 *
 * <p>Requirements:
 * <ul>
 *   <li>Deterministic ordering of stages by stage order number</li>
 *   <li>Association of AttackNodes with respective kill-chain stages</li>
 *   <li>Deterministic classification of Phase 3 evidence into stages</li>
 *   <li>Strictly zero java.util collections and zero forbidden search APIs</li>
 * </ul>
 */
public class KillChain {

    private final DynamicArray<KillChainStage> stages;
    private final DynamicArray<DynamicArray<AttackNode>> nodesByStage;

    /**
     * Constructs a KillChain initialized with the canonical 7 cyber kill-chain stages.
     */
    public KillChain() {
        this.stages = new DynamicArray<>();
        this.nodesByStage = new DynamicArray<>();
        initCanonicalStages();
    }

    /**
     * Factory method creating a default KillChain instance.
     *
     * @return configured KillChain with canonical stages
     */
    public static KillChain createDefault() {
        return new KillChain();
    }

    private void initCanonicalStages() {
        addStage(KillChainStage.RECONNAISSANCE);
        addStage(KillChainStage.EXPLOITATION);
        addStage(KillChainStage.PRIVILEGE_ESCALATION);
        addStage(KillChainStage.LATERAL_MOVEMENT);
        addStage(KillChainStage.DEFENSE_EVASION);
        addStage(KillChainStage.COMMAND_AND_CONTROL);
        addStage(KillChainStage.EXFILTRATION);
    }

    /**
     * Adds a KillChainStage maintaining ascending order by stage sequence number.
     *
     * @param stage kill-chain stage to insert
     */
    public void addStage(KillChainStage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("KillChainStage must not be null");
        }

        // Avoid duplicate stages
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).getOrder() == stage.getOrder()) {
                return;
            }
        }

        // Insert in ascending order of stage order number
        int insertIndex = stages.size();
        for (int i = 0; i < stages.size(); i++) {
            if (stage.getOrder() < stages.get(i).getOrder()) {
                insertIndex = i;
                break;
            }
        }

        stages.add(insertIndex, stage);
        nodesByStage.add(insertIndex, new DynamicArray<>());
    }

    /**
     * Returns all registered stages in order.
     *
     * @return dynamic array of stages
     */
    public DynamicArray<KillChainStage> getStages() {
        return stages;
    }

    /**
     * Retrieves a stage by its sequence number.
     *
     * @param order stage order number (e.g. 1, 2)
     * @return KillChainStage or null if not found
     */
    public KillChainStage getStage(int order) {
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).getOrder() == order) {
                return stages.get(i);
            }
        }
        return null;
    }

    /**
     * Retrieves a stage by its name (case-insensitive).
     *
     * @param name stage name
     * @return KillChainStage or null if not found
     */
    public KillChainStage getStageByName(String name) {
        if (name == null) return null;
        String upper = name.trim().toUpperCase();
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).getName().equals(upper)) {
                return stages.get(i);
            }
        }
        return null;
    }

    /**
     * Associates an AttackNode with its corresponding kill-chain stage bucket.
     *
     * @param node AttackNode to associate
     */
    public void addNode(AttackNode node) {
        if (node == null || node.getStage() == null) {
            return;
        }

        int targetOrder = node.getStage().getOrder();
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).getOrder() == targetOrder) {
                nodesByStage.get(i).add(node);
                return;
            }
        }

        // If stage not present, register it and append node
        addStage(node.getStage());
        addNode(node);
    }

    /**
     * Retrieves all AttackNodes associated with the given stage.
     *
     * @param stage kill chain stage
     * @return dynamic array of AttackNodes
     */
    public DynamicArray<AttackNode> getNodesForStage(KillChainStage stage) {
        if (stage == null) {
            return new DynamicArray<>();
        }
        return getNodesForStage(stage.getOrder());
    }

    /**
     * Retrieves all AttackNodes associated with the given stage order number.
     *
     * @param order stage sequence order
     * @return dynamic array of AttackNodes
     */
    public DynamicArray<AttackNode> getNodesForStage(int order) {
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).getOrder() == order) {
                return nodesByStage.get(i);
            }
        }
        return new DynamicArray<>();
    }

    /**
     * Returns total count of nodes associated across all stages.
     *
     * @return total AttackNode count
     */
    public int getTotalNodeCount() {
        int total = 0;
        for (int i = 0; i < nodesByStage.size(); i++) {
            total += nodesByStage.get(i).size();
        }
        return total;
    }

    /**
     * Deterministically classifies a Phase 3 SignatureMatch into a KillChainStage.
     * Uses first-principles pattern matching via {@link KMP#search(String, String)}
     * without calling forbidden string search methods.
     *
     * @param match signature match evidence
     * @return assigned KillChainStage
     */
    public KillChainStage determineStage(SignatureMatch match) {
        if (match == null) {
            return KillChainStage.RECONNAISSANCE;
        }

        LogEntry log = match.getLogEntry();
        IOC ioc = match.getIoc();

        String eventType = (log != null) ? log.getEventType() : "";
        String iocType = (ioc != null) ? ioc.getType() : "";
        String iocVal = (ioc != null) ? ioc.getValue() : "";
        String payload = (log != null) ? log.getPayload() : "";

        if (eventType.equals("AUTH")) {
            return KillChainStage.RECONNAISSANCE;
        }

        if (eventType.equals("LATERAL") || iocType.equals("PROTOCOL")) {
            return KillChainStage.LATERAL_MOVEMENT;
        }

        if (eventType.equals("DEF_EVASION") || iocType.equals("PATTERN")) {
            return KillChainStage.DEFENSE_EVASION;
        }

        if (eventType.equals("C2") || iocType.equals("NETWORK") || iocType.equals("FILE")) {
            return KillChainStage.COMMAND_AND_CONTROL;
        }

        if (eventType.equals("TOOL") || iocType.equals("TOOL")) {
            return KillChainStage.PRIVILEGE_ESCALATION;
        }

        if (eventType.equals("EXFIL")) {
            return KillChainStage.EXFILTRATION;
        }

        if (eventType.equals("EXEC")) {
            // Privilege enumeration check (whoami)
            if (KMP.search(iocVal, "whoami").length > 0 || KMP.search(payload, "whoami").length > 0) {
                return KillChainStage.PRIVILEGE_ESCALATION;
            }
            return KillChainStage.EXPLOITATION;
        }

        return KillChainStage.EXPLOITATION;
    }

    /**
     * Deterministically classifies a LogEntry into a KillChainStage.
     *
     * @param log log entry
     * @return assigned KillChainStage
     */
    public KillChainStage determineStage(LogEntry log) {
        if (log == null) {
            return KillChainStage.RECONNAISSANCE;
        }
        String eventType = log.getEventType();
        if (eventType.equals("AUTH")) return KillChainStage.RECONNAISSANCE;
        if (eventType.equals("LATERAL")) return KillChainStage.LATERAL_MOVEMENT;
        if (eventType.equals("DEF_EVASION")) return KillChainStage.DEFENSE_EVASION;
        if (eventType.equals("C2")) return KillChainStage.COMMAND_AND_CONTROL;
        if (eventType.equals("TOOL")) return KillChainStage.PRIVILEGE_ESCALATION;
        if (eventType.equals("EXFIL")) return KillChainStage.EXFILTRATION;
        if (eventType.equals("EXEC")) {
            if (KMP.search(log.getPayload(), "whoami").length > 0) {
                return KillChainStage.PRIVILEGE_ESCALATION;
            }
            return KillChainStage.EXPLOITATION;
        }
        return KillChainStage.EXPLOITATION;
    }

    /**
     * Clears all attack node associations across all stages while preserving stage schema.
     */
    public void clear() {
        for (int i = 0; i < nodesByStage.size(); i++) {
            nodesByStage.get(i).clear();
        }
    }
}
