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
package org.jacoco.report.internal.xml;

import java.io.IOException;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.analysis.ISourceNode;

/**
 * Serializes coverage data as XML fragments.
 */
public final class XMLCoverageWriter {

	/**
	 * Writes the structure of a given bundle.
	 *
	 * @param bundle
	 *            bundle coverage data
	 * @param element
	 *            container element for the bundle data
	 * @throws IOException
	 *             if XML can't be written to the underlying output
	 */
	public static void writeBundle(final IBundleCoverage bundle,
			final ReportElement element) throws IOException {
		for (final IPackageCoverage p : bundle.getPackages()) {
			writePackage(p, element);
		}
		writeCounters(bundle, element);
	}

	private static void writePackage(final IPackageCoverage p,
			final ReportElement parent) throws IOException {
		final ReportElement element = parent.packageElement(p.getName());
		for (final IClassCoverage c : p.getClasses()) {
			writeClass(c, element);
		}
		for (final ISourceFileCoverage s : p.getSourceFiles()) {
			writeSourceFile(s, element);
		}
		writeCounters(p, element);
	}

	private static void writeClass(final IClassCoverage c,
			final ReportElement parent) throws IOException {
		final ReportElement element = parent.classElement(c);
		for (final IMethodCoverage m : c.getMethods()) {
			writeMethod(m, element);
		}
		writeCounters(c, element);
	}

	private static void writeMethod(final IMethodCoverage m,
			final ReportElement parent) throws IOException {
		final ReportElement element = parent.method(m);
		writeCounters(m, element);
	}

	private static void writeSourceFile(final ISourceFileCoverage s,
			final ReportElement parent) throws IOException {
		final ReportElement element = parent.sourcefile(s.getName());
		writeLines(s, element);
		writeCounters(s, element);
	}

	/**
	 * Writes all non-zero counters of the given node.
	 *
	 * @param node
	 *            node to retrieve counters from
	 * @param parent
	 *            container for the counter elements
	 * @throws IOException
	 *             if XML can't be written to the underlying output
	 */
	public static void writeCounters(final ICoverageNode node,
			final ReportElement parent) throws IOException {
		for (final CounterEntity counterEntity : CounterEntity.values()) {
			final ICounter counter = node.getCounter(counterEntity);
			if (counter.getTotalCount() > 0) {
				parent.counter(counterEntity, counter);
			}
		}
	}

	private static void writeLines(final ISourceNode source,
			final ReportElement parent) throws IOException {
		final int last = source.getLastLine();
		for (int nr = source.getFirstLine(); nr <= last; nr++) {
			final ILine line = source.getLine(nr);
			if (line.getStatus() != ICounter.EMPTY) {
				parent.line(nr, line);
			}
		}
	}

	private XMLCoverageWriter() {
	}

}
