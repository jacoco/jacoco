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


import java.util.ArrayList;
import java.util.List;

/**
 * Used in the configuration of the "check" goal for specifying minimum ratios
 * of coverage and rules which in turn can define min ratios.
 */
public class CheckConfiguration extends AbstractRule{

    private List<Rule> rules;

    public CheckConfiguration(){
        rules=new ArrayList<Rule>();
        setName("Project Coverage");
    }

    public List<Rule> getRules(){
        return rules;
    }
}
