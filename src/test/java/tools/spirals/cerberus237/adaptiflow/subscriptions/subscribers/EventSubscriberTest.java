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
import org.junit.Test;
import tools.spirals.cerberus237.adaptiflow.interfaces.ConditionEvaluator;
import tools.spirals.cerberus237.adaptiflow.scanner.ExampleAction;
import tools.spirals.cerberus237.adaptationactionsbase.core.IAdaptationAction;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;

/**
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class EventSubscriberTest {
    private EventSubscriber<Double> subscriber;
    private List<IAdaptationAction> actions;
    private TestConditionEvaluator conditionEvaluator;

    private static class TestConditionEvaluator implements ConditionEvaluator<Double> {
        @Override
        public boolean test(Double metric) {
            return true; // Always true for testing
        }
    }

    @Before
    public void setUp() {
        actions = new ArrayList<>();
        actions.add(new ExampleAction(() -> System.out.println("Action performed!"))); // Test action
        conditionEvaluator = new TestConditionEvaluator();
        subscriber = new EventSubscriber<>(actions, conditionEvaluator);
    }

    @Test
    public void testUpdateCallsActions() {
        // Redirect System.out to capture the output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        String message = "Test Message";
        subscriber.update(42.0, message);

        // Check that the action was performed
        String expectedOutput = "Action performed!\n"; // Assuming a newline character is printed
        Assert.assertTrue(outContent.toString().contains(expectedOutput));

        // Restore original System.out
        System.setOut(originalOut);
    }

    @Test
    public void testConditionEvaluator() {
        Assert.assertEquals(conditionEvaluator, subscriber.getConditionEvaluator());
    }
}