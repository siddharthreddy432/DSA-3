package engine.alerting;

import engine.datastructures.DynamicArray;

/**
 * Priority-driven router for scheduling and dispatching security alerts.
 * <p>
 * Backed by the first-principles {@link BinaryHeap} ordered by {@link AlertPriority}.
 * Ensures alerts are dequeued deterministically according to severity, kill-chain depth,
 * and arrival sequence.
 */
public class AlertRouter {

    private final BinaryHeap<Alert> heap;

    /**
     * Constructs an AlertRouter.
     */
    public AlertRouter() {
        this.heap = new BinaryHeap<>(AlertPriority.INSTANCE);
    }

    /**
     * Routes an alert by enqueueing it into the internal priority heap.
     *
     * @param alert the security alert to route
     * @throws IllegalArgumentException if alert is null
     */
    public void routeAlert(Alert alert) {
        if (alert == null) {
            throw new IllegalArgumentException("Cannot route null alert");
        }
        heap.insert(alert);
    }

    /**
     * Routes multiple alerts into the priority heap.
     *
     * @param alerts array of alerts to route
     */
    public void routeAll(DynamicArray<Alert> alerts) {
        if (alerts == null) return;
        for (int i = 0; i < alerts.size(); i++) {
            Alert a = alerts.get(i);
            if (a != null) {
                routeAlert(a);
            }
        }
    }

    /**
     * Retrieves and dequeues the next highest priority alert.
     *
     * @return the highest priority alert
     * @throws IllegalStateException if no alerts are pending
     */
    public Alert getNextAlert() {
        return heap.extractMax();
    }

    /**
     * Inspects the next highest priority alert without removing it.
     *
     * @return the highest priority alert
     * @throws IllegalStateException if no alerts are pending
     */
    public Alert peekNextAlert() {
        return heap.peek();
    }

    /**
     * Returns the total count of pending alerts currently queued in the router.
     *
     * @return pending alert count
     */
    public int getPendingAlertCount() {
        return heap.size();
    }

    /**
     * Checks if there are pending alerts waiting to be routed.
     *
     * @return true if one or more alerts are pending
     */
    public boolean hasPendingAlerts() {
        return !heap.isEmpty();
    }

    /**
     * Drains all pending alerts in descending priority order.
     *
     * @return dynamic array containing alerts in priority order
     */
    public DynamicArray<Alert> drainAlerts() {
        DynamicArray<Alert> result = new DynamicArray<>(heap.size());
        while (!heap.isEmpty()) {
            result.add(heap.extractMax());
        }
        return result;
    }

    /**
     * Clears all pending alerts from the router.
     */
    public void clear() {
        heap.clear();
    }
}
