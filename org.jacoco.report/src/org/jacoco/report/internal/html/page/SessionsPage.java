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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.IHTMLReportContext;
import org.jacoco.report.internal.html.index.ElementIndex;
import org.jacoco.report.internal.html.resources.Styles;

/**
 * Page to display information about sessions covered by this report.
 */
public class SessionsPage extends ReportPage {

	private static final String MSG_SESSIONS = "This coverage report is based "
			+ "on execution data from the following sessions:";

	private static final String MSG_NO_SESSIONS = "No session information available.";

	private static final String MSG_EXECDATA = "Execution data for the "
			+ "following classes is considered in this report:";

	private static final String MSG_NO_EXECDATA = "No execution data available.";

	private final List<SessionInfo> sessionInfos;

	private final DateFormat dateFormat;

	private final List<ExecutionData> executionData;

	private final ElementIndex index;

	/**
	 * Creates a new page page to display session information.
	 *
	 * @param sessionInfos
	 *            session info objects
	 * @param executionData
	 *            execution data objects
	 * @param index
	 *            index for cross-linking
	 * @param parent
	 *            optional hierarchical parent
	 * @param folder
	 *            base folder to create this page in
	 * @param context
	 *            settings context
	 */
	public SessionsPage(final List<SessionInfo> sessionInfos,
			final Collection<ExecutionData> executionData,
			final ElementIndex index, final ReportPage parent,
			final ReportOutputFolder folder, final IHTMLReportContext context) {
		super(parent, folder, context);
		this.sessionInfos = sessionInfos;
		this.executionData = new ArrayList<ExecutionData>(executionData);
		this.index = index;
		this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
				DateFormat.DEFAULT, context.getLocale());
		final ILanguageNames names = context.getLanguageNames();
		Collections.sort(this.executionData, new Comparator<ExecutionData>() {
			public int compare(final ExecutionData e1, final ExecutionData e2) {
				return names.getQualifiedClassName(e1.getName())
						.compareTo(names.getQualifiedClassName(e2.getName()));
			}
		});
	}

	@Override
	protected void content(final HTMLElement body) throws IOException {
		if (sessionInfos.isEmpty()) {
			body.p().text(MSG_NO_SESSIONS);
		} else {
			body.p().text(MSG_SESSIONS);
			sessionTable(body);
		}
		if (executionData.isEmpty()) {
			body.p().text(MSG_NO_EXECDATA);
		} else {
			body.p().text(MSG_EXECDATA);
			executionDataTable(body);
		}
	}

	private void sessionTable(final HTMLElement body) throws IOException {
		final HTMLElement table = body.table(Styles.COVERAGETABLE);
		{
			final HTMLElement tr = table.thead().tr();
			tr.td().text("Session");
			tr.td().text("Start Time");
			tr.td().text("Dump Time");
		}
		final HTMLElement tbody = table.tbody();
		for (final SessionInfo i : sessionInfos) {
			final HTMLElement tr = tbody.tr();
			tr.td().span(Styles.EL_SESSION).text(i.getId());
			tr.td().text(dateFormat.format(new Date(i.getStartTimeStamp())));
			tr.td().text(dateFormat.format(new Date(i.getDumpTimeStamp())));
		}
	}

	private void executionDataTable(final HTMLElement body) throws IOException {
		final HTMLElement table = body.table(Styles.COVERAGETABLE);
		{
			final HTMLElement tr = table.thead().tr();
			tr.td().text("Class");
			tr.td().text("Id");
		}
		final HTMLElement tbody = table.tbody();
		final ILanguageNames names = context.getLanguageNames();
		for (final ExecutionData e : executionData) {
			final HTMLElement tr = tbody.tr();
			final String link = index.getLinkToClass(e.getId());
			final String qualifiedName = names
					.getQualifiedClassName(e.getName());
			if (link == null) {
				tr.td().span(Styles.EL_CLASS).text(qualifiedName);
			} else {
				tr.td().a(link, Styles.EL_CLASS).text(qualifiedName);
			}
			final String id = String.format("%016x", Long.valueOf(e.getId()));
			tr.td().code().text(id);
		}
	}

	@Override
	protected String getFileName() {
		return "jacoco-sessions.html";
	}

	public String getLinkStyle() {
		return Styles.EL_SESSION;
	}

	public String getLinkLabel() {
		return "Sessions";
	}

}
