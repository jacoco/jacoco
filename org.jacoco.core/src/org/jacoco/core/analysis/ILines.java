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
 * For all elements that are located in a source file and for source files
 * itself individual line coverage is described by this interface.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: 11 $
 */
public interface ILines extends ICounter {

	/** Flag for lines that do not contain code (value is 0x00). */
	public static final byte NO_CODE = 0x00;

	/** Flag for lines that are not covered (value is 0x01). */
	public static final byte NOT_COVERED = 0x01;

	/** Flag for lines that are fully covered (value is 0x02). */
	public static final byte FULLY_COVERED = 0x02;

	/** Flag for lines that are partly covered (value is 0x03). */
	public static final byte PARTLY_COVERED = NOT_COVERED | FULLY_COVERED;

	/**
	 * The number of the first line coverage information is available for. If no
	 * line is yet contained, the method returns -1.
	 * 
	 * @return number of the first line or -1
	 */
	public int getFirstLine();

	/**
	 * The number of the last line coverage information is available for. If no
	 * line is yet contained, the method returns -1.
	 * 
	 * @return number of the last line or -1
	 */
	public int getLastLine();

	/**
	 * Returns the coverage status of the given line.
	 * 
	 * @see #NO_CODE
	 * @see #NOT_COVERED
	 * @see #PARTLY_COVERED
	 * @see #FULLY_COVERED
	 * 
	 * @param line
	 *            number of the line in question
	 * @return status of this line
	 */
	public byte getStatus(int line);

}
