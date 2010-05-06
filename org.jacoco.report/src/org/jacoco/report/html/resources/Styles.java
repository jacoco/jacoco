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
package org.jacoco.report.html.resources;

/**
 * Constants for styles defined by the report style sheet.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface Styles {

	/** Breadcrumb bar */
	public static final String BREADCRUMB = "breadcrumb";

	/** Footer */
	public static final String FOOTER = "footer";

	/** Test block aligned to the right */
	public static final String RIGHT = "right";

	/** Report element */
	public static final String EL_REPORT = "el_report";

	/** Sessions element */
	public static final String EL_SESSIONS = "el_sessions";

	/** Coverage table */
	public static final String COVERAGETABLE = "coverage";

	/** Table cells for the first column of a counter */
	public static final String CTR1 = "ctr1";

	/** Table cells for the second column of a counter */
	public static final String CTR2 = "ctr2";

	/** Block of source code */
	public static final String SOURCE = "source";

	/** Line number before each source line */
	public static final String NR = "nr";

	/** Part of source code that is not covered */
	public static final String NOT_COVERED = "nc";

	/** Part of source code that is partly covered */
	public static final String PARTLY_COVERED = "pc";

	/** Part of source code that is fully covered */
	public static final String FULLY_COVERED = "fc";

}
