package tools.spirals.cerberus237.adaptiflow.subscriptions.subscribers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.spirals.cerberus237.adaptiflow.interfaces.ConditionEvaluator;
import tools.spirals.cerberus237.adaptiflow.interfaces.IAdaptationAction;
import tools.spirals.cerberus237.adaptiflow.operators.TrueEvaluator;

import java.util.List;

/**
 * The {@link EventSubscriber} class is a concrete implementation of the
 * {@link AbstractEventSubscriber} that reacts to updates from observable objects.
 * Enhanced with performance logging for action execution.
 */
public class EventSubscriber<T> extends AbstractEventSubscriber<T> {
    private static final Logger LOG = LoggerFactory.getLogger(EventSubscriber.class);

    private String subscriberId;
    private String serviceName;

    public EventSubscriber(List<IAdaptationAction> actions, ConditionEvaluator<T> conditionEvaluator) {
        super(actions, conditionEvaluator);
        this.subscriberId = generateSubscriberId();
    }

    public EventSubscriber(List<IAdaptationAction> actions) {
        super(actions, new TrueEvaluator<>());
        this.subscriberId = generateSubscriberId();
    }

    public EventSubscriber(String serviceName, List<IAdaptationAction> actions) {
        super(actions, new TrueEvaluator<>());
        this.serviceName = serviceName;
        this.subscriberId = generateSubscriberId();
    }

    public EventSubscriber(String serviceName, List<IAdaptationAction> actions,
                           ConditionEvaluator<T> conditionEvaluator) {
        super(actions, conditionEvaluator);
        this.serviceName = serviceName;
        this.subscriberId = generateSubscriberId();
    }

    public void update(T metricValue, String message) { update(metricValue, message, null); }

    @Override
    public void update(T metricValue, String message, String cycleId) {
        long updateStartTime = System.nanoTime();
        if (cycleId == null) cycleId = "unknown-cycle";

        // Existing: SUBSCRIBER_UPDATE_START
        LOG.info("SUBSCRIBER_UPDATE_START|cycleId={}|subscriberId={}|service={}|message={}|actionsCount={}|startTime={}",
                cycleId, subscriberId, serviceName, message, actions.size(), updateStartTime);


        // Optional context (does not affect parser)
        LOG.info("SUBSCRIBER_METRIC_RECEIVED|cycleId={}|subscriberId={}|service={}|metric={}",
                cycleId, subscriberId, serviceName, metricValue);

        int successfulActions = 0;
        int failedActions = 0;

        for (int i = 0; i < actions.size(); i++) {
            IAdaptationAction action = actions.get(i);
            long actionStartTime = System.nanoTime();

            // NEW (safe): action marker for chart annotation
            LOG.info("ACTION_MARKER|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|marker=ACTION_START|timestamp={}",
                    cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionStartTime);

            try {
                // Existing: ACTION_EXECUTION_START (not parsed by your script but harmless)
                LOG.info("ACTION_EXECUTION_START|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|startTime={}",
                        cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionStartTime);

                action.perform();

                long actionEndTime = System.nanoTime();
                long actionLatency = actionEndTime - actionStartTime;
                successfulActions++;

                // Existing: ACTION_EXECUTION_COMPLETE
                LOG.info("ACTION_EXECUTION_COMPLETE|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|latencyNs={}|endTime={}|metric={}",
                        cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionLatency, actionEndTime, metricValue);

                // NEW (safe): action marker end
                LOG.info("ACTION_MARKER|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|marker=ACTION_END|timestamp={}",
                        cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionEndTime);

            } catch (Exception e) {
                long actionEndTime = System.nanoTime();
                long actionLatency = actionEndTime - actionStartTime;
                failedActions++;

                LOG.error("ACTION_EXECUTION_FAILED|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|latencyNs={}|error={}",
                        cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionLatency, e.getMessage(), e);

                // NEW (safe): failed action marker
                LOG.info("ACTION_MARKER|cycleId={}|subscriberId={}|service={}|actionIndex={}|actionClass={}|marker=ACTION_FAILED|timestamp={}",
                        cycleId, subscriberId, serviceName, i, action.getClass().getSimpleName(), actionEndTime);
            }
        }

        long updateEndTime = System.nanoTime();
        long totalUpdateLatency = updateEndTime - updateStartTime;

        // Existing: SUBSCRIBER_UPDATE_COMPLETE
        LOG.info("SUBSCRIBER_UPDATE_COMPLETE|cycleId={}|subscriberId={}|service={}|successfulActions={}|failedActions={}|totalLatencyNs={}|endTime={}",
                cycleId, subscriberId, serviceName, successfulActions, failedActions, totalUpdateLatency, updateEndTime);

    }

    private String generateSubscriberId() { return "subscriber-" + System.currentTimeMillis() + "-" + hashCode(); }

    public String getSubscriberId() { return subscriberId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
}