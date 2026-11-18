/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    r3bb1t - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionDataStore;

/**
 * Keeps one of several different class files with the same name. All classes
 * are collected before one copy of each name is chosen, so the choice does not
 * depend on the order in which class files are found: the copy execution data
 * was recorded for is kept, otherwise the copy with the lowest class id.
 */
class DuplicateClassFilter implements ICoverageVisitor {

	private final ExecutionDataStore executionData;

	private final Map<String, List<Copy>> classes = new HashMap<String, List<Copy>>();

	private String location = "";

	/**
	 * Creates a new filter.
	 *
	 * @param executionData
	 *            execution data the analyzed classes are matched against
	 */
	DuplicateClassFilter(final ExecutionDataStore executionData) {
		this.executionData = executionData;
	}

	/**
	 * Creates an analyzer that passes its classes to this filter together with
	 * the location of their class files.
	 *
	 * @return new analyzer
	 */
	Analyzer createAnalyzer() {
		return new Analyzer(executionData, this) {
			@Override
			public void analyzeClass(final byte[] buffer, final String location)
					throws IOException {
				setLocation(location);
				super.analyzeClass(buffer, location);
			}
		};
	}

	/**
	 * Sets the location of the class file of the classes visited next.
	 *
	 * @param location
	 *            location description of the class file
	 */
	void setLocation(final String location) {
		this.location = location;
	}

	public void visitCoverage(final IClassCoverage coverage) {
		final String name = coverage.getName();
		List<Copy> copies = classes.get(name);
		if (copies == null) {
			copies = new ArrayList<Copy>(1);
			classes.put(name, copies);
		}
		for (final Copy copy : copies) {
			if (copy.coverage.getId() == coverage.getId()) {
				// Identical class files: name one location whatever the order
				if (location.compareTo(copy.location) < 0) {
					copy.location = location;
				}
				return;
			}
		}
		copies.add(new Copy(coverage, location,
				executionData.get(coverage.getId()) != null));
	}

	/**
	 * Passes the kept copy of every class to the given visitor.
	 *
	 * @param visitor
	 *            visitor to receive the kept classes
	 */
	void accept(final ICoverageVisitor visitor) {
		for (final List<Copy> copies : classes.values()) {
			visitor.visitCoverage(kept(copies).coverage);
		}
	}

	/**
	 * Returns the classes found in more than one class file with different
	 * content, sorted by name.
	 *
	 * @return classes found with different content
	 */
	List<Duplicate> getDuplicates() {
		final List<Duplicate> duplicates = new ArrayList<Duplicate>();
		for (final List<Copy> copies : classes.values()) {
			if (copies.size() > 1) {
				duplicates.add(new Duplicate(copies));
			}
		}
		Collections.sort(duplicates);
		return duplicates;
	}

	private static Copy kept(final List<Copy> copies) {
		Copy kept = copies.get(0);
		for (final Copy copy : copies) {
			if (copy.isPreferredTo(kept)) {
				kept = copy;
			}
		}
		return kept;
	}

	private static class Copy {

		final IClassCoverage coverage;

		final boolean executed;

		String location;

		Copy(final IClassCoverage coverage, final String location,
				final boolean executed) {
			this.coverage = coverage;
			this.location = location;
			this.executed = executed;
		}

		boolean isPreferredTo(final Copy other) {
			if (executed != other.executed) {
				return executed;
			}
			return coverage.getId() < other.coverage.getId();
		}

	}

	/**
	 * A class found in more than one class file with different content.
	 */
	static class Duplicate implements Comparable<Duplicate> {

		private final String name;

		private final String kept;

		private final List<String> ignored;

		private final int executedCopies;

		Duplicate(final List<Copy> copies) {
			final Copy keptCopy = kept(copies);
			this.name = keptCopy.coverage.getName();
			this.kept = keptCopy.location;
			this.ignored = new ArrayList<String>();
			int executed = 0;
			for (final Copy copy : copies) {
				if (copy != keptCopy) {
					ignored.add(copy.location);
				}
				if (copy.executed) {
					executed++;
				}
			}
			Collections.sort(ignored);
			this.executedCopies = executed;
		}

		/**
		 * @return VM name of the class
		 */
		String getName() {
			return name;
		}

		/**
		 * @return location of the reported copy
		 */
		String getKept() {
			return kept;
		}

		/**
		 * @return locations of the ignored copies, sorted
		 */
		List<String> getIgnored() {
			return ignored;
		}

		/**
		 * @return number of copies execution data was recorded for
		 */
		int getExecutedCopies() {
			return executedCopies;
		}

		public int compareTo(final Duplicate other) {
			return name.compareTo(other.name);
		}

	}

}
