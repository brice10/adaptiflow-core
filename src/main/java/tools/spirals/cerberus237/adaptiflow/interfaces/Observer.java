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
package tools.spirals.cerberus237.adaptiflow.interfaces;

/**
 * The {@code Observer} interface represents an entity that wishes to be notified
 * of changes in an observable object.
 * <p>
 * This interface is parameterized with a type {@code T},
 * allowing observers to receive and handle various types of data.
 * </p>
 *
 * @param <T> the type of the data that this observer will receive from the observable.
 * @author Arléon Zemtsop (Cerberus)
 */
public interface Observer<T> {

    /**
     * Updates the observer with a new metric value and an associated message.
     * <p>
     * This method is called by the observable to notify the observer of a change
     * in the state or a new metric value, along with a message providing context
     * or information about the update.
     * </p>
     *
     * @param metricValue the new metric value provided by the observable.
     * @param message     a message providing context or information about the update.
     */
    void update(T metricValue, String message);

    /**
     * Returns the condition evaluator associated with this observer.
     * <p>
     * The condition evaluator is used to determine whether the observer
     * should take action based on the received metric value.
     * </p>
     *
     * @return the condition evaluator for this observer.
     */
    ConditionEvaluator<T> getConditionEvaluator();

    /**
     * For evaluation
     * @param metricValue
     * @param message
     * @param cycleId
     */
    void update(T metricValue, String message, String cycleId);
}