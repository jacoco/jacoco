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
package org.jacoco.report.internal.html.page;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.report.ISourceFileLocator;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link PackagePage}.
 */
public class PackagePageTest extends PageTestBase {

	private IPackageCoverage node;
	private ISourceFileLocator sourceLocator;

	private PackagePage page;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		sourceLocator = new ISourceFileLocator() {

			public int getTabWidth() {
				return 4;
			}

			public Reader getSourceFile(String packageName, String fileName)
					throws IOException {
				return null;
			}
		};
	}

	@Test
	public void should_render_non_empty_classes() throws Exception {
		final ClassCoverageImpl nonEmptyClass = new ClassCoverageImpl(
				"example/NonEmptyClass", 0, false);
		final MethodCoverageImpl nonEmptyMethod = new MethodCoverageImpl("m",
				"()V", null);
		nonEmptyMethod.increment(CounterImpl.COUNTER_1_0,
				CounterImpl.COUNTER_0_0, 42);
		nonEmptyClass.addMethod(nonEmptyMethod);
		final ClassCoverageImpl emptyClass = new ClassCoverageImpl(
				"example/EmptyClass", 0, false);

		node = new PackageCoverageImpl("example",
				Arrays.<IClassCoverage> asList(emptyClass, nonEmptyClass),
				Collections.<ISourceFileCoverage> emptySet());

		page = new PackagePage(node, null, sourceLocator, rootFolder, context);
		page.render();

		final Document doc = support.parse(output.getFile("index.html"));
		assertEquals("NonEmptyClass", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/a"));
		assertEquals("1",
				support.findStr(doc, "count(/html/body/table[1]/tbody/tr)"));
	}

	@Test
	public void testContentsWithSource() throws Exception {
		ClassCoverageImpl class1 = new ClassCoverageImpl(
				"org/jacoco/example/Foo1", 0x1000, false);
		MethodCoverageImpl method1 = new MethodCoverageImpl("m", "()V", null);
		method1.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 42);
		class1.addMethod(method1);
		ClassCoverageImpl class2 = new ClassCoverageImpl(
				"org/jacoco/example/Foo2", 0x2000, false);
		MethodCoverageImpl method2 = new MethodCoverageImpl("m", "()V", null);
		method2.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 42);
		class2.addMethod(method2);
		ISourceFileCoverage src1 = new SourceFileCoverageImpl("Src1.java",
				"org/jacoco/example");
		node = new PackageCoverageImpl("org/jacoco/example",
				Arrays.<IClassCoverage> asList(class1, class2),
				Arrays.asList(src1));

		page = new PackagePage(node, null, sourceLocator, rootFolder, context);
		page.render();

		final Document doc = support.parse(output.getFile("index.html"));

		// Expect "Source Files" links
		assertEquals("index.source.html",
				support.findStr(doc, "/html/body/div[1]/span[1]/a/@href"));
		assertEquals("el_source",
				support.findStr(doc, "/html/body/div[1]/span[1]/a/@class"));
		assertEquals("Source Files",
				support.findStr(doc, "/html/body/div[1]/span[1]/a"));
		assertEquals("el_class", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/a/@class"));
		assertEquals("Foo1", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/a"));
		assertEquals("el_class", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[2]/td[1]/a/@class"));
		assertEquals("Foo2", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[2]/td[1]/a"));

		output.assertFile("index.source.html");
	}

	@Test
	public void testContentsNoSource() throws Exception {
		ClassCoverageImpl class1 = new ClassCoverageImpl(
				"org/jacoco/example/Foo1", 0x1000, false);
		MethodCoverageImpl method1 = new MethodCoverageImpl("m", "()V", null);
		method1.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 42);
		class1.addMethod(method1);
		ClassCoverageImpl class2 = new ClassCoverageImpl(
				"org/jacoco/example/Foo2", 0x2000, false);
		MethodCoverageImpl method2 = new MethodCoverageImpl("m", "()V", null);
		method2.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 42);
		class2.addMethod(method2);
		node = new PackageCoverageImpl("org/jacoco/example",
				Arrays.<IClassCoverage> asList(class1, class2),
				Collections.<ISourceFileCoverage> emptyList());

		page = new PackagePage(node, null, sourceLocator, rootFolder, context);
		page.render();

		// Expect no "Source Files" link
		final Document doc = support.parse(output.getFile("index.html"));
		assertEquals("Sessions",
				support.findStr(doc, "/html/body/div[1]/span[1]/a"));

		// Expect no source files page:
		output.assertNoFile("index.source.html");
	}

}
