/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import static org.jacoco.core.analysis.ICoverageNode.ElementType.SOURCEFILE;
import static org.junit.Assert.assertEquals;

import org.jacoco.core.analysis.EBigOFunction;
import org.jacoco.core.analysis.EBigOFunction.Type;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.junit.Test;

/**
 * Unit test for {@link SourceFileCoverageImpl}.
 */
public class SourceFileCoverageImplTest {

	@Test
	public void testProperties() {
		SourceFileCoverageImpl data = new SourceFileCoverageImpl("Sample.java",
				"org/jacoco/examples");
		assertEquals(SOURCEFILE, data.getElementType());
		assertEquals("org/jacoco/examples", data.getPackageName());
	}

	@Test
	public void testIncrementChildWithEBigOLines() {
		SourceFileCoverageImpl node = new SourceFileCoverageImpl("Sample.java",
				"org/jacoco/examples");

		final SourceNodeImpl child = new SourceNodeImpl(ElementType.CLASS,
				"Foo");
		final EBigOFunction ebigo = new EBigOFunction(Type.Linear, 2, 3);
		final EBigOFunction lineEbigo = new EBigOFunction(Type.Exponential, 3,
				4);
		child.increment(CounterImpl.getInstance(1, 11, 11),
				CounterImpl.getInstance(3, 33, 0), 5);
		child.setEBigOFunction(ebigo);
		child.setLineEBigOFunction(lineEbigo, 5);

		node.increment(child);

		assertEquals(ebigo, node.getEBigOFunction());
		assertEquals(lineEbigo, node.getLineEBigOFunction(5));
	}

}
