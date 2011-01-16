/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal.html;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.MemoryMultiReportOutput;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.internal.html.index.IIndexUpdate;
import org.jacoco.report.internal.html.resources.Resources;
import org.jacoco.report.internal.html.table.Table;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link PackagePage}.
 */
public class PackagePageTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private IHTMLReportContext context;

	private PackagePage page;

	@Before
	public void setup() {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
		context = new IHTMLReportContext() {

			public ILanguageNames getLanguageNames() {
				throw new AssertionError("Unexpected method call.");
			}

			public Resources getResources() {
				throw new AssertionError("Unexpected method call.");
			}

			public Table getTable() {
				throw new AssertionError("Unexpected method call.");
			}

			public String getFooterText() {
				throw new AssertionError("Unexpected method call.");
			}

			public ILinkable getSessionsPage() {
				throw new AssertionError("Unexpected method call.");
			}

			public String getOutputEncoding() {
				throw new AssertionError("Unexpected method call.");
			}

			public IIndexUpdate getIndexUpdate() {
				throw new AssertionError("Unexpected method call.");
			}

			public Locale getLocale() {
				throw new AssertionError("Unexpected method call.");
			}
		};
		Collection<IClassCoverage> classes = Collections.emptyList();
		Collection<ISourceFileCoverage> sources = Collections.emptyList();
		final IPackageCoverage node = new PackageCoverageImpl("foo", classes,
				sources);
		page = new PackagePage(node, null, root, context);
	}

	@Test(expected = AssertionError.class)
	public void testVisitChildNegative1() {
		page.visitChild(new CoverageNodeImpl(ElementType.GROUP, "Foo"));
	}

	@Test(expected = AssertionError.class)
	public void testVisitChildNegative2() {
		page.visitChild(new CoverageNodeImpl(ElementType.BUNDLE, "Foo"));
	}

	@Test(expected = AssertionError.class)
	public void testVisitChildNegative3() {
		page.visitChild(new CoverageNodeImpl(ElementType.PACKAGE, "Foo"));
	}

	@Test(expected = AssertionError.class)
	public void testVisitChildNegative4() {
		page.visitChild(new CoverageNodeImpl(ElementType.METHOD, "Foo"));
	}

}
