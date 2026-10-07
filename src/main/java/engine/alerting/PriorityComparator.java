package engine.alerting;

/**
 * Functional comparator interface for first-principles priority queues.
 * <p>
 * Returns a positive integer if {@code a} has higher priority than {@code b},
 * a negative integer if {@code a} has lower priority than {@code b},
 * or zero if they have equal priority.
 *
 * @param <T> the compared element type
 */
@FunctionalInterface
public interface PriorityComparator<T> {

    /**
     * Compares two elements for priority order.
     *
     * @param a first element
     * @param b second element
     * @return positive if a &gt; b, negative if a &lt; b, zero if equal
     */
    int compare(T a, T b);
}
