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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.spirals.cerberus237.adaptiflow.interfaces.ConditionEvaluator;
import tools.spirals.cerberus237.adaptationactionsbase.core.IAdaptationAction;
import tools.spirals.cerberus237.adaptiflow.operators.TrueEvaluator;

import java.util.List;

/**
 * The {@link EventCounterSubscriber} class is a concrete implementation of the
 * {@link AbstractEventSubscriber} that triggers adaptation actions based on a
 * specified counting cycle.
 * <p>
 * This class counts the number of updates received and performs the actions
 * only when the count reaches a predefined cycle.
 * </p>
 *
 * @param <T> the type of data that this subscriber will work with.
 * @author Arléon Zemtsop (Cerberus)
 */
public class EventCounterSubscriber<T> extends AbstractEventSubscriber<T> {
    private static final Logger LOG = LoggerFactory.getLogger(EventCounterSubscriber.class);

    /**
     * The cycle count after which actions will be performed.
     */
    private final int cycle;

    /**
     * The current count of received updates.
     */
    private int counter = 0;

    /**
     * Constructs an {@code EventCounterSubscriber} with the specified actions,
     * condition evaluator, and cycle count.
     *
     * @param actions a list of adaptation actions to be executed.
     * @param conditionEvaluator the condition evaluator that determines when
     *                           the actions should be performed.
     * @param cycle the number of updates to count before performing actions.
     */
    public EventCounterSubscriber(List<IAdaptationAction> actions,
                                  ConditionEvaluator<T> conditionEvaluator,
                                  int cycle) {
        super(actions, conditionEvaluator);
        this.cycle = cycle;
    }

    /**
     * Constructs an {@code EventCounterSubscriber} with the specified actions
     * and a default condition evaluator that always returns true.
     *
     * @param actions a list of adaptation actions to be executed.
     */
    public EventCounterSubscriber(List<IAdaptationAction> actions, int cycle) {
        super(actions, new TrueEvaluator<>());
        this.cycle = cycle;
    }

    /**
     * Updates the subscriber with a new metric value and a message.
     * <p>
     * This method increments the counter and checks if it has reached the cycle limit.
     * If so, it prints the message, performs the adaptation actions, and resets the counter.
     * </p>
     *
     * @param metricValue the new metric value to be evaluated.
     * @param message a message indicating the context of the update.
     */
    @Override
    public void update(T metricValue, String message) {
        counter++;
        if (counter >= cycle) {
            LOG.info("{}: {}", message, metricValue);
            actions.forEach(IAdaptationAction::perform);
            counter = 0; // Reset the counter after actions are performed
        }
    }

    /**
     * Retrieves the current counter value.
     *
     * @return the current count of updates received since the last action was performed.
     */
    public int getCounter() {
        return counter;
    }
}