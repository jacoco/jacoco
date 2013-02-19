/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    Kyle Lieber - implementation of CheckMojo
 *
 *******************************************************************************/
package org.jacoco.maven;

import static org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import static org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.analysis.ICoverageNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class Rule {

    private static Collection<String> ACCEPTED_COUNTER_PROPERTIES = Arrays.asList("coveredRatio");
    private static Collection<String> ACCEPTED_ELEMENTS = Arrays.asList("class","package");
    private static List<String> ACCEPTED_COUNTER_ENTITIES = Arrays.asList("instruction","branch","line","complexity","method","class");

    /**
     * Name for this rule
     */
    private String name;

    /**
     * Element this rule applies to
     */
    private ElementType element;

    /**
     * The counterEntity covered by this rule
     */
    private CounterEntity counterEntity;

    /**
     * The counterProperty that is being checked by the rule
     */
    private String counterProperty;

    /**
     *  The minimum value required
     */
    private double minimum=-1d;

    /**
     * Sets the counterProperty for this rule
     * @param counterProperty The counterProperty to check against
     * @throws RuntimeException if the value is not supported (so it can reported by maven plugin)
     */
    public void setCounterProperty(String counterProperty) {
        if (ACCEPTED_COUNTER_PROPERTIES.contains(counterProperty)) {
            this.counterProperty=counterProperty;
        }
        else {
            throw new RuntimeException(String.format("Invalid counterProperty '%s', accepted values are %s",counterProperty,ACCEPTED_COUNTER_PROPERTIES)) ;
        }
    }

    /**
     * Sets the element for this rule
     * @param element The element name
     * @throws RuntimeException if the value is not supported (so it can reported by maven plugin)
     */
    public void setElement(String element) {
        if (ACCEPTED_ELEMENTS.contains(element)) {
            this.element=ICoverageNode.ElementType.valueOf(element.toUpperCase());
        }
        else {
            throw new RuntimeException(String.format("Invalid element '%s', accepted values are %s",element, ACCEPTED_ELEMENTS)) ;
        }
    }

    /**
     * Sets the counterEntity for this rule
     * @param counterEntity The element name
     * @throws RuntimeException if the value is not supported (so it can reported by maven plugin)
     */
    public void setCounterEntity(String counterEntity) {
        if (ACCEPTED_COUNTER_ENTITIES.contains(counterEntity)) {
            this.counterEntity=ICoverageNode.CounterEntity.valueOf(counterEntity.toUpperCase());
        }
        else {
            throw new RuntimeException(String.format("Invalid counterEntity '%s', accepted values are %s",counterEntity, ACCEPTED_COUNTER_ENTITIES)) ;
        }
    }

    /**
     * Check the rule should apply to the element.
     * @param element The element type to check
     * @return  true if the rule applies to this element type
     */
    public boolean ruleApplies(final ICoverageNode.ElementType element) {
        return this.element.equals(element);
    }

    public String getName() {
        return this.name;
    }

    public CounterEntity getCounterEntity() {
        return this.counterEntity;
    }


    public double getMinimum() {
        return this.minimum;
    }

    /**
     * Checks this rule and gets a list of errors for this rule.
     * @return  list of error messages, empty list if the rule is valid.
     */
    public List<String> getRuleErrorMessages() {
        ArrayList<String> errorMessages=new ArrayList<String>();
        if (this.counterProperty==null) {
            errorMessages.add("'counterProperty' must be defined");
        }
        if (this.element==null ) {
            errorMessages.add("'element' must be defined");
        }
        if (this.counterEntity==null) {
            errorMessages.add("'counterEntity' must be defined");
        }
        if (this.minimum<0 || this.minimum>100) {
            errorMessages.add("Value for 'minimum' for  must be between 0 and 100");
        }
        return errorMessages;
    }

}
