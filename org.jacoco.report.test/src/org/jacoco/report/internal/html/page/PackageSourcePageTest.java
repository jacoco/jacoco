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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.ILinkable;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link PackageSourcePage}.
 */
public class PackageSourcePageTest extends PageTestBase {

	private PackageCoverageImpl node;
	private ISourceFileLocator sourceLocator;
	private ILinkable packagePageLink;

	private PackageSourcePage page;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		SourceFileCoverageImpl src1 = new SourceFileCoverageImpl("Src1.java",
				"org/jacoco/example");
		src1.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 1);
		SourceFileCoverageImpl src2 = new SourceFileCoverageImpl("Src2.java",
				"org/jacoco/example");
		src2.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 1);
		node = new PackageCoverageImpl("org/jacoco/example",
				Collections.<IClassCoverage> emptyList(),
				Arrays.<ISourceFileCoverage> asList(src1, src2));
		sourceLocator = new ISourceFileLocator() {

			public int getTabWidth() {
				return 4;
			}

			public Reader getSourceFile(String packageName, String fileName)
					throws IOException {
				return fileName.equals("Src1.java") ? new StringReader("")
						: null;
			}
		};
		packagePageLink = new ILinkable() {

			public String getLinkStyle() {
				fail();
				return null;
			}

			public String getLinkLabel() {
				fail();
				return null;
			}

			public String getLink(ReportOutputFolder base) {
				return "index.html";
			}
		};
	}

	@Test
	public void testContents() throws Exception {
		page = new PackageSourcePage(node, null, sourceLocator, rootFolder,
				context, packagePageLink);
		page.render();

		final Document doc = support.parse(output.getFile("index.source.html"));
		assertEquals("index.html",
				support.findStr(doc, "/html/body/div[1]/span[1]/a/@href"));
		assertEquals("el_class",
				support.findStr(doc, "/html/body/div[1]/span[1]/a/@class"));
		assertEquals("Classes",
				support.findStr(doc, "/html/body/div[1]/span[1]/a"));
		assertEquals("el_source", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/a/@class"));
		assertEquals("Src1.java", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/a"));
		assertEquals("el_source", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[2]/td[1]/span/@class"));
		assertEquals("Src2.java", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[2]/td[1]/span"));
	}

	@Test
	public void should_render_non_empty_sources() throws Exception {
		final ISourceFileCoverage emptySource = new SourceFileCoverageImpl(
				"Empty.java", "example");
		final SourceFileCoverageImpl nonEmptySource = new SourceFileCoverageImpl(
				"NonEmpty.java", "example");
		nonEmptySource.increment(CounterImpl.COUNTER_1_0,
				CounterImpl.COUNTER_0_0, 1);
		node = new PackageCoverageImpl("example",
				Collections.<IClassCoverage> emptyList(),
				Arrays.asList(emptySource, nonEmptySource));

		page = new PackageSourcePage(node, null, sourceLocator, rootFolder,
				context, packagePageLink);
		page.render();

		final Document doc = support.parse(output.getFile("index.source.html"));
		assertEquals("NonEmpty.java", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/span"));
		assertEquals("1",
				support.findStr(doc, "count(/html/body/table[1]/tbody/tr)"));
	}

	@Test
	public void testGetSourceFilePages() throws Exception {
		page = new PackageSourcePage(node, null, sourceLocator, rootFolder,
				context, packagePageLink);
		page.render();

		assertNotNull(page.getSourceFilePage("Src1.java"));
		assertNull(page.getSourceFilePage("Src2.java"));
	}

}
