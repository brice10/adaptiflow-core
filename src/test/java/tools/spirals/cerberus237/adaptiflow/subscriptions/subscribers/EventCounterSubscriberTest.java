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
package tools.spirals.cerberus237.adaptiflow.subscriptions.subscribers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tools.spirals.cerberus237.adaptationactionsbase.core.IAdaptationAction;
import tools.spirals.cerberus237.adaptiflow.operators.TrueEvaluator;
import tools.spirals.cerberus237.adaptiflow.scanner.ExampleAction;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class EventCounterSubscriberTest {
    private EventCounterSubscriber<Double> counterSubscriber;

    @Before
    public void setUp() {
        List<IAdaptationAction> actions = new ArrayList<>();
        actions.add(new ExampleAction(() -> System.out.println("Action performed!"))); // Test action
        counterSubscriber = new EventCounterSubscriber<Double>(actions, new TrueEvaluator<>(), 3);
    }

    @Test
    public void testUpdateIncrementsCounter() {
        counterSubscriber.update(42.0, "First");
        Assert.assertEquals(1, counterSubscriber.getCounter());
    }

    @Test
    public void testUpdateDoesNotTriggerActionBeforeCycle() {
        counterSubscriber.update(42.0, "First");
        counterSubscriber.update(43.0, "Second");
        Assert.assertEquals(2, counterSubscriber.getCounter());
    }

    @Test
    public void testUpdateTriggersActionOnCycle() {
        counterSubscriber.update(42.0, "First");
        counterSubscriber.update(43.0, "Second");
        counterSubscriber.update(44.0, "Third"); // This should trigger action

        Assert.assertEquals(0, counterSubscriber.getCounter()); // Counter should reset
    }
}