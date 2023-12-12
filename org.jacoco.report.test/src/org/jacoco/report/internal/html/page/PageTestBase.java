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

import java.io.IOException;
import java.util.Locale;

import org.jacoco.report.ILanguageNames;
import org.jacoco.report.JavaNames;
import org.jacoco.report.MemoryMultiReportOutput;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLSupport;
import org.jacoco.report.internal.html.IHTMLReportContext;
import org.jacoco.report.internal.html.ILinkable;
import org.jacoco.report.internal.html.LinkableStub;
import org.jacoco.report.internal.html.index.IIndexUpdate;
import org.jacoco.report.internal.html.resources.Resources;
import org.jacoco.report.internal.html.resources.Styles;
import org.jacoco.report.internal.html.table.LabelColumn;
import org.jacoco.report.internal.html.table.Table;
import org.junit.After;

/**
 * Unit tests for {@link ReportPage}.
 */
public abstract class PageTestBase {

	protected MemoryMultiReportOutput output;

	protected ReportOutputFolder rootFolder;

	protected IHTMLReportContext context;

	protected HTMLSupport support;

	protected void setup() throws Exception {
		output = new MemoryMultiReportOutput();
		rootFolder = new ReportOutputFolder(output);
		final Resources resources = new Resources(rootFolder);
		final Table table = new Table();
		table.add("Element", null, new LabelColumn(), true);
		context = new IHTMLReportContext() {

			public ILanguageNames getLanguageNames() {
				return new JavaNames();
			}

			public Resources getResources() {
				return resources;
			}

			public Table getTable() {
				return table;
			}

			public String getFooterText() {
				return "CustomFooter";
			}

			public ILinkable getSessionsPage() {
				return new LinkableStub("sessions.html", "Sessions",
						Styles.EL_SESSION);
			}

			public String getOutputEncoding() {
				return "UTF-8";
			}

			public IIndexUpdate getIndexUpdate() {
				return new IIndexUpdate() {
					public void addClass(ILinkable link, long classid) {
					}
				};
			}

			public Locale getLocale() {
				return Locale.ENGLISH;
			}

		};
		support = new HTMLSupport();
	}

	@After
	public void teardown() throws IOException {
		output.close();
		output.assertAllClosed();
	}

}
