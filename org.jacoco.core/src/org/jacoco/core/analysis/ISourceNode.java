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
package org.jacoco.core.analysis;

/**
 * Interface for coverage nodes that have individual source lines like methods,
 * classes and source files.
 */
public interface ISourceNode extends ICoverageNode {

	/** Place holder for unknown lines (no debug information) */
	public static int UNKNOWN_LINE = -1;

	/**
	 * The number of the first line coverage information is available for. If no
	 * line is contained, the method returns -1.
	 * 
	 * @return number of the first line or {@link #UNKNOWN_LINE}
	 */
	public int getFirstLine();

	/**
	 * The number of the last line coverage information is available for. If no
	 * line is contained, the method returns -1.
	 * 
	 * @return number of the last line or {@link #UNKNOWN_LINE}
	 */
	public int getLastLine();

	/**
	 * Returns the line information for given line.
	 * 
	 * @param nr
	 *            line number of interest
	 * @return line information
	 */
	public ILine getLine(int nr);

	/**
	 * Has any EBigOFunction been set on this node or any of its lines.
	 * 
	 * @return {@code true} if any EBigOFunction been set on this node or any of
	 *         its lines; Otherwise, {@code false}
	 */
	public boolean hasEBigO();

	/**
	 * Returns the results of an E-Big-O analysis a line in this node. This
	 * value is populated by the optional Empirical-Big-O analysis step.
	 * 
	 * @param nr
	 *            line number of interest
	 * @return the results of an E-Big-O analysis on this node. If the analysis
	 *         failed or was not performed the function returned will have a
	 *         type of <code>Undefined</code>
	 */
	public EBigOFunction getLineEBigOFunction(final int nr);

}
