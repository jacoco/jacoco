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
package org.jacoco.core.analysis;

/**
 * Interface for coverage nodes that have individual source lines like methods,
 * classes and source files.
 */
public interface ISourceNode extends ICoverageNode {

	/** Place holder for unknown lines (no debug information) */
	int UNKNOWN_LINE = -1;

	/**
	 * The number of the first line coverage information is available for. If no
	 * line is contained, the method returns -1.
	 *
	 * @return number of the first line or {@link #UNKNOWN_LINE}
	 */
	int getFirstLine();

	/**
	 * The number of the last line coverage information is available for. If no
	 * line is contained, the method returns -1.
	 *
	 * @return number of the last line or {@link #UNKNOWN_LINE}
	 */
	int getLastLine();

	/**
	 * Returns the line information for given line.
	 *
	 * @param nr
	 *            line number of interest
	 * @return line information
	 */
	ILine getLine(int nr);

}
