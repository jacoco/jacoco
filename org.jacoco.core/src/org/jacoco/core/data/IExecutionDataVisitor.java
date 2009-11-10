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
	 * Provides execution data for the class with the given id. Each slot in the
	 * array represents a probe in the instrumented class. A value of
	 * <code>true</code> indicates that a probe has been executed.
	 * 
	 * @param id
	 *            id of the class
	 * @param name
	 *            VM name of the class
	 * @param data
	 *            coverage data for the class
	 */
	public void visitClassExecution(long id, String name, boolean[] data);

}
