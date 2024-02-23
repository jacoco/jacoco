/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    Kyle Lieber - implementation of CheckMojo
 *
 *******************************************************************************/
package org.jacoco.report;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.jacoco.core.utils.FileUtils;

/**
 * A file filter using includes/excludes patterns.
 */
public class FileFilter {

	private static final String DEFAULT_INCLUDES = "**";
	private static final String DEFAULT_EXCLUDES = "";

	private final List<String> includes;
	private final List<String> excludes;

	/**
	 * Construct a new FileFilter
	 *
	 * @param includes
	 *            list of includes patterns
	 * @param excludes
	 *            list of excludes patterns
	 */
	public FileFilter(final List<String> includes,
			final List<String> excludes) {
		this.includes = includes;
		this.excludes = excludes;
	}

	/**
	 * Returns a list of file names.
	 *
	 * @param directory
	 *            the directory to scan
	 * @return a list of files
	 * @throws IOException
	 *             if file system access fails
	 */
	public List<String> getFileNames(final File directory) throws IOException {
		return FileUtils.getFileNames(directory, getIncludes(), getExcludes(),
				false);
	}

	/**
	 * Returns a list of files.
	 *
	 * @param directory
	 *            the directory to scan
	 * @return a list of files
	 * @throws IOException
	 *             if file system access fails
	 */
	public List<File> getFiles(final File directory) throws IOException {
		return FileUtils.getFiles(directory, getIncludes(), getExcludes());
	}

	/**
	 * Get the includes pattern
	 *
	 * @return the pattern
	 */
	public List<String> getIncludes() {
		return this.buildPattern(this.includes, DEFAULT_INCLUDES);
	}

	/**
	 * Get the excludes pattern
	 *
	 * @return the pattern
	 */
	public List<String> getExcludes() {
		return this.buildPattern(this.excludes, DEFAULT_EXCLUDES);
	}

	private List<String> buildPattern(final List<String> patterns,
			final String defaultPattern) {
		if (patterns == null || patterns.isEmpty()) {
			return Collections.singletonList(defaultPattern);
		}
		return patterns;
	}
}
