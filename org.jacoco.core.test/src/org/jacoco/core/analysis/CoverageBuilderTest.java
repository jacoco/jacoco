/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.analysis;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IClassStructureOutput;
import org.jacoco.core.data.IMethodStructureOutput;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CoverageBuilder}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageBuilderTest {

	private ExecutionDataStore executionData;

	private CoverageBuilder coverageBuilder;

	@Before
	public void setup() {
		executionData = new ExecutionDataStore();
		coverageBuilder = new CoverageBuilder(executionData);
	}

	@Test
	public void testCreateClassNotCovered() {
		final IClassStructureOutput classStructure = coverageBuilder
				.classStructure(123L, "org/jacoco/examples/Sample",
						"testbundle");
		final IMethodStructureOutput methodStructure = classStructure
				.methodStructure(0, "doit", "()V", null);
		methodStructure.block(0, 5, new int[] { 6, 7, 8 });
		methodStructure.end();
		classStructure.end();

		final Collection<ClassNode> classes = coverageBuilder.getClasses();
		assertEquals(1, classes.size(), 1.0);
		ClassNode c = classes.iterator().next();
		assertEquals("org/jacoco/examples/Sample", c.getName());
		assertEquals("testbundle", c.getBundle());
		assertEquals(1, c.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, c.getClassCounter().getCoveredCount(), 0.0);
		assertEquals(1, c.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, c.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, c.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, c.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(3, c.getLineCounter().getTotalCount(), 0.0);
		assertEquals(0, c.getLineCounter().getCoveredCount(), 0.0);

		final Collection<ICoverageDataNode> methods = c.getChilden();
		assertEquals(1, methods.size(), 1.0);
		MethodNode m = (MethodNode) methods.iterator().next();
		assertEquals("doit", m.getName());
		assertEquals("()V", m.getDesc());
		assertEquals(1, m.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, m.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, m.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, m.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(3, m.getLineCounter().getTotalCount(), 0.0);
		assertEquals(0, m.getLineCounter().getCoveredCount(), 0.0);

		final Collection<ICoverageDataNode> blocks = m.getChilden();
		assertEquals(1, blocks.size(), 1.0);
		ICoverageDataNode b = blocks.iterator().next();
		assertEquals(1, b.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, b.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(3, b.getLineCounter().getTotalCount(), 0.0);
		assertEquals(0, b.getLineCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testCreateClassCovered() {
		executionData.classExecution(123L,
				new boolean[][] { new boolean[] { true } });
		final IClassStructureOutput classStructure = coverageBuilder
				.classStructure(123L, "org/jacoco/examples/Sample",
						"testbundle");
		final IMethodStructureOutput methodStructure = classStructure
				.methodStructure(0, "doit", "()V", null);
		methodStructure.block(0, 5, new int[] { 6, 7, 8 });
		methodStructure.end();
		classStructure.end();

		final Collection<ClassNode> classes = coverageBuilder.getClasses();
		assertEquals(1, classes.size(), 1.0);
		ClassNode c = classes.iterator().next();
		assertEquals("org/jacoco/examples/Sample", c.getName());
		assertEquals("testbundle", c.getBundle());
		assertEquals(1, c.getClassCounter().getTotalCount(), 0.0);
		assertEquals(1, c.getClassCounter().getCoveredCount(), 0.0);
		assertEquals(1, c.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(1, c.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, c.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(1, c.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(3, c.getLineCounter().getTotalCount(), 0.0);
		assertEquals(3, c.getLineCounter().getCoveredCount(), 0.0);

		final Collection<ICoverageDataNode> methods = c.getChilden();
		assertEquals(1, methods.size(), 1.0);
		MethodNode m = (MethodNode) methods.iterator().next();
		assertEquals("doit", m.getName());
		assertEquals("()V", m.getDesc());
		assertEquals(1, m.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(1, m.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, m.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(1, m.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(3, m.getLineCounter().getTotalCount(), 0.0);
		assertEquals(3, m.getLineCounter().getCoveredCount(), 0.0);

		final Collection<ICoverageDataNode> blocks = m.getChilden();
		assertEquals(1, blocks.size(), 1.0);
		ICoverageDataNode b = blocks.iterator().next();
		assertEquals(1, b.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(1, b.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(3, b.getLineCounter().getTotalCount(), 0.0);
		assertEquals(3, b.getLineCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testCreateSourceFile() {
		final IClassStructureOutput classStructure1 = coverageBuilder
				.classStructure(123L, "org/jacoco/examples/Sample",
						"testbundle");
		classStructure1.sourceFile("Sample.java");
		classStructure1.end();

		final IClassStructureOutput classStructure2 = coverageBuilder
				.classStructure(123L, "org/jacoco/examples/Sample",
						"testbundle");
		classStructure2.sourceFile("Sample.java");
		classStructure2.end();

		final Collection<SourceFileNode> sourcefiles = coverageBuilder
				.getSourceFiles();
		assertEquals(1, sourcefiles.size(), 1.0);
		SourceFileNode s = sourcefiles.iterator().next();

		assertEquals(2, s.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, s.getClassCounter().getCoveredCount(), 0.0);
	}

}
