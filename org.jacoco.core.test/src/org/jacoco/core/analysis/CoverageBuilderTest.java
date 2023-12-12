/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
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

import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CoverageBuilder}.
 */
public class CoverageBuilderTest {

	private CoverageBuilder coverageBuilder;

	@Before
	public void setup() {
		coverageBuilder = new CoverageBuilder();
	}

	@Test
	public void testCreateClassMissed() {
		final MethodCoverageImpl method = new MethodCoverageImpl("doit", "()V",
				null);
		method.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 6);
		method.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 7);
		method.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 8);
		method.incrementMethodCounter();
		addClass(123L, false, "Sample", null, method);

		final Collection<IClassCoverage> classes = coverageBuilder.getClasses();
		assertEquals(1, classes.size());
		IClassCoverage c = classes.iterator().next();
		assertEquals("Sample", c.getName());
		assertEquals(1, c.getClassCounter().getTotalCount());
		assertEquals(0, c.getClassCounter().getCoveredCount());
		assertEquals(1, c.getMethodCounter().getTotalCount());
		assertEquals(0, c.getMethodCounter().getCoveredCount());
		assertEquals(3, c.getLineCounter().getTotalCount());
		assertEquals(0, c.getLineCounter().getCoveredCount());

		final Collection<IMethodCoverage> methods = c.getMethods();
		assertEquals(1, methods.size());
		IMethodCoverage m = methods.iterator().next();
		assertEquals("doit", m.getName());
		assertEquals("()V", m.getDesc());
		assertEquals(1, m.getMethodCounter().getTotalCount());
		assertEquals(0, m.getMethodCounter().getCoveredCount());
		assertEquals(3, m.getLineCounter().getTotalCount());
		assertEquals(0, m.getLineCounter().getCoveredCount());
	}

	@Test
	public void testCreateClassCovered() {
		final MethodCoverageImpl method = new MethodCoverageImpl("doit", "()V",
				null);
		method.increment(CounterImpl.COUNTER_0_1, CounterImpl.COUNTER_0_0, 6);
		method.increment(CounterImpl.COUNTER_0_1, CounterImpl.COUNTER_0_0, 7);
		method.increment(CounterImpl.COUNTER_0_1, CounterImpl.COUNTER_0_0, 8);
		method.incrementMethodCounter();
		addClass(123L, false, "Sample", null, method);

		final Collection<IClassCoverage> classes = coverageBuilder.getClasses();
		assertEquals(1, classes.size());
		IClassCoverage c = classes.iterator().next();
		assertEquals("Sample", c.getName());
		assertEquals(1, c.getClassCounter().getTotalCount());
		assertEquals(1, c.getClassCounter().getCoveredCount());
		assertEquals(1, c.getMethodCounter().getTotalCount());
		assertEquals(1, c.getMethodCounter().getCoveredCount());
		assertEquals(3, c.getLineCounter().getTotalCount());
		assertEquals(3, c.getLineCounter().getCoveredCount());

		final Collection<IMethodCoverage> methods = c.getMethods();
		assertEquals(1, methods.size());
		IMethodCoverage m = methods.iterator().next();
		assertEquals("doit", m.getName());
		assertEquals("()V", m.getDesc());
		assertEquals(1, m.getMethodCounter().getTotalCount());
		assertEquals(1, m.getMethodCounter().getCoveredCount());
		assertEquals(3, m.getLineCounter().getTotalCount());
		assertEquals(3, m.getLineCounter().getCoveredCount());
	}

	@Test
	public void should_not_ignore_empty_classes() {
		addClass(123L, false, "Empty", null);

		assertEquals(1, coverageBuilder.getClasses().size());
	}

	@Test(expected = IllegalStateException.class)
	public void testDuplicateClassNameDifferent() {
		MethodCoverageImpl method = new MethodCoverageImpl("doit", "()V", null);
		method.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 3);
		addClass(123L, false, "Sample", null, method);

		// Add class with different id must fail:
		method = new MethodCoverageImpl("doit", "()V", null);
		method.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 3);
		addClass(345L, false, "Sample", null, method);
	}

	@Test
	public void testDuplicateClassNameIdentical() {
		MethodCoverageImpl method = new MethodCoverageImpl("doit", "()V", null);
		method.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 3);
		addClass(123L, false, "Sample", null, method);

		// Add class with same id:
		method = new MethodCoverageImpl("doit", "()V", null);
		method.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 3);
		addClass(123L, false, "Sample", null, method);

		// Second add must be ignored:
		final Collection<IClassCoverage> classes = coverageBuilder.getClasses();
		assertEquals(1, classes.size());
	}

	@Test
	public void testCreateSourceFile() {
		final MethodCoverageImpl method1 = new MethodCoverageImpl("doit", "()V",
				null);
		method1.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 3);
		addClass(123L, false, "Sample", "Sample.java", method1);

		final MethodCoverageImpl method2 = new MethodCoverageImpl("doit", "()V",
				null);
		method2.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 6);
		addClass(234L, false, "Second", "Sample.java", method2);

		final Collection<ISourceFileCoverage> sourcefiles = coverageBuilder
				.getSourceFiles();
		assertEquals(1, sourcefiles.size());
		ISourceFileCoverage s = sourcefiles.iterator().next();

		assertEquals(2, s.getClassCounter().getTotalCount());
		assertEquals(0, s.getClassCounter().getCoveredCount());
	}

	@Test
	public void testCreateSourceFileDuplicateClassNameIdentical() {
		final MethodCoverageImpl method1 = new MethodCoverageImpl("doit", "()V",
				null);
		method1.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 3);
		addClass(123L, false, "Sample", "Sample.java", method1);

		final MethodCoverageImpl method2 = new MethodCoverageImpl("doit", "()V",
				null);
		method2.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 3);
		addClass(123L, false, "Sample", "Sample.java", method2);

		final Collection<ISourceFileCoverage> sourcefiles = coverageBuilder
				.getSourceFiles();
		assertEquals(1, sourcefiles.size());
		ISourceFileCoverage s = sourcefiles.iterator().next();

		assertEquals(1, s.getClassCounter().getTotalCount());
		assertEquals(0, s.getClassCounter().getCoveredCount());
	}

	@Test
	public void testGetBundle() {
		final MethodCoverageImpl method1 = new MethodCoverageImpl("doit", "()V",
				null);
		method1.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 3);
		addClass(1, false, "org/jacoco/examples/Sample1", null, method1);

		final MethodCoverageImpl method2 = new MethodCoverageImpl("doit", "()V",
				null);
		method2.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 6);
		addClass(2, false, "org/jacoco/examples/Sample2", null, method2);

		final MethodCoverageImpl method3 = new MethodCoverageImpl("doit", "()V",
				null);
		method3.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 1);
		addClass(3, false, "Sample3", null, method3);

		IBundleCoverage bundle = coverageBuilder.getBundle("testbundle");
		assertEquals("testbundle", bundle.getName());

		final Collection<IPackageCoverage> packages = bundle.getPackages();
		assertEquals(2, packages.size());
		Map<String, IPackageCoverage> packagesByName = new HashMap<String, IPackageCoverage>();
		for (IPackageCoverage p : packages) {
			packagesByName.put(p.getName(), p);
		}

		IPackageCoverage p1 = packagesByName.get("org/jacoco/examples");
		assertNotNull(p1);
		assertEquals(
				new HashSet<String>(Arrays.asList("org/jacoco/examples/Sample1",
						"org/jacoco/examples/Sample2")),
				getNames(p1.getClasses()));

		IPackageCoverage p2 = packagesByName.get("");
		assertNotNull(p2);
		assertEquals(Collections.singleton("Sample3"),
				getNames(p2.getClasses()));
	}

	@Test
	public void testGetNoMatchClasses() {
		MethodCoverageImpl m = new MethodCoverageImpl("doit", "()V", null);
		m.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 1);
		addClass(1, true, "Sample1", null, m);

		m = new MethodCoverageImpl("doit", "()V", null);
		m.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 2);
		addClass(2, true, "Sample2", null, m);

		m = new MethodCoverageImpl("doit", "()V", null);
		m.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 3);
		addClass(3, false, "Sample3", null, m);

		final Set<String> actual = getNames(
				coverageBuilder.getNoMatchClasses());
		final Set<String> expected = new HashSet<String>(
				Arrays.asList("Sample1", "Sample2"));

		assertEquals(expected, actual);
	}

	private Set<String> getNames(Collection<? extends ICoverageNode> nodes) {
		Set<String> result = new HashSet<String>();
		for (ICoverageNode n : nodes) {
			result.add(n.getName());
		}
		return result;
	}

	private void addClass(long id, boolean nomatch, String name, String source,
			MethodCoverageImpl... methods) {
		final ClassCoverageImpl coverage = new ClassCoverageImpl(name, id,
				nomatch);
		coverage.setSourceFileName(source);
		for (MethodCoverageImpl m : methods) {
			coverage.addMethod(m);
		}
		coverageBuilder.visitCoverage(coverage);
	}
}
