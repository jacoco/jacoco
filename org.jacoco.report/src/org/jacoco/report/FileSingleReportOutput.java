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

import static java.lang.String.format;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Implementation of {@link ISingleReportOutput} that writes the file directly
 * to a given location.
 */
public class FileSingleReportOutput implements ISingleReportOutput {

	private final File file;

	/**
	 * Creates a new instance for document output to the given location.
	 * 
	 * @param file
	 */
	public FileSingleReportOutput(final File file) {
		this.file = file;
	}

	public OutputStream createFile() throws IOException {
		final File parent = file.getParentFile();
		parent.mkdirs();
		if (!parent.isDirectory()) {
			throw new IOException(format("Can't create directory %s.", parent));
		}
		return new BufferedOutputStream(new FileOutputStream(file));
	}

}
