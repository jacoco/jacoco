/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.analysis;

import static org.junit.Assert.assertEquals;

import org.jacoco.core.data.IMethodStructureVisitor;
import org.junit.Test;

/**
 * Unit test for {@link MethodCoverage}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class MethodCoverageTest {

	@Test
	public void testProperties() {
		// Example: java.util.Collections.emptySet()
		MethodCoverage data = new MethodCoverage("emptySet",
				"()Ljava/util/Set;",
				"<T:Ljava/lang/Object;>()Ljava/util/Set<TT;>;");
		assertEquals(ICoverageNode.ElementType.METHOD, data.getElementType());
		assertEquals("emptySet", data.getName());
		assertEquals("()Ljava/util/Set;", data.getDesc());
		assertEquals("<T:Ljava/lang/Object;>()Ljava/util/Set<TT;>;",
				data.getSignature());
	}

	@Test
	public void testEmptyMethod() {
		ICoverageNode data = new MethodCoverage("sample", "()V", null);

		assertEquals(CounterImpl.getInstance(0, 0), data.getLineCounter());
		assertEquals(CounterImpl.getInstance(0, 0),
				data.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(0, 0), data.getBranchCounter());
		assertEquals(CounterImpl.getInstance(1, 0), data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(0, 0), data.getClassCounter());
	}

	@Test
	public void testInstructionMissed() {
		MethodCoverage data = new MethodCoverage("sample", "()V", null);
		data.addInsn(false, 0);
		data.addInsn(false, 0);

		assertEquals(CounterImpl.getInstance(1, 0), data.getLineCounter());
		assertEquals(CounterImpl.getInstance(2, 0),
				data.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(0, 0), data.getBranchCounter());
		assertEquals(CounterImpl.getInstance(1, 0), data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(0, 0), data.getClassCounter());
	}

	@Test
	public void testInstructionCovered() {
		MethodCoverage data = new MethodCoverage("sample", "()V", null);
		data.addInsn(true, 0);
		data.addInsn(false, 0);

		assertEquals(CounterImpl.getInstance(1, 1), data.getLineCounter());
		assertEquals(CounterImpl.getInstance(2, 1),
				data.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(0, 0), data.getBranchCounter());
		assertEquals(CounterImpl.getInstance(1, 1), data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(0, 0), data.getClassCounter());
	}

	@Test
	public void testInstructionNoLine() {
		MethodCoverage data = new MethodCoverage("sample", "()V", null);
		data.addInsn(false, IMethodStructureVisitor.UNKNOWN_LINE);

		assertEquals(CounterImpl.getInstance(0, 0), data.getLineCounter());
		assertEquals(CounterImpl.getInstance(1, 0),
				data.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(0, 0), data.getBranchCounter());
		assertEquals(CounterImpl.getInstance(1, 0), data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(0, 0), data.getClassCounter());
	}

	@Test
	public void testBranches() {
		MethodCoverage data = new MethodCoverage("sample", "()V", null);
		data.addBranches(7, 3, 0);

		assertEquals(CounterImpl.getInstance(0, 0), data.getLineCounter());
		assertEquals(CounterImpl.getInstance(0, 0),
				data.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(7, 3), data.getBranchCounter());
		assertEquals(CounterImpl.getInstance(1, 0), data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(0, 0), data.getClassCounter());
	}

}
