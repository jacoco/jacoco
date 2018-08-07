/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reads a single source file and allows access to it through special probe
 * comments in the following format <code>//$line-<i>tag</i>$.
 */
public class Source {

	private final List<String> lines = new ArrayList<String>();

	/**
	 * Reads a source file from the given reader.
	 * 
	 * @param reader
	 *            the reader to read from
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public Source(final Reader reader) throws IOException {
		final BufferedReader buffer = new BufferedReader(reader);
		for (String l = buffer.readLine(); l != null; l = buffer.readLine()) {
			addLine(l);
		}
		buffer.close();
	}

	private void addLine(final String l) {
		lines.add(l);
	}

	/**
	 * Returns all lines of the source file as a list.
	 * 
	 * @return all lines of the source file
	 */
	public List<String> getLines() {
		return Collections.unmodifiableList(lines);
	}

	/**
	 * Returns the line with the given number
	 * 
	 * @param nr
	 *            line number (first line is 1)
	 * @return line content
	 */
	public String getLine(int nr) {
		return lines.get(nr - 1);
	}

}
