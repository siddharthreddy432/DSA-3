package engine.alerting;

/**
 * Deterministic comparator defining the priority ranking of security alerts.
 * <p>
 * Evaluation order:
 * <ol>
 *   <li><b>Severity:</b> Higher severity dominates (e.g. 5 &gt; 3).</li>
 *   <li><b>Attack Depth:</b> If severity ties, deeper attack penetration dominates (e.g. 4 &gt; 2).</li>
 *   <li><b>Insertion Sequence:</b> If depth ties, earlier/lower monotonic sequence wins (FIFO stability).</li>
 *   <li><b>Alert ID:</b> Lexicographical fallback to ensure strict deterministic ordering.</li>
 * </ol>
 */
public class AlertPriority implements PriorityComparator<Alert> {

    public static final AlertPriority INSTANCE = new AlertPriority();

    @Override
    public int compare(Alert a1, Alert a2) {
        if (a1 == null && a2 == null) return 0;
        if (a1 == null) return -1;
        if (a2 == null) return 1;

        // 1. Severity: higher integer has higher priority
        if (a1.getSeverity() != a2.getSeverity()) {
            return Integer.compare(a1.getSeverity(), a2.getSeverity());
        }

        // 2. Attack Depth: greater depth has higher priority
        if (a1.getAttackDepth() != a2.getAttackDepth()) {
            return Integer.compare(a1.getAttackDepth(), a2.getAttackDepth());
        }

        // 3. Insertion Sequence: lower/earlier sequence has higher priority
        if (a1.getInsertionSequence() != a2.getInsertionSequence()) {
            return Long.compare(a2.getInsertionSequence(), a1.getInsertionSequence());
        }

        // 4. Stable fallback on Alert ID
        return a2.getAlertId().compareTo(a1.getAlertId());
    }

    /**
     * Static utility method comparing two alerts using standard priority rules.
     *
     * @param a1 first alert
     * @param a2 second alert
     * @return positive if a1 has higher priority, negative if a2 has higher priority, 0 if equal
     */
    public static int compareAlerts(Alert a1, Alert a2) {
        return INSTANCE.compare(a1, a2);
    }
}
