/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import junit.framework.Assert;
import org.jacoco.core.analysis.ICoverageNode;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;

import static org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import static org.jacoco.core.analysis.ICoverageNode.ElementType;

public class RuleTest {

    @Test
    public void testClassElement() throws Exception{
        Assert.assertTrue("Element Type should not be allowed", checkValueIsUnsupported("element","group"));
        Assert.assertTrue("Element Type should not be allowed", checkValueIsUnsupported("element","bundle"));
        Assert.assertTrue("Element Type should not be allowed", checkValueIsUnsupported("element","i_am_invalid"));
        checkRuleElement("package", ElementType.PACKAGE);
        checkRuleElement("class", ElementType.CLASS);
    }


    @Test
    public void testCounterEntity() throws Exception{
        checkCounterEntity("line", CounterEntity.LINE);
        checkCounterEntity("branch", CounterEntity.BRANCH);
        checkCounterEntity("instruction", CounterEntity.INSTRUCTION);
        checkCounterEntity("class", CounterEntity.CLASS);
        checkCounterEntity("complexity", CounterEntity.COMPLEXITY);
        Assert.assertTrue("Entity should not be allowed", checkValueIsUnsupported("counterEntity","i_am_invalid"));
    }

    @Test
    public void testCounterProperty() throws Exception {
        Rule rule=new Rule();
        rule.setCounterProperty("coveredRatio");
        Assert.assertTrue("Property should not be allowed", checkValueIsUnsupported("counterProperty","i_am_invalid"));
    }

    @Test
    public void checkRuleErrorMessages() {
        Rule rule=new Rule();
        List<String> ruleErrorMessages = rule.getRuleErrorMessages();
        Assert.assertTrue("Mandatory fields for rule not filled in",ruleErrorMessages.size()>0);
        Assert.assertTrue("Must report missing 'element'",assertContainsString(ruleErrorMessages,"'element'"));
        Assert.assertTrue("Must report missing 'counterEntity'",assertContainsString(ruleErrorMessages,"'counterEntity'"));
        Assert.assertTrue("Must report missing 'counterProperty'",assertContainsString(ruleErrorMessages,"'counterProperty'"));
        Assert.assertTrue("Must report valid minimum value",assertContainsString(ruleErrorMessages,"'minimum'"));
    }

    private boolean assertContainsString(List<String> errorMessages, String subString) {
        for (String errorMessage : errorMessages) {
            if (errorMessage.contains(subString)) {
                return true;
            }
        }
        return false;
    }

    private void checkRuleElement(String text, ICoverageNode.ElementType elementType) {
        Rule rule=new Rule();
        rule.setElement(text);
        Assert.assertTrue("Rule should apply to this element", rule.ruleApplies(elementType));
        EnumSet<ElementType> noneApplicableTypes = EnumSet.allOf(ElementType.class);
        noneApplicableTypes.remove(elementType);
        for (ElementType type : noneApplicableTypes) {
            Assert.assertFalse("Rule should not apply to this type",rule.ruleApplies(type));
        }
    }

    private void checkCounterEntity(String text, CounterEntity entity){
        Rule rule=new Rule();
        rule.setCounterEntity(text);
        Assert.assertEquals("Must match entity type", entity, rule.getCounterEntity());
    }


    private boolean checkValueIsUnsupported(String property, String text) throws Exception{
        boolean exceptionThrown=false;
        Rule rule=new Rule();
        try{
            String setterMethod = String.format("set%C%s",property.charAt(0), property.substring(1));
            Method m=Rule.class.getDeclaredMethod(setterMethod,String.class);
            m.invoke(rule,text);
            rule.setCounterEntity(text);
        }
        catch (Exception e){
            if (e.getCause().getMessage().contains(String.format("Invalid %s '%s'",property,text))) {
                exceptionThrown=true;
            }
            else {
                throw  e;
            }
        }
        return exceptionThrown;
    }

}
