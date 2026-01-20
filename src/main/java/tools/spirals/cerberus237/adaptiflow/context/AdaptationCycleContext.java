package tools.spirals.cerberus237.adaptiflow.context;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Context for tracking adaptation cycles across distributed services
 */
public class AdaptationCycleContext {
    private static final AtomicLong CYCLE_COUNTER = new AtomicLong(0);
    private static final ThreadLocal<String> currentCycleId = new ThreadLocal<>();
    private static final ThreadLocal<Long> observationStartTime = new ThreadLocal<>();

    public static String generateCycleId(String serviceName) {
        return serviceName + "-" + System.currentTimeMillis() + "-" + CYCLE_COUNTER.incrementAndGet();
    }

    public static void setCurrentCycle(String cycleId) {
        currentCycleId.set(cycleId);
        observationStartTime.set(System.nanoTime());
    }

    public static String getCurrentCycle() {
        return currentCycleId.get();
    }

    public static Long getObservationStartTime() {
        return observationStartTime.get();
    }

    public static void clear() {
        currentCycleId.remove();
        observationStartTime.remove();
    }

    public static long getElapsedTime() {
        Long start = observationStartTime.get();
        return start != null ? System.nanoTime() - start : 0;
    }
}