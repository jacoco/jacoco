/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Håvard Nesvold - initial implementation
 *
 *******************************************************************************/
package org.jacoco.report.check;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CheckResultTest {

    private ICoverageNode node;
    private Limit limit;
    private String elementName;

    @Before
    public void setup() {
        node = new TestNode() {
            {
                instructionCounter = CounterImpl.getInstance(1000, 0);
            }
        };
        limit = new Limit();
        elementName = "Foo2";
    }

    @Test
    public void testOKMessage() {
        CheckResult result = CheckResult.ok(node, limit, elementName);

        assertEquals("Rule conforms for CLASS Foo2: instructions covered ratio is 0.0", result.createMessage());
    }

    @Test
    public void testTooLowMessage() {
        limit.setMinimum("0.30");

        CheckResult result = CheckResult.tooLow(node, limit, elementName);

        assertEquals("Rule violated for CLASS Foo2: instructions covered ratio is 0.00, but expected minimum is 0.30", result.createMessage());
    }

    @Test
    public void testTooHighMessage() {
        limit.setMaximum("0.70");

        CheckResult result = CheckResult.tooHigh(node, limit, elementName);

        assertEquals("Rule violated for CLASS Foo2: instructions covered ratio is 0.00, but expected maximum is 0.70", result.createMessage());
    }

    private static class TestNode extends CoverageNodeImpl {
        public TestNode() {
            super(ElementType.CLASS, "Foo");
        }
    }

}
