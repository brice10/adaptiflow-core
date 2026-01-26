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

import org.junit.Assert;
import org.junit.Test;
import tools.spirals.cerberus237.adaptationactionsbase.core.IAdaptationAction;

import java.util.Map;

/**
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ActionScannerTest {

    @Test
    public void testScanForActions_ValidPackage() {
        Map<String, Class<? extends IAdaptationAction>> actions = ActionScanner.scanForActions("tools.spirals.cerberus237.adaptiflow.scanner");
        Assert.assertFalse("Action map should not be empty", actions.isEmpty());
        Assert.assertTrue("Action map should contain 'ExampleAction'", actions.containsKey("exampleaction"));
    }

    @Test(expected = RuntimeException.class)
    public void testScanForActions_InvalidPackage() {
        ActionScanner.scanForActions("tools.descartes.teastore.adaptationobserver.invalid");
    }

}