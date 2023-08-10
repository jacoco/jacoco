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
package org.jacoco.report;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface to emit multiple binary files.
 */
public interface IMultiReportOutput {

	/**
	 * Creates a file at the given local path. The returned {@link OutputStream}
	 * has to be closed before the next document is created.
	 *
	 * @param path
	 *            local path to the new document
	 * @return output for the content
	 * @throws IOException
	 *             if the creation fails
	 */
	OutputStream createFile(String path) throws IOException;

	/**
	 * Closes the underlying resource container.
	 *
	 * @throws IOException
	 *             if closing fails
	 */
	void close() throws IOException;

}
