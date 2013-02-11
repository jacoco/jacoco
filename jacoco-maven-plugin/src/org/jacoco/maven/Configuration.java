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


/**
 * Used in the configuration of the "check" goal for specifying minimum ratios
 * of coverage.
 */
public class Configuration extends AbstractRule {

    private RuleList rules;

    public Configuration(){
        rules=new RuleList();
    }

    public RuleList getRules(){
        return rules;
    }

    public boolean hasRules()
    {
        return rules.size()>0;
    }
}
