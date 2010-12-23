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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CoverageBuilder}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class CoverageBuilderTest {

	private CoverageBuilder coverageBuilder;

	@Before
	public void setup() {
		coverageBuilder = new CoverageBuilder();
	}

	@Test
	public void testCreateClassMissed() {
		final MethodCoverage method = new MethodCoverage("doit", "()V", null);
		method.addInsn(false, 6);
		method.addInsn(false, 7);
		method.addInsn(false, 8);
		addClass(123L, "Sample", null, method);

		final Collection<ClassCoverage> classes = coverageBuilder.getClasses();
		assertEquals(1, classes.size(), 1.0);
		ClassCoverage c = classes.iterator().next();
		assertEquals("Sample", c.getName());
		assertEquals(1, c.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, c.getClassCounter().getCoveredCount(), 0.0);
		assertEquals(1, c.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, c.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(3, c.getLineCounter().getTotalCount(), 0.0);
		assertEquals(0, c.getLineCounter().getCoveredCount(), 0.0);

		final Collection<MethodCoverage> methods = c.getMethods();
		assertEquals(1, methods.size(), 1.0);
		MethodCoverage m = methods.iterator().next();
		assertEquals("doit", m.getName());
		assertEquals("()V", m.getDesc());
		assertEquals(1, m.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, m.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(3, m.getLineCounter().getTotalCount(), 0.0);
		assertEquals(0, m.getLineCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testCreateClassCovered() {
		final MethodCoverage method = new MethodCoverage("doit", "()V", null);
		method.addInsn(true, 6);
		method.addInsn(true, 7);
		method.addInsn(true, 8);
		addClass(123L, "Sample", null, method);

		final Collection<ClassCoverage> classes = coverageBuilder.getClasses();
		assertEquals(1, classes.size(), 1.0);
		ClassCoverage c = classes.iterator().next();
		assertEquals("Sample", c.getName());
		assertEquals(1, c.getClassCounter().getTotalCount(), 0.0);
		assertEquals(1, c.getClassCounter().getCoveredCount(), 0.0);
		assertEquals(1, c.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(1, c.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(3, c.getLineCounter().getTotalCount(), 0.0);
		assertEquals(3, c.getLineCounter().getCoveredCount(), 0.0);

		final Collection<MethodCoverage> methods = c.getMethods();
		assertEquals(1, methods.size(), 1.0);
		MethodCoverage m = methods.iterator().next();
		assertEquals("doit", m.getName());
		assertEquals("()V", m.getDesc());
		assertEquals(1, m.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(1, m.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(3, m.getLineCounter().getTotalCount(), 0.0);
		assertEquals(3, m.getLineCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testIgnoreClassesWithoutCode() {
		final MethodCoverage method = new MethodCoverage("doit", "()V", null);
		addClass(123L, "Sample", null, method);

		final Collection<ClassCoverage> classes = coverageBuilder.getClasses();
		assertTrue(classes.isEmpty());
	}

	@Test(expected = IllegalStateException.class)
	public void testDuplicateClassName() {
		MethodCoverage method = new MethodCoverage("doit", "()V", null);
		method.addInsn(false, 3);
		addClass(123L, "Sample", null, method);

		method = new MethodCoverage("doit", "()V", null);
		method.addInsn(false, 3);
		addClass(345L, "Sample", null, method);
	}

	@Test
	public void testCreateSourceFile() {
		final MethodCoverage method1 = new MethodCoverage("doit", "()V", null);
		method1.addInsn(false, 3);
		addClass(123L, "Sample", "Sample.java", method1);

		final MethodCoverage method2 = new MethodCoverage("doit", "()V", null);
		method2.addInsn(false, 6);
		addClass(234L, "Second", "Sample.java", method2);

		final Collection<SourceFileCoverage> sourcefiles = coverageBuilder
				.getSourceFiles();
		assertEquals(1, sourcefiles.size(), 1.0);
		SourceFileCoverage s = sourcefiles.iterator().next();

		assertEquals(2, s.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, s.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testGetBundle() {
		final MethodCoverage method1 = new MethodCoverage("doit", "()V", null);
		method1.addInsn(false, 3);
		addClass(1, "org/jacoco/examples/Sample1", null, method1);

		final MethodCoverage method2 = new MethodCoverage("doit", "()V", null);
		method2.addInsn(false, 6);
		addClass(2, "org/jacoco/examples/Sample2", null, method2);

		final MethodCoverage method3 = new MethodCoverage("doit", "()V", null);
		method3.addInsn(false, 1);
		addClass(3, "Sample3", null, method3);

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
		assertEquals(
				new HashSet<String>(Arrays.asList(
						"org/jacoco/examples/Sample1",
						"org/jacoco/examples/Sample2")),
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

	private void addClass(long id, String name, String source,
			MethodCoverage... methods) {
		final ClassCoverage coverage = new ClassCoverage(name, id, null,
				"java/lang/Object", new String[0]);
		coverage.setSourceFileName(source);
		for (MethodCoverage m : methods) {
			coverage.addMethod(m);
		}
		coverageBuilder.visitCoverage(coverage);
	}
}
