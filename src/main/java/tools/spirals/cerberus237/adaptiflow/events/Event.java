package tools.spirals.cerberus237.adaptiflow.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.spirals.cerberus237.adaptiflow.context.AdaptationCycleContext;
import tools.spirals.cerberus237.adaptiflow.interfaces.Observable;
import tools.spirals.cerberus237.adaptiflow.interfaces.Observer;
import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link Event} class represents an observable event that can have multiple observers
 * subscribed to it. It collects metrics using a specified metrics collector and notifies
 * its observers when those metrics change and meet certain conditions.
 * <p>
 * This class implements the {@code Observable} interface, allowing observers to subscribe
 * or unsubscribe and receive notifications based on metric values collected.
 * </p>
 *
 * @param <T> the type of data that this event will provide to its observers.
 * @author Arléon Zemtsop (Cerberus)
 */
public class Event<T> implements Observable<T> {

    private static final Logger LOG = LoggerFactory.getLogger(Event.class);

    protected String name;
    protected String serviceName;

    protected final List<Observer<T>> subscribers = new ArrayList<>();
    protected final IMetricsCollector<T> collector;

    public Event(IMetricsCollector<T> collector) {
        this.collector = collector;
    }

    public Event(String name, IMetricsCollector<T> collector) {
        this.name = name;
        this.collector = collector;
    }

    public Event(String serviceName, String name, IMetricsCollector<T> collector) {
        this.serviceName = serviceName;
        this.name = name;
        this.collector = collector;
    }

    @Override
    public void subscribe(Observer<T> subscriber) { subscribers.add(subscriber); }

    @Override
    public void unsubscribe(Observer<T> subscriber) { subscribers.remove(subscriber); }

    @Override
    public void subscribeAll(List<Observer<T>> subs) { subs.forEach(this::subscribe); }

    @Override
    public void unsubscribeAll(List<Observer<T>> subs) { subs.forEach(this::unsubscribe); }

    /**
     * Observe metric changes and notify observers if their conditions are met.
     * Keeps existing log formats + adds harmless phase markers.
     */
    public void observe() {
        String cycleId = AdaptationCycleContext.generateCycleId(serviceName != null ? serviceName : "unknown-service");
        AdaptationCycleContext.setCurrentCycle(cycleId);

        try {
            long collectionStartTime = System.nanoTime();

            // Existing: ADAPTATION_CYCLE_START (kept for compatibility)
            LOG.info("ADAPTATION_CYCLE_START|cycleId={}|event={}|service={}|timestamp={}",
                    cycleId, name, serviceName, collectionStartTime);

            // Detection / collection
            T metric = collector.get();
            long collectionEndTime = System.nanoTime();
            long collectionLatency = collectionEndTime - collectionStartTime;

            // Existing: METRICS_COLLECTION_COMPLETE
            LOG.info("METRICS_COLLECTION_COMPLETE|cycleId={}|event={}|service={}|startTime={}|endTime={}|latencyNs={}",
                    cycleId, name, serviceName, collectionStartTime, collectionEndTime, collectionLatency);


            int notifiedSubscribers = 0;

            for (Observer<T> observer : subscribers) {
                long evaluationStartTime = System.nanoTime();
                boolean conditionMet = observer.getConditionEvaluator().test(metric);
                long evaluationEndTime = System.nanoTime();
                long evaluationLatency = evaluationEndTime - evaluationStartTime;

                // Existing: CONDITION_EVALUATION
                LOG.info("CONDITION_EVALUATION|cycleId={}|event={}|service={}|observer={}|conditionMet={}|startTime={}|endTime={}|latencyNs={}",
                        cycleId, name, serviceName, observer.getClass().getSimpleName(), conditionMet,
                        evaluationStartTime, evaluationEndTime, evaluationLatency);

                if (conditionMet) {

                    long notificationStartTime = System.nanoTime();
                    notifyObserver(observer, metric, cycleId);

                    notifiedSubscribers++;
                }
            }



            long cycleEndTime = System.nanoTime();
            long totalCycleLatency = cycleEndTime - collectionStartTime;

            // Existing: ADAPTATION_CYCLE_COMPLETE
            LOG.info("ADAPTATION_CYCLE_COMPLETE|cycleId={}|event={}|service={}|subscribersNotified={}|totalLatencyNs={}|endTime={}",
                    cycleId, name, serviceName, notifiedSubscribers, totalCycleLatency, cycleEndTime);

        } finally {
            AdaptationCycleContext.clear();
        }
    }

    @Override
    public void notifyObservers(T metricValue) {
        String cycleId = AdaptationCycleContext.getCurrentCycle();
        long startTime = System.nanoTime();
        for (Observer<T> subscriber : subscribers) {
            notifyObserver(subscriber, metricValue, cycleId);
        }
        long endTime = System.nanoTime();
        LOG.info("BULK_NOTIFICATION_COMPLETE|cycleId={}|event={}|service={}|subscribersCount={}|latencyNs={}",
                cycleId, name, serviceName, subscribers.size(), endTime - startTime);
    }

    @Override
    public void notifyObserver(Observer<T> observer, T metricValue) {
        notifyObserver(observer, metricValue, AdaptationCycleContext.getCurrentCycle());
    }

    @Override
    public void notifyObserver(Observer<T> observer, T metricValue, String cycleId, long notificationStartTime) {
        long notificationEndTime = System.nanoTime();
        long notificationLatency = notificationEndTime - notificationStartTime;
        LOG.info("CONDITIONAL_OBSERVER_NOTIFICATION|cycleId={}|event={}|service={}|observer={}|startTime={}|endTime={}|latencyNs={}", cycleId, name, serviceName, observer.getClass().getSimpleName(), notificationStartTime, notificationEndTime, notificationLatency);
        long startTime = System.nanoTime();
        observer.update(metricValue, "Handling " + this.name + " event", cycleId);
        long endTime = System.nanoTime();

        LOG.info("OBSERVER_UPDATE_COMPLETE|cycleId={}|event={}|service={}|observer={}|latencyNs={}",
                cycleId, name, serviceName, observer.getClass().getSimpleName(), endTime - startTime);
    }

    public void notifyObserver(Observer<T> observer, T metricValue, String cycleId) {
        long startTime = System.nanoTime();
        observer.update(metricValue, "Handling " + this.name + " event", cycleId);
        long endTime = System.nanoTime();

        LOG.info("OBSERVER_UPDATE_COMPLETE|cycleId={}|event={}|service={}|observer={}|latencyNs={}",
                cycleId, name, serviceName, observer.getClass().getSimpleName(), endTime - startTime);
    }

    public List<Observer<T>> getSubscribers() { return subscribers; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
}