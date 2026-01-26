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
package tools.spirals.cerberus237.adaptiflow.scanner;

import org.reflections.Reflections;

import tools.spirals.cerberus237.adaptationactionsbase.core.IAdaptationAction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for scanning a specified package to find all classes that implement
 * the {@link IAdaptationAction} interface. This class facilitates the dynamic loading
 * of adaptation actions in the application.
 * <p>
 * The {@code ActionScanner} uses the Reflections library to discover classes at runtime,
 * allowing for a flexible architecture where new adaptation actions can be added
 * without modifying the core application logic.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * Map&lt;String, Class&lt;? extends IAdaptationAction&gt;&gt; actions = ActionScanner.scanForActions("tools.descartes.teastore.recommender.adaptation.actions");
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ActionScanner {

    /**
     * Scans the specified package for classes that implement {@link IAdaptationAction}
     * and populates a map with their names and classes.
     *
     * @param packageToScan The package to scan for adaptation action classes.
     * @return A map where keys are action names (in lowercase) and values are the corresponding action classes.
     */
    public static Map<String, Class<? extends IAdaptationAction>> scanForActions(String packageToScan) {
        Map<String, Class<? extends IAdaptationAction>> actionMap = new HashMap<>();
        try {
            Reflections reflections = new Reflections(packageToScan);
            Set<Class<? extends IAdaptationAction>> actionClasses = reflections.getSubTypesOf(IAdaptationAction.class);

            for (Class<? extends IAdaptationAction> actionClass : actionClasses) {
                String actionName = actionClass.getSimpleName().toLowerCase();
                actionMap.put(actionName, actionClass);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan package: " + packageToScan, e);
        }
        return actionMap;
    }
}