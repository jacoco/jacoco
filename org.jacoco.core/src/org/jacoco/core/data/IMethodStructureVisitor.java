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
 *******************************************************************************/
package org.jacoco.core.data;

/**
 * Interface for data output of the internal structure of a single method. This
 * interface is meant to be implemented by parties that want to retrieve data
 * from the instrumentation process.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public interface IMethodStructureVisitor {

	/** Place holder for unknown lines (no debug information) */
	public static int UNKNOWN_LINE = -1;

	/**
	 * Called for every instruction.
	 * 
	 * @param covered
	 *            <code>true</code> if the instruction has been executed
	 * @param line
	 *            source line number of the instruction
	 */
	public void visitInsn(boolean covered, int line);

	/**
	 * Called for every branching point.
	 * 
	 * @param missed
	 *            number of missed branches
	 * @param covered
	 *            number of covered branches
	 * @param line
	 *            source line number of the instruction
	 */
	public void visitBranches(int missed, int covered, int line);

	/**
	 * Signals the end of this method structure.
	 */
	public void visitEnd();

}
