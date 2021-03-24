/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.jacoco.core.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Convenience utility for reading file into a {@link String}
 */
public class StringFileReader {

	private final int BUFFER_SIZE = 1024;

	private final StringBuilder stringBuilder = new StringBuilder();
	private final File file;

	public StringFileReader(File file) {
		this.file = file;
	}

	public StringFileReader(String filename) {
		this(new File(filename));
	}

	/**
	 * Read all data from given file.
	 *
	 * @return String file data as string.
	 * @throws IOException
	 *             in case of problems while reading from the file.
	 */
	public String readString() throws IOException {
		stringBuilder.setLength(0);
		FileReader reader = new FileReader(this.file);
		try {
			char[] buffer = new char[BUFFER_SIZE];
			int index = 0;
			while ((index = reader.read(buffer)) != -1) {
				stringBuilder.append(buffer, 0, index);
			}
		} finally {
			reader.close();
		}
		return stringBuilder.toString();
	}

}
