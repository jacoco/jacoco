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
package org.jacoco.report.internal.html.resources;

/**
 * Constants for styles defined by the report style sheet.
 */
public final class Styles {

	/** Breadcrumb bar */
	public static final String BREADCRUMB = "breadcrumb";

	/** Info links within the Breadcrumb bar */
	public static final String INFO = "info";

	/** Footer */
	public static final String FOOTER = "footer";

	/** Text block aligned to the right */
	public static final String RIGHT = "right";

	/** Report element */
	public static final String EL_REPORT = "el_report";

	/** Sessions element */
	public static final String EL_SESSION = "el_session";

	/** Group element */
	public static final String EL_GROUP = "el_group";

	/** Bundle element */
	public static final String EL_BUNDLE = "el_bundle";

	/** Package element */
	public static final String EL_PACKAGE = "el_package";

	/** Source file element */
	public static final String EL_SOURCE = "el_source";

	/** Class element */
	public static final String EL_CLASS = "el_class";

	/** Method element */
	public static final String EL_METHOD = "el_method";

	/** Coverage table */
	public static final String COVERAGETABLE = "coverage";

	/** Table cells for a graphical bar */
	public static final String BAR = "bar";

	/** Table cells for the first column of a counter */
	public static final String CTR1 = "ctr1";

	/** Table cells for the second column of a counter */
	public static final String CTR2 = "ctr2";

	/** Table header for sortable columns */
	public static final String SORTABLE = "sortable";

	/** Table header for column sorted upwards */
	public static final String UP = "up";

	/** Table header for column sorted downwards */
	public static final String DOWN = "down";

	/** Block of source code */
	public static final String SOURCE = "source";

	/** Line number before each source line */
	public static final String NR = "nr";

	/** Part of source code where instructions are not covered */
	public static final String NOT_COVERED = "nc";

	/** Part of source code where instructions are partly covered */
	public static final String PARTLY_COVERED = "pc";

	/** Part of source code where instructions are is fully covered */
	public static final String FULLY_COVERED = "fc";

	/** Part of source code where branches are not covered */
	public static final String BRANCH_NOT_COVERED = "bnc";

	/** Part of source code where branches are partly covered */
	public static final String BRANCH_PARTLY_COVERED = "bpc";

	/** Part of source code where branches are fully covered */
	public static final String BRANCH_FULLY_COVERED = "bfc";

	/**
	 * Returns a combined style from the given styles.
	 *
	 * @param styles
	 *            list of separate styles, entries might be null
	 * @return combined style or <code>null</code> if no style is given
	 */
	public static String combine(final String... styles) {
		final StringBuilder sb = new StringBuilder();
		for (final String style : styles) {
			if (style != null) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(style);
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}

	private Styles() {
	}

}
