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

import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.html.index.IIndexUpdate;
import org.jacoco.report.html.resources.Resources;

/**
 * Context and configuration information during creation of a HTML report.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
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
	 * Returns a table for rendering of the given type.
	 * 
	 * @param type
	 *            element type
	 * @return table for rendering
	 */
	public CoverageTable getTable(final ElementType type);

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

}