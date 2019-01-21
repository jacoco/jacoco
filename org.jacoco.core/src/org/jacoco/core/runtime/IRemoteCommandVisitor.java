/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
