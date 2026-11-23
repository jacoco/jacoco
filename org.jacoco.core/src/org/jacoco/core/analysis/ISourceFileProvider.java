/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.io.IOException;
import java.io.Reader;

/**
 * Interface to provide source content for specific classes.
 */
public interface ISourceFileProvider {

	/**
	 * Returns a reader for the source of the given class.
	 *
	 * @param packageName
	 *            VM package name
	 * @param fileName
	 *            source file name
	 * @return reader for the source content or <code>null</code> if the source
	 *         for this class is not available
	 * @throws IOException
	 *             if the source can't be read
	 */
	Reader getSourceFile(String packageName, String fileName)
			throws IOException;

}
