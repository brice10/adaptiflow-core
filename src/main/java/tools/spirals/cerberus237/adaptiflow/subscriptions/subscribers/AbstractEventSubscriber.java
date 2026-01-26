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

import tools.spirals.cerberus237.adaptiflow.interfaces.ConditionEvaluator;
import tools.spirals.cerberus237.adaptationactionsbase.core.IAdaptationAction;
import tools.spirals.cerberus237.adaptiflow.interfaces.Observer;

import java.util.List;

/**
 * The {@link AbstractEventSubscriber} class provides a base implementation for
 * event subscribers that react to changes in observable objects.
 * <p>
 * This abstract class holds a list of adaptation actions and a condition
 * evaluator to determine when those actions should be performed.
 * </p>
 *
 * @param <T> the type of data that this subscriber will work with.
 * @author Arléon Zemtsop (Cerberus)
 */
public abstract class AbstractEventSubscriber<T> implements Observer<T> {

    /**
     * A list of actions to be performed when the conditions are met.
     */
    protected final List<IAdaptationAction> actions;

    /**
     * The condition evaluator used to determine when to perform actions.
     */
    protected ConditionEvaluator<T> conditionEvaluator;

    /**
     * Constructs an {@code AbstractEventSubscriber} with the specified
     * actions and a condition evaluator.
     *
     * @param actions a list of adaptation actions to be executed.
     * @param conditionEvaluator the condition evaluator that determines when
     *                           the actions should be performed.
     */
    public AbstractEventSubscriber(List<IAdaptationAction> actions,
                                   ConditionEvaluator<T> conditionEvaluator) {
        this.actions = actions;
        this.conditionEvaluator = conditionEvaluator;
    }

    /**
     * Retrieves the condition evaluator associated with this subscriber.
     *
     * @return the current condition evaluator.
     */
    public ConditionEvaluator<T> getConditionEvaluator() {
        return conditionEvaluator;
    }
}