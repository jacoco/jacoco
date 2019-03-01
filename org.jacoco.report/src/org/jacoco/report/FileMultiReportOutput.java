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
package org.jacoco.report;

import static java.lang.String.format;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Implementation of {@link IMultiReportOutput} that writes files directly to a
 * given directory.
 */
public class FileMultiReportOutput implements IMultiReportOutput {

	private final File basedir;

	/**
	 * Creates a new instance for document output in the given base directory.
	 * 
	 * @param basedir
	 *            base directory
	 */
	public FileMultiReportOutput(final File basedir) {
		this.basedir = basedir;
	}

	public OutputStream createFile(final String path) throws IOException {
		final File file = new File(basedir, path);
		final File parent = file.getParentFile();
		parent.mkdirs();
		if (!parent.isDirectory()) {
			throw new IOException(format("Can't create directory %s.", parent));
		}
		return new BufferedOutputStream(new FileOutputStream(file));
	}

	public void close() throws IOException {
		// nothing to do here
	}

}
