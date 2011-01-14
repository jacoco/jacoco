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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.JavaNames;
import org.jacoco.report.MemoryMultiReportOutput;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.internal.html.index.ElementIndex;
import org.jacoco.report.internal.html.index.IIndexUpdate;
import org.jacoco.report.internal.html.resources.Resources;
import org.jacoco.report.internal.html.resources.Styles;
import org.jacoco.report.internal.html.table.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link SessionsPage}.
 */
public class SessionsPageTest {

	private final List<SessionInfo> noSessions = Collections.emptyList();

	private final Collection<ExecutionData> noExecutionData = Collections
			.emptyList();

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private ElementIndex index;

	private IHTMLReportContext context;

	@Before
	public void setup() {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
		index = new ElementIndex(root);
		final Resources resources = new Resources(root);
		final ILanguageNames names = new JavaNames();
		context = new IHTMLReportContext() {

			public ILanguageNames getLanguageNames() {
				return names;
			}

			public Resources getResources() {
				return resources;
			}

			public Table getTable() {
				throw new AssertionError("Unexpected method call.");
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
				return index;
			}

			public Locale getLocale() {
				return Locale.ENGLISH;
			}
		};
	}

	@After
	public void teardown() {
		output.assertAllClosed();
	}

	@Test
	public void testGetLinkStyle() {
		final SessionsPage page = new SessionsPage(noSessions, noExecutionData,
				index, null, root, context);
		assertEquals("el_session", page.getLinkStyle());
	}

	@Test
	public void testGetFileName() {
		final SessionsPage page = new SessionsPage(noSessions, noExecutionData,
				index, null, root, context);
		assertEquals(".sessions.html", page.getFileName());
	}

	@Test
	public void testGetLinkLabel() {
		final SessionsPage page = new SessionsPage(noSessions, noExecutionData,
				index, null, root, context);
		assertEquals("Sessions", page.getLinkLabel());
	}

	@Test
	public void testEmptyContent() throws Exception {
		final SessionsPage page = new SessionsPage(noSessions, noExecutionData,
				index, null, root, context);
		page.renderDocument();
		final HTMLSupport support = new HTMLSupport();
		final Document doc = support.parse(output.getFile(".sessions.html"));
		assertEquals("No session information available.",
				support.findStr(doc, "/html/body/p[1]"));
		assertEquals("No execution data available.",
				support.findStr(doc, "/html/body/p[2]"));
	}

	@Test
	public void testSessionListContent() throws Exception {
		final List<SessionInfo> sessions = new ArrayList<SessionInfo>();
		sessions.add(new SessionInfo("Session-A", 0, 0));
		sessions.add(new SessionInfo("Session-B", 0, 0));
		sessions.add(new SessionInfo("Session-C", 0, 0));
		final SessionsPage page = new SessionsPage(sessions, noExecutionData,
				index, null, root, context);
		page.renderDocument();
		final HTMLSupport support = new HTMLSupport();
		final Document doc = support.parse(output.getFile(".sessions.html"));
		assertEquals("el_session", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/span/@class"));
		assertEquals("Session-A", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/span"));
		assertEquals("Session-B",
				support.findStr(doc, "/html/body/table[1]/tbody/tr[2]/td[1]"));
		assertEquals("Session-C",
				support.findStr(doc, "/html/body/table[1]/tbody/tr[3]/td[1]"));
	}

	@Test
	public void testExecutionDataContent() throws Exception {
		final Collection<ExecutionData> data = new ArrayList<ExecutionData>();
		data.add(new ExecutionData(0x1000, "ClassB", new boolean[0]));
		data.add(new ExecutionData(0x1001, "ClassC", new boolean[0]));
		data.add(new ExecutionData(0x1002, "ClassA", new boolean[0]));
		index.addClass(new ReportPage(null, root, context) {

			public String getLinkLabel() {
				return "Foo";
			}

			@Override
			protected String getFileName() {
				return "Foo.html";
			}

			public String getLinkStyle() {
				return "sample";
			}

			@Override
			protected void content(HTMLElement body) throws IOException {
			}
		}, 0x1002);

		final SessionsPage page = new SessionsPage(noSessions, data, index,
				null, root, context);
		page.renderDocument();
		final HTMLSupport support = new HTMLSupport();
		final Document doc = support.parse(output.getFile(".sessions.html"));
		assertEquals("el_class", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/a/@class"));
		assertEquals("Foo.html", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/a/@href"));
		assertEquals("ClassA",
				support.findStr(doc, "/html/body/table[1]/tbody/tr[1]/td[1]/a"));
		assertEquals("0000000000001002", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[2]/code"));
		assertEquals("ClassB",
				support.findStr(doc, "/html/body/table[1]/tbody/tr[2]/td[1]"));
		assertEquals("ClassC",
				support.findStr(doc, "/html/body/table[1]/tbody/tr[3]/td[1]"));
	}

}
