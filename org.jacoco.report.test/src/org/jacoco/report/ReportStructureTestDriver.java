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
package org.jacoco.report;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;

/**
 * Creates a simple hierarchy of coverage nodes and feeds it into
 * {@link IReportVisitor} instances.
 */
public class ReportStructureTestDriver {

	private final List<SessionInfo> sessions = Collections.emptyList();

	private final Collection<ExecutionData> executionData = Collections
			.emptyList();

	public final ISourceFileLocator sourceFileLocator = new ISourceFileLocator() {

		public Reader getSourceFile(String packageName, String fileName)
				throws IOException {
			return new StringReader("");
		}

		public int getTabWidth() {
			return 4;
		}
	};

	private final IMethodCoverage methodCoverage;

	private final IClassCoverage classCoverage;

	private final ISourceFileCoverage sourceFileCoverage;

	private final IPackageCoverage packageCoverage;

	private final BundleCoverageImpl bundleCoverage;

	public ReportStructureTestDriver() {
		final MethodCoverageImpl m = new MethodCoverageImpl("fooMethod", "()V",
				null);
		m.increment(CounterImpl.getInstance(3, 5), CounterImpl.COUNTER_0_0, 1);
		m.increment(CounterImpl.getInstance(3, 5),
				CounterImpl.getInstance(1, 2), 2);
		m.increment(CounterImpl.getInstance(4, 5), CounterImpl.COUNTER_0_0, 4);
		m.incrementMethodCounter();
		methodCoverage = m;

		final ClassCoverageImpl classCoverageImpl = new ClassCoverageImpl(
				"org/jacoco/example/FooClass", 1001, false);
		classCoverageImpl.setSourceFileName("FooClass.java");
		classCoverageImpl.addMethod(methodCoverage);
		classCoverage = classCoverageImpl;

		final SourceFileCoverageImpl sourceFileCoverageImpl = new SourceFileCoverageImpl(
				"FooClass.java", "org/jacoco/example");
		sourceFileCoverageImpl.increment(classCoverage);
		sourceFileCoverage = sourceFileCoverageImpl;

		final ClassCoverageImpl emptyClassInNonEmptyPackage = new ClassCoverageImpl(
				"org/jacoco/example/Empty", 0, false);
		emptyClassInNonEmptyPackage.setSourceFileName("Empty.java");
		final SourceFileCoverageImpl emptySourceInNonEmptyPackage = new SourceFileCoverageImpl(
				"Empty.java", "org/jacoco/example");

		final ClassCoverageImpl emptyClassInEmptyPackage = new ClassCoverageImpl(
				"empty/Empty", 0, false);
		emptyClassInEmptyPackage.setSourceFileName("Empty.java");
		final SourceFileCoverageImpl emptySourceInEmptyPackage = new SourceFileCoverageImpl(
				"Empty.java", "empty");
		final PackageCoverageImpl emptyPackage = new PackageCoverageImpl(
				"empty",
				Collections.<IClassCoverage> singletonList(
						emptyClassInEmptyPackage),
				Collections.<ISourceFileCoverage> singletonList(
						emptySourceInEmptyPackage));

		packageCoverage = new PackageCoverageImpl("org/jacoco/example",
				Arrays.asList(classCoverage, emptyClassInNonEmptyPackage),
				Arrays.asList(sourceFileCoverage,
						emptySourceInNonEmptyPackage));
		bundleCoverage = new BundleCoverageImpl("bundle",
				Arrays.asList(packageCoverage, emptyPackage));
	}

	public void sendNestedGroups(IReportVisitor reportVisitor)
			throws IOException {
		reportVisitor.visitInfo(sessions, executionData);
		final IReportGroupVisitor report = reportVisitor.visitGroup("report");
		final IReportGroupVisitor group1 = report.visitGroup("group1");
		sendGroup(group1);
		sendBundle(report);
		reportVisitor.visitEnd();
	}

	public void sendGroup(IReportVisitor reportVisitor) throws IOException {
		reportVisitor.visitInfo(sessions, executionData);
		final IReportGroupVisitor group = reportVisitor.visitGroup("group");
		sendBundle(group);
		reportVisitor.visitEnd();
	}

	public void sendGroup(IReportGroupVisitor groupVisitor) throws IOException {
		final IReportGroupVisitor group = groupVisitor.visitGroup("group");
		sendBundle(group);
	}

	public void sendBundle(IReportVisitor reportVisitor) throws IOException {
		reportVisitor.visitInfo(sessions, executionData);
		reportVisitor.visitBundle(bundleCoverage, sourceFileLocator);
		reportVisitor.visitEnd();
	}

	public void sendBundle(IReportGroupVisitor groupVisitor)
			throws IOException {
		groupVisitor.visitBundle(bundleCoverage, sourceFileLocator);
	}

}
