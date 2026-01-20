/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.spirals.cerberus237.adaptiflow.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.spirals.cerberus237.adaptiflow.context.AdaptationCycleContext;
import tools.spirals.cerberus237.adaptiflow.interfaces.ConditionEvaluator;
import tools.spirals.cerberus237.adaptiflow.interfaces.Observer;
import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;

/**
 * The {@link ConditionalEvent} class extends the {@link Event} class and adds
 * additional functionality for evaluating conditions before notifying observers.
 * <p>
 * This class allows the event to have a specific condition evaluator that must be
 * satisfied before notifying each observer, in addition to the observer's own condition.
 * </p>
 *
 * @param <T> the type of data that this event will provide to its observers.
 * @author Arléon Zemtsop (Cerberus)
 */
public class ConditionalEvent<T> extends Event<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ConditionalEvent.class);
    protected ConditionEvaluator<T> conditionEvaluator;

    public ConditionalEvent(IMetricsCollector<T> collector, ConditionEvaluator<T> conditionEvaluator) {
        super(collector);
        this.conditionEvaluator = conditionEvaluator;
    }

    public ConditionalEvent(String name, IMetricsCollector<T> collector, ConditionEvaluator<T> conditionEvaluator) {
        super(name, collector);
        this.conditionEvaluator = conditionEvaluator;
    }

    public ConditionalEvent(String name, String serviceName, IMetricsCollector<T> collector, ConditionEvaluator<T> conditionEvaluator) {
        super(name, collector);
        this.serviceName = serviceName;
        this.conditionEvaluator = conditionEvaluator;
    }

    @Override
    public void observe() {
        String cycleId = AdaptationCycleContext.generateCycleId(serviceName != null ? serviceName : "unknown-service");
        AdaptationCycleContext.setCurrentCycle(cycleId);

        try {
            long collectionStartTime = System.nanoTime();

            // Existing: CONDITIONAL_EVENT_CYCLE_START
            LOG.info("CONDITIONAL_EVENT_CYCLE_START|cycleId={}|event={}|service={}|timestamp={}|evaluatorClass={}",
                    cycleId, name, serviceName, collectionStartTime, conditionEvaluator.getClass().getSimpleName());

            T metric = collector.get();
            long collectionEndTime = System.nanoTime();
            long collectionLatency = collectionEndTime - collectionStartTime;

            // Existing: CONDITIONAL_METRICS_COLLECTION_COMPLETE
            LOG.info("CONDITIONAL_METRICS_COLLECTION_COMPLETE|cycleId={}|event={}|service={}|startTime={}|endTime={}|latencyNs={}",
                    cycleId, name, serviceName, collectionStartTime, collectionEndTime, collectionLatency);

            // Evaluation (event-level)
            long eventConditionStartTime = System.nanoTime();

            // Existing: EVENT_CONDITION_EVALUATION
            boolean eventConditionMet = conditionEvaluator.test(metric);
            long eventConditionEndTime = System.nanoTime();
            long eventConditionLatency = eventConditionEndTime - eventConditionStartTime;

            LOG.info("EVENT_CONDITION_EVALUATION|cycleId={}|event={}|service={}|evaluatorClass={}|conditionMet={}|startTime={}|endTime={}|latencyNs={}",
                    cycleId, name, serviceName, conditionEvaluator.getClass().getSimpleName(), eventConditionMet,
                    eventConditionStartTime, eventConditionEndTime, eventConditionLatency);

            int eventConditionPassedObservers = 0;
            int totalNotifiedSubscribers = 0;

            if (eventConditionMet) {
                // Existing: EVENT_CONDITION_PASSED
                LOG.info("EVENT_CONDITION_PASSED|cycleId={}|event={}|service={}|proceedingToObserverEvaluation={}",
                        cycleId, name, serviceName, subscribers.size());

                for (Observer<T> observer : subscribers) {
                    long observerConditionStartTime = System.nanoTime();
                    boolean observerConditionMet = observer.getConditionEvaluator().test(metric);
                    long observerConditionEndTime = System.nanoTime();
                    long observerConditionLatency = observerConditionEndTime - observerConditionStartTime;

                    // Existing: OBSERVER_CONDITION_EVALUATION
                    LOG.info("OBSERVER_CONDITION_EVALUATION|cycleId={}|event={}|service={}|observer={}|observerEvaluatorClass={}|conditionMet={}|startTime={}|endTime={}|latencyNs={}",
                            cycleId, name, serviceName, observer.getClass().getSimpleName(),
                            observer.getConditionEvaluator().getClass().getSimpleName(), observerConditionMet,
                            observerConditionStartTime, observerConditionEndTime, observerConditionLatency);

                    if (observerConditionMet) {
                        eventConditionPassedObservers++;

                        long notificationStartTime = System.nanoTime();
                        notifyObserver(observer, metric, cycleId, notificationStartTime);

                        totalNotifiedSubscribers++;
                    } else {
                        LOG.info("OBSERVER_CONDITION_FAILED|cycleId={}|event={}|service={}|observer={}|skippingNotification=true",
                                cycleId, name, serviceName, observer.getClass().getSimpleName());
                    }
                }
            } else {
                // Existing: EVENT_CONDITION_FAILED
                LOG.info("EVENT_CONDITION_FAILED|cycleId={}|event={}|service={}|skippingAllObservers=true|totalObserversSkipped={}",
                        cycleId, name, serviceName, subscribers.size());
            }

            long cycleEndTime = System.nanoTime();
            long totalCycleLatency = cycleEndTime - collectionStartTime;

            // Existing: CONDITIONAL_EVENT_CYCLE_COMPLETE
            LOG.info("CONDITIONAL_EVENT_CYCLE_COMPLETE|cycleId={}|event={}|service={}|eventConditionMet={}|observersWithEventConditionPassed={}|subscribersNotified={}|totalLatencyNs={}|endTime={}",
                    cycleId, name, serviceName, eventConditionMet, eventConditionPassedObservers,
                    totalNotifiedSubscribers, totalCycleLatency, cycleEndTime);

        } finally {
            AdaptationCycleContext.clear();
        }
    }

    @Override
    public void notifyObservers(T metricValue) {
        String cycleId = AdaptationCycleContext.getCurrentCycle();

        long eventConditionStartTime = System.nanoTime();
        boolean eventConditionMet = conditionEvaluator.test(metricValue);
        long eventConditionEndTime = System.nanoTime();

        LOG.info("BULK_NOTIFICATION_CONDITION_CHECK|cycleId={}|event={}|service={}|conditionMet={}|latencyNs={}",
                cycleId, name, serviceName, eventConditionMet, eventConditionEndTime - eventConditionStartTime);

        if (!eventConditionMet) {
            LOG.info("BULK_NOTIFICATION_SKIPPED|cycleId={}|event={}|service={}|reason=eventConditionNotMet|subscribersCount={}",
                    cycleId, name, serviceName, subscribers.size());
            return;
        }

        long startTime = System.nanoTime();
        int notifiedCount = 0;

        for (Observer<T> subscriber : subscribers) {
            long observerConditionStartTime = System.nanoTime();
            boolean observerConditionMet = subscriber.getConditionEvaluator().test(metricValue);
            long observerConditionEndTime = System.nanoTime();

            if (observerConditionMet) {
                notifyObserver(subscriber, metricValue, cycleId);
                notifiedCount++;

                LOG.info("BULK_OBSERVER_CONDITION_PASSED|cycleId={}|event={}|service={}|observer={}|conditionLatencyNs={}",
                        cycleId, name, serviceName, subscriber.getClass().getSimpleName(),
                        observerConditionEndTime - observerConditionStartTime);
            } else {
                LOG.info("BULK_OBSERVER_CONDITION_FAILED|cycleId={}|event={}|service={}|observer={}|conditionLatencyNs={}",
                        cycleId, name, serviceName, subscriber.getClass().getSimpleName(),
                        observerConditionEndTime - observerConditionStartTime);
            }
        }

        long endTime = System.nanoTime();
        LOG.info("CONDITIONAL_BULK_NOTIFICATION_COMPLETE|cycleId={}|event={}|service={}|subscribersCount={}|notifiedCount={}|latencyNs={}",
                cycleId, name, serviceName, subscribers.size(), notifiedCount, endTime - startTime);
    }

    @Override
    public void notifyObserver(Observer<T> observer, T metricValue, String cycleId) {
        long preCheckStartTime = System.nanoTime();
        boolean eventConditionMet = conditionEvaluator.test(metricValue);
        boolean observerConditionMet = observer.getConditionEvaluator().test(metricValue);
        long preCheckEndTime = System.nanoTime();

        LOG.info("FINAL_CONDITION_VERIFICATION|cycleId={}|event={}|service={}|observer={}|eventConditionMet={}|observerConditionMet={}|verificationLatencyNs={}",
                cycleId, name, serviceName, observer.getClass().getSimpleName(),
                eventConditionMet, observerConditionMet, preCheckEndTime - preCheckStartTime);

        if (eventConditionMet && observerConditionMet) {
            long startTime = System.nanoTime();
            observer.update(metricValue, "Handling " + this.name + " conditional event", cycleId);
            long endTime = System.nanoTime();

            // Existing: CONDITIONAL_OBSERVER_UPDATE_COMPLETE
            LOG.info("CONDITIONAL_OBSERVER_UPDATE_COMPLETE|cycleId={}|event={}|service={}|observer={}|latencyNs={}",
                    cycleId, name, serviceName, observer.getClass().getSimpleName(), endTime - startTime);
        } else {
            LOG.warn("CONDITIONAL_OBSERVER_UPDATE_SKIPPED|cycleId={}|event={}|service={}|observer={}|reason=conditionsNotMet|eventCondition={}|observerCondition={}",
                    cycleId, name, serviceName, observer.getClass().getSimpleName(), eventConditionMet, observerConditionMet);
        }
    }

    @Override
    public void notifyObserver(Observer<T> observer, T metricValue, String cycleId, long notificationStartTime) {
        long notificationEndTime = System.nanoTime();
        long notificationLatency = notificationEndTime - notificationStartTime;

        // Existing: CONDITIONAL_OBSERVER_NOTIFICATION (kept in caller)
        LOG.info("CONDITIONAL_OBSERVER_NOTIFICATION|cycleId={}|event={}|service={}|observer={}|startTime={}|endTime={}|latencyNs={}",
                cycleId, name, serviceName, observer.getClass().getSimpleName(),
                notificationStartTime, notificationEndTime, notificationLatency);

        long preCheckStartTime = System.nanoTime();
        boolean eventConditionMet = conditionEvaluator.test(metricValue);
        boolean observerConditionMet = observer.getConditionEvaluator().test(metricValue);
        long preCheckEndTime = System.nanoTime();

        LOG.info("FINAL_CONDITION_VERIFICATION|cycleId={}|event={}|service={}|observer={}|eventConditionMet={}|observerConditionMet={}|verificationLatencyNs={}",
                cycleId, name, serviceName, observer.getClass().getSimpleName(),
                eventConditionMet, observerConditionMet, preCheckEndTime - preCheckStartTime);

        if (eventConditionMet && observerConditionMet) {
            long startTime = System.nanoTime();
            observer.update(metricValue, "Handling " + this.name + " conditional event", cycleId);
            long endTime = System.nanoTime();

            LOG.info("CONDITIONAL_OBSERVER_UPDATE_COMPLETE|cycleId={}|event={}|service={}|observer={}|latencyNs={}",
                    cycleId, name, serviceName, observer.getClass().getSimpleName(), endTime - startTime);
        } else {
            LOG.warn("CONDITIONAL_OBSERVER_UPDATE_SKIPPED|cycleId={}|event={}|service={}|observer={}|reason=conditionsNotMet|eventCondition={}|observerCondition={}",
                    cycleId, name, serviceName, observer.getClass().getSimpleName(), eventConditionMet, observerConditionMet);
        }
    }

    public ConditionEvaluator<T> getConditionEvaluator() { return conditionEvaluator; }
    public void setConditionEvaluator(ConditionEvaluator<T> ce) { this.conditionEvaluator = ce; }
}