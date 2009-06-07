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
package org.jacoco.core.data;

/**
 * Coverage data of a single basic block.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class BlockCoverageData extends CoverageDataImpl {

	/**
	 * Creates a new block data object with the given parameters.
	 * 
	 * @param instructionCount
	 *            number of byte code instructions contained in this block
	 * @param lineNumbers
	 *            source lines corresponding to this block
	 * @param covered
	 *            <code>true</code>, if this block is covered
	 */
	public BlockCoverageData(final int instructionCount,
			final int[] lineNumbers, final boolean covered) {
		super(ElementType.BLOCK, true);
		blockCounter = CounterImpl.getInstance(covered);
		instructionCounter = CounterImpl.getInstance(instructionCount, covered);
		lines.increment(lineNumbers, covered);
	}
}
