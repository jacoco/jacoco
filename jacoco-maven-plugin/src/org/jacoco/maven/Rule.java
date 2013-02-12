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

import org.jacoco.core.runtime.WildcardMatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Rule extends AbstractRule {
    private String element;

    private Set<String> includes;

    private List<WildcardMatcher> includeWildCardMatcher;

    public Rule() {
        includes=new HashSet<String>();
        element="class";
    }

    /**
     * Set the element type this rule applies to
     * @param element
     *          The element type rule applies e.g. class or package
     */
    public void setElement(final String element)
    {
        this.element=element;
    }

    /**
     * The element type this rule applies to
     */
    public String getElement(){
        return element;
    }

    private void initWildCardMatcher() {
        if (includeWildCardMatcher==null || includes.size()!=includeWildCardMatcher.size()) {
            includeWildCardMatcher=new ArrayList<WildcardMatcher>();
            for (String include : includes) {
                includeWildCardMatcher.add(new WildcardMatcher(include));
            }
        }
    }

    /**
     *
     * @param element The element type to check
     * @param elementName  The name of the element
     * @return  true if the rule applies to this element type and elementname
     */
    public boolean ruleApplies(final String element, final String elementName) {
        if (!this.element.equals(element)){
            return false;
        }
        if (includes.isEmpty()){
            return true;
        }
        initWildCardMatcher();
        for (WildcardMatcher wildcardMatcher : includeWildCardMatcher) {
            if (wildcardMatcher.matches(elementName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the includes filter set for this rule
     * @return A set of includes filter
     */
    public Set<String> getIncludes() {
        return includes;
    }
}
