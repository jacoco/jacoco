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
