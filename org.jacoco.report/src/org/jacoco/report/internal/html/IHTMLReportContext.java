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
	public Resources getResources();

	/**
	 * Returns the language names call-back used in this report.
	 * 
	 * @return language names
	 */
	public ILanguageNames getLanguageNames();

	/**
	 * Returns a table for rendering coverage nodes.
	 * 
	 * @return table for rendering
	 */
	public Table getTable();

	/**
	 * Returns a string of textual information to include in every page footer.
	 * 
	 * @return footer text or empty string
	 */
	public String getFooterText();

	/**
	 * Returns the link to the sessions page.
	 * 
	 * @return sessions page link
	 */
	public ILinkable getSessionsPage();

	/**
	 * Returns the encoding of the generated HTML documents.
	 * 
	 * @return encoding for generated HTML documents
	 */
	public String getOutputEncoding();

	/**
	 * Returns the service for index updates.
	 * 
	 * @return sevice for indes updates
	 */
	public IIndexUpdate getIndexUpdate();

	/**
	 * Returns the locale used to format numbers and dates.
	 * 
	 * @return locale for numbers and dates
	 */
	public Locale getLocale();

}