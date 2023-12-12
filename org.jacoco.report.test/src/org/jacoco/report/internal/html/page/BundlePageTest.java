/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.internal.html.page;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link BundlePage}.
 */
public class BundlePageTest extends PageTestBase {

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
	}

	@Test
	public void should_render_non_empty_packages() throws Exception {
		final ClassCoverageImpl classCoverage = new ClassCoverageImpl(
				"example/Class", 0, false);
		final MethodCoverageImpl methodCoverage = new MethodCoverageImpl("m",
				"()V", null);
		methodCoverage.increment(CounterImpl.COUNTER_1_0,
				CounterImpl.COUNTER_0_0, 42);
		classCoverage.addMethod(methodCoverage);
		final IPackageCoverage nonEmptyPackage = new PackageCoverageImpl(
				"example",
				Collections.<IClassCoverage> singleton(classCoverage),
				Collections.<ISourceFileCoverage> emptySet());

		final IPackageCoverage emptyPackage = new PackageCoverageImpl("empty",
				Collections.<IClassCoverage> emptySet(),
				Collections.<ISourceFileCoverage> emptySet());

		final IBundleCoverage node = new BundleCoverageImpl("bundle",
				Arrays.asList(nonEmptyPackage, emptyPackage));

		final BundlePage page = new BundlePage(node, null, null, rootFolder,
				context);
		page.render();

		final Document doc = support.parse(output.getFile("index.html"));
		assertEquals("el_package", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/a/@class"));
		assertEquals("example", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/a"));
		assertEquals("1",
				support.findStr(doc, "count(/html/body/table[1]/tbody/tr)"));
	}

	@Test
	public void should_render_message_when_no_class_files_specified()
			throws Exception {
		final IBundleCoverage node = new BundleCoverageImpl("bundle",
				Collections.<IPackageCoverage> emptySet());

		final BundlePage page = new BundlePage(node, null, null, rootFolder,
				context);
		page.render();

		final Document doc = support.parse(output.getFile("index.html"));
		assertEquals("No class files specified.",
				support.findStr(doc, "/html/body/p"));
	}

	@Test
	public void should_render_message_when_all_classes_empty()
			throws Exception {
		final ClassCoverageImpl emptyClass = new ClassCoverageImpl(
				"example/Class", 0, false);
		final IBundleCoverage node = new BundleCoverageImpl("bundle",
				Collections.<IClassCoverage> singleton(emptyClass),
				Collections.<ISourceFileCoverage> emptySet());

		final BundlePage page = new BundlePage(node, null, null, rootFolder,
				context);
		page.render();

		final Document doc = support.parse(output.getFile("index.html"));
		assertEquals(
				"None of the analyzed classes contain code relevant for code coverage.",
				support.findStr(doc, "/html/body/p"));
	}

}
