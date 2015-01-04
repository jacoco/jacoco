/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.report.ISourceFileLocator;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link PackageSourcePage}.
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
	public void testContentsWithSource() throws Exception {
		IClassCoverage class1 = new ClassCoverageImpl(
				"org/jacoco/example/Foo1", 0x1000, false, null,
				"java/lang/Object", null);
		IClassCoverage class2 = new ClassCoverageImpl(
				"org/jacoco/example/Foo2", 0x2000, false, null,
				"java/lang/Object", null);
		ISourceFileCoverage src1 = new SourceFileCoverageImpl("Src1.java",
				"org/jacoco/example");
		node = new PackageCoverageImpl("org/jacoco/example", Arrays.asList(
				class1, class2), Arrays.asList(src1));

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
		assertEquals("Foo1",
				support.findStr(doc, "/html/body/table[1]/tbody/tr[1]/td[1]/a"));
		assertEquals("el_class", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[2]/td[1]/a/@class"));
		assertEquals("Foo2",
				support.findStr(doc, "/html/body/table[1]/tbody/tr[2]/td[1]/a"));

		output.assertFile("index.source.html");
	}

	@Test
	public void testContentsNoSource() throws Exception {
		IClassCoverage class1 = new ClassCoverageImpl(
				"org/jacoco/example/Foo1", 0x1000, false, null,
				"java/lang/Object", null);
		IClassCoverage class2 = new ClassCoverageImpl(
				"org/jacoco/example/Foo2", 0x2000, false, null,
				"java/lang/Object", null);
		node = new PackageCoverageImpl("org/jacoco/example", Arrays.asList(
				class1, class2), Collections.<ISourceFileCoverage> emptyList());

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
