package engine.alerting;

import engine.datastructures.DynamicArray;

/**
 * Minimal manager and entry point for registering and dispatching security alerts.
 * <p>
 * Delegates priority scheduling and queue maintenance to {@link AlertRouter}.
 * Maintains an internal monotonic sequence counter to guarantee stable FIFO tie-breaking
 * for registered alerts.
 * <p>
 * Does not implement external notifications (SMS, email, webhooks, or dashboards).
 */
public class AlertManager {

    private final AlertRouter router;
    private long sequenceCounter;

    /**
     * Constructs an AlertManager with a fresh AlertRouter.
     */
    public AlertManager() {
        this.router = new AlertRouter();
        this.sequenceCounter = 0;
    }

    /**
     * Registers and routes an alert.
     *
     * @param alert the alert to register
     * @throws IllegalArgumentException if alert is null
     */
    public void registerAlert(Alert alert) {
        if (alert == null) {
            throw new IllegalArgumentException("Cannot register null alert");
        }
        router.routeAlert(alert);
    }

    /**
     * Convenience factory and registration method assigning monotonic sequence number.
     *
     * @param alertId unique alert identifier
     * @param severity severity level
     * @param attackDepth kill chain depth
     * @param sourceReference originating node or entity
     * @param message description text
     * @return the registered Alert instance
     */
    public Alert createAndRegisterAlert(String alertId, int severity, int attackDepth, String sourceReference, String message) {
        Alert alert = new Alert(alertId, severity, attackDepth, sourceReference, message, ++sequenceCounter);
        registerAlert(alert);
        return alert;
    }

    /**
     * Retrieves and dequeues the next highest priority alert.
     *
     * @return highest priority alert
     * @throws IllegalStateException if no alerts are pending
     */
    public Alert getNextAlert() {
        return router.getNextAlert();
    }

    /**
     * Peeks at the next highest priority alert without dequeuing.
     *
     * @return highest priority alert
     * @throws IllegalStateException if no alerts are pending
     */
    public Alert peekNextAlert() {
        return router.peekNextAlert();
    }

    /**
     * Returns the number of alerts currently waiting in the queue.
     *
     * @return pending alert count
     */
    public int getPendingAlertCount() {
        return router.getPendingAlertCount();
    }

    /**
     * Checks if there are pending alerts waiting to be handled.
     *
     * @return true if alerts are pending
     */
    public boolean hasPendingAlerts() {
        return router.hasPendingAlerts();
    }

    /**
     * Drains all pending alerts in strict priority order.
     *
     * @return dynamic array of alerts in priority order
     */
    public DynamicArray<Alert> drainAllAlerts() {
        return router.drainAlerts();
    }

    /**
     * Clears all alerts and resets sequence counter.
     */
    public void clear() {
        router.clear();
        sequenceCounter = 0;
    }
}
