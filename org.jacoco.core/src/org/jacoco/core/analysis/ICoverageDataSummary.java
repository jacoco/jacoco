/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
package org.jacoco.core.analysis;

/**
 * Interface for summary with different coverage counters.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface ICoverageDataSummary {

	/**
	 * Returns the counter for byte code instructions.
	 * 
	 * @return counter for instructions
	 */
	public abstract ICounter getInstructionCounter();

	/**
	 * Returns the counter for blocks.
	 * 
	 * @return counter for blocks
	 */
	public abstract ICounter getBlockCounter();

	/**
	 * Returns the counter for lines.
	 * 
	 * @return counter for lines
	 */
	public abstract ICounter getLineCounter();

	/**
	 * Returns the counter for methods.
	 * 
	 * @return counter for methods
	 */
	public abstract ICounter getMethodCounter();

	/**
	 * Returns the counter for types.
	 * 
	 * @return counter for types
	 */
	public abstract ICounter getClassCounter();

}