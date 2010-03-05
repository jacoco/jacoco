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
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.data;

/**
 * Interface for data output of the internal structure of a single method. This
 * interface is meant to be implemented by parties that want to retrieve data
 * from the instrumentation process.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface IMethodStructureVisitor {

	/**
	 * Called for every block within the method.
	 * 
	 * @param id
	 *            identifier of the block within the method
	 * @param instructionCount
	 *            number of byte code instructions within this block
	 * @param lineNumbers
	 *            list of source lines corresponding to this block
	 */
	public void block(int id, int instructionCount, int[] lineNumbers);

	/**
	 * Signals the end of this method structure.
	 */
	public void visitEnd();

}
