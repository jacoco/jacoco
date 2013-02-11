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
import org.junit.Test;

import java.util.Set;

public class RuleTest {

    @Test
    public void emptyInclude()
    {
        Rule rule=new Rule();
        rule.setElement("class");
        Assert.assertTrue(rule.matches("class","test"));
        Assert.assertTrue(rule.matches("class","package/test/test"));
    }

    @Test
    public void starInclude()
    {
        Rule rule=new Rule();
        rule.getIncludes().add("*");
        Assert.assertTrue(rule.matches("class","test"));
        Assert.assertTrue(rule.matches("class","package/test/test"));
    }

    @Test
    public void multipleInclude()
    {
        Rule rule=new Rule();
        Set<String> includes = rule.getIncludes();
        includes.add("package/sub/*");
        Assert.assertTrue(rule.matches("class","package/sub/Test"));
        Assert.assertFalse(rule.matches("class","package/another/Test"));
        includes.add("package/another/*");
        Assert.assertTrue(rule.matches("class","package/sub/Test"));
        Assert.assertTrue(rule.matches("class","package/another/Test"));
    }
}
