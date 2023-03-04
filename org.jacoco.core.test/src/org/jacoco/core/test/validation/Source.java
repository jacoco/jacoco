/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;

/**
 * Reads a single source file and allows access to its line coverage.
 */
public class Source {

	private static final String SRC_LOCATION = "src/";

	private static final String SRC_ENCODING = "UTF-8";

	private static final Pattern COMMENT_PATTERN = Pattern
			.compile("(?<!https?:)//(.*)");

	/**
	 * Represents a single line in a source file.
	 */
	public class Line {

		private final int nr;
		private final String text;
		private final ILine coverage;

		private Line(int nr, String text, ILine coverage) {
			this.nr = nr;
			this.text = text;
			this.coverage = coverage;
		}

		public int getNr() {
			return nr;
		}

		public String getText() {
			return text;
		}

		public ILine getCoverage() {
			return coverage;
		}

		/**
		 * @return the text of a single line comment if present or
		 *         <code>null</code>
		 */
		public String getComment() {
			final Matcher matcher = COMMENT_PATTERN.matcher(text);
			return matcher.find() ? matcher.group(1) : null;
		}

		@Override
		public String toString() {
			return Source.this.coverage.getName() + ":" + nr;
		}

	}

	private final List<Line> lines;

	private final ISourceFileCoverage coverage;

	/**
	 * Reads a source file from the given reader.
	 *
	 * @param reader
	 *            the reader to read from, will be closed
	 * @param coverage
	 *            corresponding coverage data
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public Source(final Reader reader, ISourceFileCoverage coverage)
			throws IOException {
		this.lines = new ArrayList<Line>();
		this.coverage = coverage;
		final BufferedReader buffer = new BufferedReader(reader);
		int nr = 1;
		for (String l = buffer.readLine(); l != null; l = buffer.readLine()) {
			lines.add(new Line(nr, l, coverage.getLine(nr)));
			nr++;
		}
		buffer.close();
	}

	/**
	 * @return all lines of the source file
	 */
	public List<Line> getLines() {
		return Collections.unmodifiableList(lines);
	}

	/**
	 * @return the corresponding coverage node
	 */
	public ISourceFileCoverage getCoverage() {
		return coverage;
	}

	/**
	 * Loads the source file which holds the given target class.
	 *
	 * @param target
	 *            the target class we want the source for
	 * @param bundle
	 *            the bundle containing the analyzed class and its source file
	 * @return a {@link Source} instance
	 */
	public static Source load(Class<?> target, IBundleCoverage bundle)
			throws IOException {
		final IPackageCoverage pkgCov = findByName(bundle.getPackages(),
				vm(target.getPackage().getName()));
		final IClassCoverage clsCov = findByName(pkgCov.getClasses(),
				vm(target.getName()));
		final ISourceFileCoverage srcCov = findByName(pkgCov.getSourceFiles(),
				clsCov.getSourceFileName());
		return new Source(open(SRC_LOCATION + pkgCov.getName() + "/"
				+ clsCov.getSourceFileName()), srcCov);
	}

	private static <T extends ICoverageNode> T findByName(Collection<T> nodes,
			String name) {
		for (T node : nodes) {
			if (name.equals(node.getName())) {
				return node;
			}
		}
		throw new AssertionError("Node not found: " + name);
	}

	private static String vm(String javaname) {
		return javaname.replace('.', '/');
	}

	private static Reader open(String file) throws IOException {
		return new InputStreamReader(new FileInputStream(file), SRC_ENCODING);
	}

}
