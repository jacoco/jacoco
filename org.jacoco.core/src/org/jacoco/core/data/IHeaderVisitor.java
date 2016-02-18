/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.data;

/**
 * Interface for data output of collected header. This interface is meant to be
 * implemented by parties that want to retrieve data from the coverage runtime.
 */
public interface IHeaderVisitor {

	/**
	 * Provides header for the subsequent execution data calls. This method is
	 * called only once.
	 * 
	 * @param info
	 *            header
	 */
	public void visitHeaderInfo(final HeaderInfo info);

}
