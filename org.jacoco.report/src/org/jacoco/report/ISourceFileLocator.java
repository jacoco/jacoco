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
import java.io.Reader;

/**
 * Interface to look-up source files that will be included with the report.
 */
public interface ISourceFileLocator {

	/**
	 * Tries to locate the given source file and opens a reader with the
	 * appropriate encoding.
	 *
	 * @param packageName
	 *            VM name of the package
	 * @param fileName
	 *            name of the source file
	 * @return reader if the file could be located, <code>null</code> otherwise
	 * @throws IOException
	 *             in case of problems while opening the file
	 */
	Reader getSourceFile(String packageName, String fileName)
			throws IOException;

	/**
	 * Returns number of blank characters that represent a tab in source code.
	 *
	 * @return tab width as number of blanks
	 */
	int getTabWidth();

}
