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

import java.util.List;

/**
 * The {@code Observable} interface represents a subject that maintains a list of observers
 * and notifies them of any changes in its state or metrics.
 * <p>
 * This interface is parameterized with a type {@code T},
 * allowing for the observation of various types of data.
 * </p>
 *
 * @param <T> the type of the data that this observable will provide to its observers.
 * @author Arléon Zemtsop (Cerberus)
 */
public interface Observable<T> {

    /**
     * Subscribes a single observer to this observable.
     * <p>
     * This method adds the specified observer to the list of observers
     * that will be notified when the observable's state changes.
     * </p>
     *
     * @param observer the observer to be added to the subscription list.
     */
    void subscribe(Observer<T> observer);

    /**
     * Subscribes multiple observers to this observable.
     * <p>
     * This method adds all specified observers to the list of observers
     * that will be notified when the observable's state changes.
     * </p>
     *
     * @param observers a list of observers to be added to the subscription list.
     */
    void subscribeAll(List<Observer<T>> observers);

    /**
     * Unsubscribes a single observer from this observable.
     * <p>
     * This method removes the specified observer from the list of observers,
     * preventing it from receiving further notifications about state changes.
     * </p>
     *
     * @param observer the observer to be removed from the subscription list.
     */
    void unsubscribe(Observer<T> observer);

    /**
     * Unsubscribes multiple observers from this observable.
     * <p>
     * This method removes all specified observers from the list of observers,
     * preventing them from receiving further notifications about state changes.
     * </p>
     *
     * @param observers a list of observers to be removed from the subscription list.
     */
    void unsubscribeAll(List<Observer<T>> observers);

    /**
     * Notifies all subscribed observers with the given metric value.
     * <p>
     * This method triggers an update to all observers, passing them
     * the current metric value, which they can use to react to the change.
     * </p>
     *
     * @param metricValue the new metric value to be sent to the observers.
     */
    void notifyObservers(T metricValue);

    /**
     * Notifies a specific observer with the given metric value.
     * <p>
     * This method triggers an update to the specified observer only,
     * passing the current metric value for its consideration.
     * </p>
     *
     * @param subscriber the observer to be notified.
     * @param metricValue the new metric value to be sent to the observer.
     */
    void notifyObserver(Observer<T> subscriber, T metricValue);

    /**
     * For evaluation
     * @param subscriber
     * @param metricValue
     * @param cycleId
     * @param notificationStartTime
     */
    void notifyObserver(Observer<T> subscriber, T metricValue, String cycleId, long notificationStartTime);

}