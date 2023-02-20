/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.csv;

import java.io.IOException;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.report.ILanguageNames;

/**
 * Writer for rows in the CVS report representing the summary data of a single
 * class.
 */
class ClassRowWriter {

	private static final CounterEntity[] COUNTERS = { CounterEntity.INSTRUCTION,
			CounterEntity.BRANCH, CounterEntity.LINE, CounterEntity.COMPLEXITY,
			CounterEntity.METHOD };

	private final DelimitedWriter writer;

	private final ILanguageNames languageNames;

	/**
	 * Creates a new row writer that writes class information to the given CSV
	 * writer.
	 *
	 * @param writer
	 *            writer for csv output
	 * @param languageNames
	 *            converter for Java identifiers
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public ClassRowWriter(final DelimitedWriter writer,
			final ILanguageNames languageNames) throws IOException {
		this.writer = writer;
		this.languageNames = languageNames;
		writeHeader();
	}

	private void writeHeader() throws IOException {
		writer.write("GROUP", "PACKAGE", "CLASS");
		for (final CounterEntity entity : COUNTERS) {
			writer.write(entity.name() + "_MISSED");
			writer.write(entity.name() + "_COVERED");
		}
		writer.nextLine();
	}

	/**
	 * Writes the class summary information as a row.
	 *
	 * @param groupName
	 *            name of the group
	 * @param packageName
	 *            vm name of the package
	 * @param node
	 *            class coverage data
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public void writeRow(final String groupName, final String packageName,
			final IClassCoverage node) throws IOException {
		writer.write(groupName);
		writer.write(languageNames.getPackageName(packageName));
		final String className = languageNames.getClassName(node.getName(),
				node.getSignature(), node.getSuperName(),
				node.getInterfaceNames());
		writer.write(className);

		for (final CounterEntity entity : COUNTERS) {
			final ICounter counter = node.getCounter(entity);
			writer.write(counter.getMissedCount());
			writer.write(counter.getCoveredCount());
		}

		writer.nextLine();
	}

}
