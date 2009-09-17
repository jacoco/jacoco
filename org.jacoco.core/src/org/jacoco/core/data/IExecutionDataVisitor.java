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
 * Interface for data output of collected execution data. This interface is
 * meant to be implemented by parties that want to retrieve data from the
 * coverage runtime.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface IExecutionDataVisitor {

	/**
	 * Provides execution data for the class with the given id. The first
	 * dimension of the array corresponds to the method id the second to the
	 * block id as provided with the structure data for this class. A value of
	 * <code>true</code> indicates that a block has been executed, i.e. its last
	 * instruction was called.
	 * 
	 * @param id
	 *            id of the class
	 * @param name
	 *            VM name of the class
	 * @param blockdata
	 *            coverage data for the class
	 */
	public void visitClassExecution(long id, String name, boolean[][] blockdata);

}
