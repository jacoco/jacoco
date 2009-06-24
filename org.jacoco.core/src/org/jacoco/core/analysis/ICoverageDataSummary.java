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
	 * Parameter type for generic counter access.
	 */
	public enum CounterEntity {
		/** counter for instructions */
		INSTRUCTION,

		/** counter for basic blocks */
		BLOCK,
		/** counter for source lines */
		LINE,

		/** counter for methods */
		METHOD,

		/** counter for classes */
		CLASS
	}

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
	public ICounter getBlockCounter();

	/**
	 * Returns the counter for lines.
	 * 
	 * @return counter for lines
	 */
	public ICounter getLineCounter();

	/**
	 * Returns the counter for methods.
	 * 
	 * @return counter for methods
	 */
	public ICounter getMethodCounter();

	/**
	 * Returns the counter for types.
	 * 
	 * @return counter for types
	 */
	public ICounter getClassCounter();

	/**
	 * Generic access to the the counters.
	 * 
	 * @param entity
	 *            entity we're we want to have the counter for
	 * @return counter for the given entity
	 */
	public ICounter getCounter(CounterEntity entity);

}