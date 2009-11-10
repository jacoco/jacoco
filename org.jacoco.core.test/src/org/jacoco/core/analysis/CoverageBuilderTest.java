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
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IClassStructureVisitor;
import org.jacoco.core.data.IMethodStructureVisitor;
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
		final IClassStructureVisitor classStructure = coverageBuilder
				.visitClassStructure(123L, "org/jacoco/examples/Sample");
		final IMethodStructureVisitor methodStructure = classStructure
				.visitMethodStructure("doit", "()V", null);
		methodStructure.block(0, 5, new int[] { 6, 7, 8 });
		methodStructure.visitEnd();
		classStructure.visitEnd();

		final Collection<ClassCoverage> classes = coverageBuilder.getClasses();
		assertEquals(1, classes.size(), 1.0);
		ClassCoverage c = classes.iterator().next();
		assertEquals("org/jacoco/examples/Sample", c.getName());
		assertEquals(1, c.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, c.getClassCounter().getCoveredCount(), 0.0);
		assertEquals(1, c.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, c.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, c.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, c.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(3, c.getLineCounter().getTotalCount(), 0.0);
		assertEquals(0, c.getLineCounter().getCoveredCount(), 0.0);

		final Collection<MethodCoverage> methods = c.getMethods();
		assertEquals(1, methods.size(), 1.0);
		MethodCoverage m = methods.iterator().next();
		assertEquals("doit", m.getName());
		assertEquals("()V", m.getDesc());
		assertEquals(1, m.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, m.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, m.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, m.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(3, m.getLineCounter().getTotalCount(), 0.0);
		assertEquals(0, m.getLineCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testCreateClassCovered() {
		executionData.visitClassExecution(123L, "org/jacoco/examples/Sample",
				new boolean[] { true });
		final IClassStructureVisitor classStructure = coverageBuilder
				.visitClassStructure(123L, "org/jacoco/examples/Sample");
		final IMethodStructureVisitor methodStructure = classStructure
				.visitMethodStructure("doit", "()V", null);
		methodStructure.block(0, 5, new int[] { 6, 7, 8 });
		methodStructure.visitEnd();
		classStructure.visitEnd();

		final Collection<ClassCoverage> classes = coverageBuilder.getClasses();
		assertEquals(1, classes.size(), 1.0);
		ClassCoverage c = classes.iterator().next();
		assertEquals("org/jacoco/examples/Sample", c.getName());
		assertEquals(1, c.getClassCounter().getTotalCount(), 0.0);
		assertEquals(1, c.getClassCounter().getCoveredCount(), 0.0);
		assertEquals(1, c.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(1, c.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, c.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(1, c.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(3, c.getLineCounter().getTotalCount(), 0.0);
		assertEquals(3, c.getLineCounter().getCoveredCount(), 0.0);

		final Collection<MethodCoverage> methods = c.getMethods();
		assertEquals(1, methods.size(), 1.0);
		MethodCoverage m = methods.iterator().next();
		assertEquals("doit", m.getName());
		assertEquals("()V", m.getDesc());
		assertEquals(1, m.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(1, m.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, m.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(1, m.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(3, m.getLineCounter().getTotalCount(), 0.0);
		assertEquals(3, m.getLineCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testCreateSourceFile() {
		final IClassStructureVisitor classStructure1 = coverageBuilder
				.visitClassStructure(123L, "org/jacoco/examples/Sample");
		classStructure1.visitSourceFile("Sample.java");
		classStructure1.visitEnd();

		final IClassStructureVisitor classStructure2 = coverageBuilder
				.visitClassStructure(123L, "org/jacoco/examples/Sample");
		classStructure2.visitSourceFile("Sample.java");
		classStructure2.visitEnd();

		final Collection<SourceFileCoverage> sourcefiles = coverageBuilder
				.getSourceFiles();
		assertEquals(1, sourcefiles.size(), 1.0);
		SourceFileCoverage s = sourcefiles.iterator().next();

		assertEquals(2, s.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, s.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testGetBundle() {
		coverageBuilder.visitClassStructure(1, "org/jacoco/examples/Sample1")
				.visitEnd();
		coverageBuilder.visitClassStructure(2, "org/jacoco/examples/Sample2")
				.visitEnd();
		coverageBuilder.visitClassStructure(3, "Sample3").visitEnd();
		BundleCoverage bundle = coverageBuilder.getBundle("testbundle");
		assertEquals("testbundle", bundle.getName());

		final Collection<PackageCoverage> packages = bundle.getPackages();
		assertEquals(2, packages.size(), 0.0);
		Map<String, PackageCoverage> packagesByName = new HashMap<String, PackageCoverage>();
		for (PackageCoverage p : packages) {
			packagesByName.put(p.getName(), p);
		}

		PackageCoverage p1 = packagesByName.get("org/jacoco/examples");
		assertNotNull(p1);
		assertEquals(new HashSet<String>(Arrays.asList(
				"org/jacoco/examples/Sample1", "org/jacoco/examples/Sample2")),
				getNames(p1.getClasses()));

		PackageCoverage p2 = packagesByName.get("");
		assertNotNull(p2);
		assertEquals(Collections.singleton("Sample3"),
				getNames(p2.getClasses()));
	}

	private Set<String> getNames(Collection<? extends ICoverageNode> nodes) {
		Set<String> result = new HashSet<String>();
		for (ICoverageNode n : nodes) {
			result.add(n.getName());
		}
		return result;
	}

}
