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
package org.jacoco.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Locator for source files that picks source files from a given directory in
 * the file system.
 */
public class DirectorySourceFileLocator implements ISourceFileLocator {

	private final File directory;

	private final String encoding;

	private final int tabWidth;

	/**
	 * Creates a new locator that searches for source files in the given
	 * directory.
	 * 
	 * @param directory
	 *            directory to search for source file
	 * @param encoding
	 *            encoding of the source files
	 * @param tabWidth
	 *            tab width in source files as number of blanks
	 * 
	 */
	public DirectorySourceFileLocator(final File directory,
			final String encoding, final int tabWidth) {
		this.directory = directory;
		this.encoding = encoding;
		this.tabWidth = tabWidth;
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

	public int getTabWidth() {
		return tabWidth;
	}

}
