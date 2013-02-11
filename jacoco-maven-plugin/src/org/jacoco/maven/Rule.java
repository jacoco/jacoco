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
    private String name;
    private String element;
    /**
     * pattern to consider for this rule
     *
     * @parameter
     */
    private Set<String> includes;
    private List<WildcardMatcher> includeWildCardMatcher;

    public Rule()
    {
        includes=new HashSet<String>();
        name="no name";
        element="class";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setElement(String element)
    {
        this.element=element;
    }

    public String getElement(){
        return element;
    }

    private boolean hasIncludes() {
        return includes!=null && includes.size()>0;
    }

    private void initWildCardMatcher(){
        if (includeWildCardMatcher==null || includes.size()!=includeWildCardMatcher.size())
        {
            includeWildCardMatcher=new ArrayList<WildcardMatcher>();
            for (String include : includes) {
                includeWildCardMatcher.add(new WildcardMatcher(include));
            }
        }
    }

    public boolean matches(String elemenType,String elementName) {
        if (!this.element.equals(elemenType)){
            return false;
        }
        if (!hasIncludes()){
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

    public Set<String> getIncludes() {
        return includes;
    }
}
