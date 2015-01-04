/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a single source file and allows access to it through special probe
 * comments in the following format <code>//$line-<i>tag</i>$.
 */
public class Source {

	/**
	 * Reads the source for the given type from the <code>./src/</code> folder
	 * relative to the working directory.
	 * 
	 * @param type
	 *            type to load the source file for
	 * @throws IOException
	 * @throws
	 */
	public static Source getSourceFor(final Class<?> type) throws IOException {
		String file = "src/" + type.getName().replace('.', '/') + ".java";
		return new Source(new FileReader(file));
	}

	private static final Pattern TAG_PATTERN = Pattern
			.compile("\\$line-(.*)\\$");

	private final List<String> lines = new ArrayList<String>();

	private final Map<String, Integer> tags = new HashMap<String, Integer>();

	/**
	 * Reads a source file from the given reader.
	 * 
	 * @param reader
	 * @throws IOException
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
		final Matcher m = TAG_PATTERN.matcher(l);
		if (m.find()) {
			final String tag = m.group(1);
			if (tags.put(tag, Integer.valueOf(lines.size())) != null) {
				throw new IllegalArgumentException("Duplicate tag: " + tag);
			}
		}
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

	/**
	 * Returns the line number with the given tag
	 * 
	 * @param tag
	 *            tag from a <code>//$line-<i>tag</i>$ marker
	 * @return line number (first line is 1)
	 * @throws NoSuchElementException
	 *             if there is no such tag
	 */
	public int getLineNumber(String tag) throws NoSuchElementException {
		final Integer nr = tags.get(tag);
		if (nr == null) {
			throw new NoSuchElementException("Unknown tag: " + tag);
		}
		return nr.intValue();
	}

}
