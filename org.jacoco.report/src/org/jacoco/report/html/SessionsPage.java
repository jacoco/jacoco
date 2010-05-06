/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.html;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Styles;

/**
 * Page to display information about sessions covered by this report.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class SessionsPage extends ReportPage {

	private static final String MSG_SESSIONS = "This coverage report is based "
			+ "on execution data from the following sessions:";

	private final List<SessionInfo> sessionInfos;

	private final DateFormat dateFormat = DateFormat.getDateTimeInstance();

	/**
	 * Creates a new page page to display session information.
	 * 
	 * @param sessionInfos
	 * @param parent
	 * @param folder
	 * @param context
	 */
	public SessionsPage(final List<SessionInfo> sessionInfos,
			final ReportPage parent, final ReportOutputFolder folder,
			final IHTMLReportContext context) {
		super(parent, folder, context);
		this.sessionInfos = sessionInfos;
	}

	@Override
	protected void content(final HTMLElement body) throws IOException {
		body.p().text(MSG_SESSIONS);
		final HTMLElement table = body.table(Styles.COVERAGETABLE);
		tableheader(table.thead());
		tablebody(table.tbody());
	}

	private void tableheader(final HTMLElement thead) throws IOException {
		final HTMLElement tr = thead.tr();
		tr.td().text("Session");
		tr.td().text("Start Time");
		tr.td().text("Dump Time");
	}

	private void tablebody(final HTMLElement tbody) throws IOException {
		for (final SessionInfo i : sessionInfos) {
			final HTMLElement tr = tbody.tr();
			tr.td().text(i.getId());
			tr.td().text(dateFormat.format(new Date(i.getStartTimeStamp())));
			tr.td().text(dateFormat.format(new Date(i.getDumpTimeStamp())));
		}
	}

	@Override
	protected String getElementStyle() {
		return Styles.EL_SESSIONS;
	}

	@Override
	protected String getFileName() {
		return ".sessions.html";
	}

	@Override
	protected String getLabel() {
		return "Sessions";
	}

}
