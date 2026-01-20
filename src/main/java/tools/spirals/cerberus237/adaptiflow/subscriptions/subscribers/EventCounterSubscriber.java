package tools.spirals.cerberus237.adaptiflow.subscriptions.subscribers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.spirals.cerberus237.adaptiflow.interfaces.ConditionEvaluator;
import tools.spirals.cerberus237.adaptiflow.interfaces.IAdaptationAction;
import tools.spirals.cerberus237.adaptiflow.operators.TrueEvaluator;

import java.util.List;

/**
 * The {@link EventCounterSubscriber} class is a concrete implementation of the
 * {@link AbstractEventSubscriber} that triggers adaptation actions based on a
 * specified counting cycle.
 * Enhanced with performance logging and cycle counting metrics.
 */
public class EventCounterSubscriber<T> extends AbstractEventSubscriber<T> {
    private static final Logger LOG = LoggerFactory.getLogger(EventCounterSubscriber.class);

    private final int cycle;
    private int counter = 0;
    private String subscriberId;
    private String serviceName;

    public EventCounterSubscriber(List<IAdaptationAction> actions, ConditionEvaluator<T> conditionEvaluator, int cycle) {
        super(actions, conditionEvaluator);
        this.cycle = cycle;
        this.subscriberId = generateSubscriberId();
    }

    public EventCounterSubscriber(List<IAdaptationAction> actions, int cycle) {
        super(actions, new TrueEvaluator<>());
        this.cycle = cycle;
        this.subscriberId = generateSubscriberId();
    }

    public EventCounterSubscriber(String serviceName, List<IAdaptationAction> actions,
                                  ConditionEvaluator<T> conditionEvaluator, int cycle) {
        super(actions, conditionEvaluator);
        this.serviceName = serviceName;
        this.cycle = cycle;
        this.subscriberId = generateSubscriberId();
    }

    public void update(T metricValue, String message) { update(metricValue, message, null); }

    @Override
    public void update(T metricValue, String message, String cycleId) {
        long updateStartTime = System.nanoTime();
        counter++;
        if (cycleId == null) cycleId = "unknown-cycle";

        // Existing: COUNTER_SUBSCRIBER_UPDATE
        LOG.info("COUNTER_SUBSCRIBER_UPDATE|cycleId={}|subscriberId={}|service={}|currentCount={}|targetCount={}|message={}|startTime={}",
                cycleId, subscriberId, serviceName, counter, cycle, message, updateStartTime);

        if (counter >= cycle) {
            LOG.info("CYCLE_THRESHOLD_REACHED|cycleId={}|subscriberId={}|service={}|count={}|startTime={}",
                    cycleId, subscriberId, serviceName, counter, System.nanoTime());

            int successfulActions = 0;
            int failedActions = 0;

            for (int i = 0; i < actions.size(); i++) {
                IAdaptationAction action = actions.get(i);
                long actionStartTime = System.nanoTime();

                // NEW (safe): action marker
                LOG.info("ACTION_MARKER|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|marker=ACTION_START|timestamp={}",
                        cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionStartTime);

                try {
                    LOG.info("COUNTER_ACTION_EXECUTION_START|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|startTime={}",
                            cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionStartTime);

                    action.perform();

                    long actionEndTime = System.nanoTime();
                    long actionLatency = actionEndTime - actionStartTime;
                    successfulActions++;

                    // Existing: COUNTER_ACTION_EXECUTION_COMPLETE
                    LOG.info("COUNTER_ACTION_EXECUTION_COMPLETE|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|latencyNs={}|metric={}",
                            cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionLatency, metricValue);

                    // NEW (safe): action end marker
                    LOG.info("ACTION_MARKER|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|marker=ACTION_END|timestamp={}",
                            cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionEndTime);

                } catch (Exception e) {
                    long actionEndTime = System.nanoTime();
                    long actionLatency = actionEndTime - actionStartTime;
                    failedActions++;

                    LOG.error("COUNTER_ACTION_EXECUTION_FAILED|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|latencyNs={}|error={}",
                            cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionLatency, e.getMessage(), e);

                    LOG.info("ACTION_MARKER|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|marker=ACTION_FAILED|timestamp={}",
                            cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionEndTime);
                }
            }

            counter = 0; // reset
            long updateEndTime = System.nanoTime();
            long totalUpdateLatency = updateEndTime - updateStartTime;

            // Existing: COUNTER_SUBSCRIBER_CYCLE_COMPLETE
            LOG.info("COUNTER_SUBSCRIBER_CYCLE_COMPLETE|cycleId={}|subscriberId={}|service={}|successfulActions={}|failedActions={}|totalLatencyNs={}",
                    cycleId, subscriberId, serviceName, successfulActions, failedActions, totalUpdateLatency);

        } else {
            // Existing: CYCLE_THRESHOLD_NOT_REACHED
            LOG.info("CYCLE_THRESHOLD_NOT_REACHED|cycleId={}|subscriberId={}|service={}|currentCount={}|targetCount={}",
                    cycleId, subscriberId, serviceName, counter, cycle);
        }
    }

    public int getCounter() { return counter; }

    private String generateSubscriberId() { return "counter-subscriber-" + System.currentTimeMillis() + "-" + hashCode(); }

    public String getSubscriberId() { return subscriberId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
}