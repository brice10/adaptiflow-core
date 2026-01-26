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
 * The {@link EventSubscriber} class is a concrete implementation of the
 * {@link AbstractEventSubscriber} that reacts to updates from observable objects.
 * <p>
 * This class utilizes a list of adaptation actions and a condition evaluator
 * to determine when to perform actions based on the received metrics.
 * </p>
 *
 * @param <T> the type of data that this subscriber will work with.
 * @author Arléon Zemtsop (Cerberus)
 */
public class EventSubscriber<T> extends AbstractEventSubscriber<T> {
    private static final Logger LOG = LoggerFactory.getLogger(EventSubscriber.class);

    /**
     * Constructs an {@code EventSubscriber} with the specified actions
     * and condition evaluator.
     *
     * @param actions a list of adaptation actions to be executed.
     * @param conditionEvaluator the condition evaluator that determines when
     *                           the actions should be performed.
     */
    public EventSubscriber(List<IAdaptationAction> actions,
                           ConditionEvaluator<T> conditionEvaluator) {
        super(actions, conditionEvaluator);
        this.conditionEvaluator = conditionEvaluator;
    }

    /**
     * Constructs an {@code EventSubscriber} with the specified actions
     * and a default condition evaluator that always returns true.
     *
     * @param actions a list of adaptation actions to be executed.
     */
    public EventSubscriber(List<IAdaptationAction> actions) {
        super(actions, new TrueEvaluator<>());
    }

    /**
     * Updates the subscriber with a new metric value and a message.
     * <p>
     * This method prints the message and the metric value, and then
     * performs all the adaptation actions if the conditions are met.
     * </p>
     *
     * @param metricValue the new metric value to be evaluated.
     * @param message a message indicating the context of the update.
     */
    @Override
    public void update(T metricValue, String message) {
        LOG.info("{}: {}", message, metricValue);
        actions.forEach(IAdaptationAction::perform);
    }
}