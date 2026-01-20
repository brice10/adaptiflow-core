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

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import tools.spirals.cerberus237.adaptiflow.interfaces.ConditionEvaluator;
import tools.spirals.cerberus237.adaptiflow.interfaces.Observer;
import tools.spirals.cerberus237.adaptiflow.operators.GreaterThanEvaluator;
import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class EventTest {
    private TestMetricsCollector collector;
    private Event<Double> event;
    private TestObserver<Double> observer;

    private static class TestMetricsCollector implements IMetricsCollector<Double> {
        private double value;

        public TestMetricsCollector(double value) {
            this.value = value;
        }

        @Override
        public Double get() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    private static class TestObserver<T extends Comparable<T>> implements Observer<T> {
        private final List<T> notifiedValues = new ArrayList<>();

        @Override
        public void update(T metricValue, String message) {
            notifiedValues.add(metricValue);
        }

        @Override
        public ConditionEvaluator<T> getConditionEvaluator() {
            return (ConditionEvaluator<T>) new GreaterThanEvaluator<>(50.0);
        }

        public List<T> getNotifiedValues() {
            return notifiedValues;
        }
    }

    @Before
    public void setUp() {
        collector = new TestMetricsCollector(50.0);
        event = new Event<>(collector);
        observer = new TestObserver<>();
        event.subscribe(observer);
    }

    @Test
    public void testSubscribe() {
        Assert.assertEquals(1, event.getSubscribers().size());
        Assert.assertTrue(event.getSubscribers().contains(observer));
    }

    @Test
    public void testUnsubscribe() {
        event.unsubscribe(observer);
        Assert.assertTrue(event.getSubscribers().isEmpty());
    }

    @Test
    public void testNotifyObservers() {
        collector.setValue(60.0);
        event.observe(); // Should notify the observer

        Assert.assertEquals(1, observer.getNotifiedValues().size());
        Assert.assertEquals(60.0, observer.getNotifiedValues().get(0), 0.01);
    }

    @Test
    public void testNoNotificationWhenConditionFails() {
        collector.setValue(40.0); // Assuming the condition fails
        event.observe(); // Should NOT notify the observer

        Assert.assertTrue(observer.getNotifiedValues().isEmpty());
    }
}