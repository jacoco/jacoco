/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.report.StubLocator;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link PackagePage}.
 */
public class PackagePageTest extends PageTestBase {

	private StubLocator locator;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		locator = new StubLocator();
		locator.put("org/jacoco/example/Foo.java", "class Foo {}");
	}

	private void renderPackagePage(boolean linkToSource,
			boolean addSourceFileCoverage) throws IOException {
		ClassCoverageImpl classCoverage = new ClassCoverageImpl(
				"org/jacoco/example/Foo", 123, null, "java/lang/Object", null);
		classCoverage.setSourceFileName("Foo.java");
		classCoverage.addMethod(new MethodCoverageImpl("a", "()V", null));
		List<ISourceFileCoverage> sourceCoverageList = new ArrayList<ISourceFileCoverage>();
		if (addSourceFileCoverage) {
			SourceFileCoverageImpl sourceCoverage = new SourceFileCoverageImpl(
					"Foo.java", "org/jacoco/example");
			sourceCoverageList.add(sourceCoverage);
		}
		PackageCoverageImpl node = new PackageCoverageImpl(
				"org/jacoco/example",
				Arrays.asList((IClassCoverage) classCoverage),
				sourceCoverageList);
		PackagePage page = new PackagePage(node, null, locator, rootFolder,
				linkToSource, context);
		page.render();
	}

	private String getLink() throws Exception {
		final Document doc = support.parse(output.getFile("index.html"));
		return support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/a/@href");
	}

	@Test
	public void testContentsWithoutLinkingToSource() throws Exception {
		renderPackagePage(false, false);
		assertEquals("Foo.html", getLink());
	}

	@Test
	public void testContentsWithoutLinkingToSourceButSourceIsAvailable()
			throws Exception {
		renderPackagePage(false, true);
		assertEquals("Foo.html", getLink());
	}

	@Test
	public void testContentsLinkingToSourceWhenSourceIsAvailable()
			throws Exception {
		renderPackagePage(true, true);
		assertEquals("Foo.java.html", getLink());
	}

	@Test
	public void testContentsLinkingToSourceWhenSourceIsNotAvailable()
			throws Exception {
		renderPackagePage(true, false);
		assertEquals("Foo.html", getLink());
	}

}
