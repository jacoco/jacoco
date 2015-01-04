/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.data;

/**
 * Interface for data output of collected execution data. This interface is
 * meant to be implemented by parties that want to retrieve data from the
 * coverage runtime.
 */
public interface IExecutionDataVisitor {

	/**
	 * Provides execution data for a class.
	 * 
	 * @param data
	 *            execution data for a class
	 */
	public void visitClassExecution(ExecutionData data);

}
