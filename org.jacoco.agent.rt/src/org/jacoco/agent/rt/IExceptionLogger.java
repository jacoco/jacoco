/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt;

/**
 * At several places exception might occur that should be reported. For
 * testability these exceptions are emitted against this interface.
 */
public interface IExceptionLogger {

	/**
	 * Logs the given exception.
	 * 
	 * @param ex
	 *            exception to log
	 */
	public void logExeption(Exception ex);

}
