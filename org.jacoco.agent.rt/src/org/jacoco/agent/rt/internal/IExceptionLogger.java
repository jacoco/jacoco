/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

/**
 * At several places exception might occur that should be reported. For
 * testability these exceptions are emitted against this interface.
 */
public interface IExceptionLogger {

	/**
	 * Default implementation which dumps the stack trace to System.err.
	 */
	IExceptionLogger SYSTEM_ERR = new IExceptionLogger() {
		public void logExeption(final Exception ex) {
			ex.printStackTrace();
		}
	};

	/**
	 * Logs the given exception.
	 *
	 * @param ex
	 *            exception to log
	 */
	void logExeption(Exception ex);

}
