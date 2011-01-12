/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Locator for source files that picks source files from a given directory in
 * the file system.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class DirectorySourceFileLocator implements ISourceFileLocator {

	private final File directory;

	private final String encoding;

	/**
	 * Creates a new locator that searches for source files in the given
	 * directory.
	 * 
	 * @param directory
	 *            directory to search for source file
	 * @param encoding
	 *            encoding of the source files
	 */
	public DirectorySourceFileLocator(final File directory, final String encoding) {
		this.directory = directory;
		this.encoding = encoding;
	}

	public Reader getSourceFile(final String packageName, final String fileName)
			throws IOException {
		final File dir = new File(directory, packageName);
		final File file = new File(dir, fileName);
		if (file.exists()) {
			return new InputStreamReader(new FileInputStream(file), encoding);
		}
		return null;
	}

}
