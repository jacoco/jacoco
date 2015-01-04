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
 * Interface for data output of collected session information. This interface is
 * meant to be implemented by parties that want to retrieve data from the
 * coverage runtime.
 */
public interface ISessionInfoVisitor {

	/**
	 * Provides session information for the subsequent execution data calls. In
	 * case of merged sessions this method might be called multiple times.
	 * 
	 * @param info
	 *            session information
	 */
	public void visitSessionInfo(final SessionInfo info);

}
