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
package org.jacoco.report.check;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.JavaNames;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link BundleChecker}.
 */
public class BundleCheckerTest implements IViolationsOutput {

	private List<Rule> rules;
	private ILanguageNames names;
	private List<String> messages;

	@Before
	public void setup() {
		rules = new ArrayList<Rule>();
		names = new JavaNames();
		messages = new ArrayList<String>();
	}

	@Test
	public void testBundleLimit() {
		addRule(ElementType.BUNDLE);
		final BundleChecker checker = new BundleChecker(rules, names, this);
		checker.checkBundle(createBundle());
		assertMessage(
				"Rule violated for bundle Test: instructions covered ratio is 0.50, but expected minimum is 0.75");
	}

	@Test
	public void testPackageLimit() {
		addRule(ElementType.PACKAGE);
		final BundleChecker checker = new BundleChecker(rules, names, this);
		checker.checkBundle(createBundle());
		assertMessage(
				"Rule violated for package org.jacoco.example: instructions covered ratio is 0.50, but expected minimum is 0.75");
	}

	@Test
	public void testSourceFileLimit() {
		addRule(ElementType.SOURCEFILE);
		final BundleChecker checker = new BundleChecker(rules, names, this);
		checker.checkBundle(createBundle());
		assertMessage(
				"Rule violated for source file org/jacoco/example/FooClass.java: instructions covered ratio is 0.50, but expected minimum is 0.75");
	}

	@Test
	public void testClassLimit() {
		addRule(ElementType.CLASS);
		final BundleChecker checker = new BundleChecker(rules, names, this);
		checker.checkBundle(createBundle());
		assertMessage(
				"Rule violated for class org.jacoco.example.FooClass: instructions covered ratio is 0.50, but expected minimum is 0.75");
	}

	@Test
	public void testMethodLimit() {
		addRule(ElementType.METHOD);
		final BundleChecker checker = new BundleChecker(rules, names, this);
		checker.checkBundle(createBundle());
		assertMessage(
				"Rule violated for method org.jacoco.example.FooClass.fooMethod(): instructions covered ratio is 0.50, but expected minimum is 0.75");
	}

	@Test
	public void testGroupLimitNotSupported() {
		addRule(ElementType.GROUP);
		final BundleChecker checker = new BundleChecker(rules, names, this);
		checker.checkBundle(createBundle());
		assertEquals(Collections.emptyList(), messages);
	}

	@Test
	public void testLimitOk() {
		final Rule rule = new Rule();
		rule.setElement(ElementType.BUNDLE);
		final Limit limit = rule.createLimit();
		limit.setMinimum("0.25");
		rules.add(rule);
		final BundleChecker checker = new BundleChecker(rules, names, this);
		checker.checkBundle(createBundle());
		assertEquals(Collections.emptyList(), messages);
	}

	@Test
	public void testBundleNoMatch() {
		addRule(ElementType.BUNDLE).setExcludes("*");
		final BundleChecker checker = new BundleChecker(rules, names, this);
		checker.checkBundle(createBundle());
		assertEquals(Collections.emptyList(), messages);
	}

	private Rule addRule(ElementType elementType) {
		final Rule rule = new Rule();
		rule.setElement(elementType);
		final Limit limit = rule.createLimit();
		limit.setMinimum("0.75");
		rules.add(rule);
		return rule;
	}

	private IBundleCoverage createBundle() {
		final MethodCoverageImpl m = new MethodCoverageImpl("fooMethod", "()V",
				null);
		m.increment(CounterImpl.getInstance(5, 5), CounterImpl.COUNTER_0_0, 1);
		m.incrementMethodCounter();

		final ClassCoverageImpl c = new ClassCoverageImpl(
				"org/jacoco/example/FooClass", 1001, false);
		c.setSourceFileName("FooClass.java");
		c.addMethod(m);

		final SourceFileCoverageImpl s = new SourceFileCoverageImpl(
				"FooClass.java", "org/jacoco/example");
		s.increment(c);

		IPackageCoverage p = new PackageCoverageImpl("org/jacoco/example",
				Collections.singleton((IClassCoverage) c),
				Collections.singleton((ISourceFileCoverage) s));
		return new BundleCoverageImpl("Test", Collections.singleton(p));
	}

	private void assertMessage(String expected) {
		assertEquals(Collections.singletonList(expected), messages);
	}

	public void onViolation(ICoverageNode node, Rule rule, Limit limit,
			String message) {
		messages.add(message);
	}

}
