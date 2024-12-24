/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Stark X - initial API and implementation
 *******************************************************************************/
package org.jacoco.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileUtils {
	private FileUtils() {
	}

	public static List<File> getFiles(final File directory,
			final Collection<String> includes,
			final Collection<String> excludes, final boolean includeBaseDir)
			throws IOException {
		final List<PathMatcher> includeMatchers = buildPathMatchers(includes);
		final List<PathMatcher> excludeMatchers = buildPathMatchers(excludes);

		final List<File> files = new ArrayList<File>();
		SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
			boolean matches(Path file, Collection<PathMatcher> patterns) {
				for (PathMatcher pattern : patterns) {
					if (pattern.matches(file)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) {
				final Path relativePath = directory.toPath().relativize(file);
				if (matches(relativePath, includeMatchers)
						&& !matches(relativePath, excludeMatchers)) {
					final Path path = includeBaseDir ? file : relativePath;
					files.add(path.toFile());
				}
				return FileVisitResult.CONTINUE;
			}
		};
		Files.walkFileTree(directory.toPath(), visitor);

		return files;
	}

	private static List<PathMatcher> buildPathMatchers(
			Collection<String> includeOrExcludes) {
		final List<PathMatcher> excludeMatchers = new ArrayList<PathMatcher>();
		for (String xclude : includeOrExcludes) {
			final PathMatcher matcher = FileUtils.buildPathMatcher(xclude);
			if (matcher != null) {
				excludeMatchers.add(matcher);
			}
			if (xclude.startsWith("**/")) {
				final PathMatcher rootMatcher = FileUtils
						.buildPathMatcher(xclude.substring(3));
				if (rootMatcher != null) {
					excludeMatchers.add(rootMatcher);
				}
			}
		}
		return excludeMatchers;
	}

	public static List<File> getFiles(final File directory,
			final Collection<String> includes,
			final Collection<String> excludes) throws IOException {
		return getFiles(directory, includes, excludes, true);
	}

	public static List<String> getFileNames(File directory,
			final Collection<String> includes,
			final Collection<String> excludes, boolean includeBaseDir)
			throws IOException {
		final List<File> files = FileUtils.getFiles(directory, includes,
				excludes, includeBaseDir);
		final List<String> names = new ArrayList<String>();
		for (File file : files) {
			names.add(file.toString());
		}
		return names;
	}

	public static List<String> getFileNames(File directory,
			final Collection<String> includes,
			final Collection<String> excludes) throws IOException {
		return getFileNames(directory, includes, excludes, true);
	}

	public static PathMatcher buildPathMatcher(String pattern) {
		if (pattern == null || pattern.isEmpty()) {
			return null;
		}
		return FileSystems.getDefault().getPathMatcher("glob:" + pattern);
	}
}
