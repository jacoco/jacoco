/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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
 * The instruction and branch coverage of a single source line is described by
 * this interface.
 */
public interface ILine {

	/**
	 * Returns the instruction counter for this line.
	 * 
	 * @return instruction counter
	 */
	ICounter getInstructionCounter();

	/**
	 * Returns the branches counter for this line.
	 * 
	 * @return branches counter
	 */
	ICounter getBranchCounter();

	/**
	 * Returns the coverage status of this line, calculated from the
	 * instructions counter and branch counter.
	 * 
	 * @see ICounter#EMPTY
	 * @see ICounter#NOT_COVERED
	 * @see ICounter#PARTLY_COVERED
	 * @see ICounter#FULLY_COVERED
	 * 
	 * @return status of this line
	 */
	int getStatus();

}
