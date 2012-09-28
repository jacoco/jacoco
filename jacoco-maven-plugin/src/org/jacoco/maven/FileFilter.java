/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.plexus.util.StringUtils;

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
	public FileFilter(final List<String> includes, final List<String> excludes) {
		this.includes = includes;
		this.excludes = excludes;
	}

	/**
	 * Get the includes pattern
	 * 
	 * @return the pattern
	 */
	public String getIncludes() {
		return this.buildPattern(this.includes, DEFAULT_INCLUDES);
	}

	/**
	 * Get the excludes pattern
	 * 
	 * @return the pattern
	 */
	public String getExcludes() {
		return this.buildPattern(this.excludes, DEFAULT_EXCLUDES);
	}

	private String buildPattern(final List<String> patterns,
			final String defaultPattern) {
		String pattern = defaultPattern;
		if (CollectionUtils.isNotEmpty(patterns)) {
			pattern = StringUtils.join(patterns.iterator(), ",");
		}
		return pattern;
	}
}
