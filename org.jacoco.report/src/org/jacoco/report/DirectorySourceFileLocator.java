/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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
import java.io.InputStream;

/**
 * Locator for source files that picks source files from a given directory in
 * the file system.
 */
public class DirectorySourceFileLocator extends InputStreamSourceFileLocator {

	private final File directory;

	/**
	 * Creates a new locator that searches for source files in the given
	 * directory.
	 * 
	 * @param directory
	 *            directory to search for source file
	 * @param encoding
	 *            encoding of the source files, <code>null</code> for platform
	 *            default encoding
	 * @param tabWidth
	 *            tab width in source files as number of blanks
	 * 
	 */
	public DirectorySourceFileLocator(final File directory,
			final String encoding, final int tabWidth) {
		super(encoding, tabWidth);
		this.directory = directory;
	}

	@Override
	protected InputStream getSourceStream(final String path) throws IOException {
		final File file = new File(directory, path);
		if (file.exists()) {
			return new FileInputStream(file);
		} else {
			return null;
		}
	}

}
