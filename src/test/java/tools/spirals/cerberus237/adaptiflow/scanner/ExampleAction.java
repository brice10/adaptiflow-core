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

import tools.spirals.cerberus237.adaptationactionsbase.core.IAdaptationAction;
import tools.spirals.cerberus237.adaptationactionsbase.enums.AdaptationActionResult;

/**
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ExampleAction implements IAdaptationAction {

    private final String actionId;
    private final Runnable action;
    private boolean performed = false;

    public ExampleAction(String actionId, Runnable action) {
        this.actionId = actionId;
        this.action = action;
    }

    public ExampleAction(Runnable action) {
        this("test-action-" + System.currentTimeMillis(), action);
    }

    @Override
    public AdaptationActionResult perform() {
        action.run();
        performed = true;
        return AdaptationActionResult.SUCCESS;
    }

    @Override
    public String getActionId() {
        return actionId;
    }

    @Override
    public String getDescription() {
        return "Test action: " + actionId;
    }

    @Override
    public boolean canPerform() {
        return true;
    }

    public boolean wasPerformed() {
        return performed;
    }
}
