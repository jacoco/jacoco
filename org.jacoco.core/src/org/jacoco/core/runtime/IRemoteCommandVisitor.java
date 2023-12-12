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
package org.jacoco.core.runtime;

import java.io.IOException;

/**
 * Interface for remote commands to a coverage runtime.
 */
public interface IRemoteCommandVisitor {

	/**
	 * Requests a execution data dump with an optional reset.
	 *
	 * @param dump
	 *            <code>true</code> if the dump should be executed
	 * @param reset
	 *            <code>true</code> if the reset should be executed
	 * @throws IOException
	 *             in case of problems with the remote connection
	 */
	void visitDumpCommand(boolean dump, boolean reset) throws IOException;

}
