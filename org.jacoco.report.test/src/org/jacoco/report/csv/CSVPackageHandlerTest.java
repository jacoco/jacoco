/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.csv;

import java.io.StringWriter;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.JavaNames;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CSVPackageHandler}.
 */
public class CSVPackageHandlerTest {

	private IReportVisitor handler;

	@Before
	public void setup() throws Exception {
		final DelimitedWriter dw = new DelimitedWriter(new StringWriter());
		final ClassRowWriter rw = new ClassRowWriter(dw, new JavaNames());
		handler = new CSVPackageHandler(rw, "group", "package");
	}

	@Test(expected = AssertionError.class)
	public void testVisitChildNegative1() throws Exception {
		handler.visitChild(new CoverageNodeImpl(ElementType.GROUP, "Foo"));
	}

	@Test(expected = AssertionError.class)
	public void testVisitChildNegative2() throws Exception {
		handler.visitChild(new CoverageNodeImpl(ElementType.BUNDLE, "Foo"));
	}

	@Test(expected = AssertionError.class)
	public void testVisitChildNegative3() throws Exception {
		handler.visitChild(new CoverageNodeImpl(ElementType.PACKAGE, "Foo"));
	}

	@Test(expected = AssertionError.class)
	public void testVisitChildNegative4() throws Exception {
		handler.visitChild(new CoverageNodeImpl(ElementType.METHOD, "Foo"));
	}

}
