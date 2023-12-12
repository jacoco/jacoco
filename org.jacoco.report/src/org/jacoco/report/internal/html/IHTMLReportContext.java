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
package org.jacoco.report.internal.html;

import java.util.Locale;

import org.jacoco.report.ILanguageNames;
import org.jacoco.report.internal.html.index.IIndexUpdate;
import org.jacoco.report.internal.html.resources.Resources;
import org.jacoco.report.internal.html.table.Table;

/**
 * Context and configuration information during creation of a HTML report.
 */
public interface IHTMLReportContext {

	/**
	 * Returns the static resources used in this report.
	 *
	 * @return static resources
	 */
	Resources getResources();

	/**
	 * Returns the language names call-back used in this report.
	 *
	 * @return language names
	 */
	ILanguageNames getLanguageNames();

	/**
	 * Returns a table for rendering coverage nodes.
	 *
	 * @return table for rendering
	 */
	Table getTable();

	/**
	 * Returns a string of textual information to include in every page footer.
	 *
	 * @return footer text or empty string
	 */
	String getFooterText();

	/**
	 * Returns the link to the sessions page.
	 *
	 * @return sessions page link
	 */
	ILinkable getSessionsPage();

	/**
	 * Returns the encoding of the generated HTML documents.
	 *
	 * @return encoding for generated HTML documents
	 */
	String getOutputEncoding();

	/**
	 * Returns the service for index updates.
	 *
	 * @return sevice for indes updates
	 */
	IIndexUpdate getIndexUpdate();

	/**
	 * Returns the locale used to format numbers and dates.
	 *
	 * @return locale for numbers and dates
	 */
	Locale getLocale();

}
