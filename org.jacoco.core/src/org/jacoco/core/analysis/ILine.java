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
package org.jacoco.core.analysis;

/**
 * The instruction and branch coverage of a single source line is described by
 * this interface.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: 11 $
 */
public interface ILine {

	/**
	 * Flag for lines that do not contain instructions (value is 0x00).
	 */
	public static final byte NO_CODE = 0x00;

	/**
	 * Flag for lines where no instruction or branch is covered (value is 0x01).
	 */
	public static final byte NOT_COVERED = 0x01;

	/**
	 * Flag for lines where all instructions and branches are covered (value is
	 * 0x02).
	 */
	public static final byte FULLY_COVERED = 0x02;

	/**
	 * Flag for lines where only a part of the instructions or branches are
	 * covered (value is 0x03).
	 */
	public static final byte PARTLY_COVERED = NOT_COVERED | FULLY_COVERED;

	/**
	 * Returns the coverage status of the given line.
	 * 
	 * @see #NO_CODE
	 * @see #NOT_COVERED
	 * @see #PARTLY_COVERED
	 * @see #FULLY_COVERED
	 * 
	 * @return status of this line
	 */
	public byte getStatus();

	/**
	 * Returns the instruction counter for this line.
	 * 
	 * @return instruction counter
	 */
	public ICounter getInstructionCounter();

	/**
	 * Returns the branches counter for this line.
	 * 
	 * @return branches counter
	 */
	public ICounter getBranchCounter();

}
