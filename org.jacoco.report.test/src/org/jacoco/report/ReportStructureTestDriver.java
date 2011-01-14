/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;

/**
 * Creates a simple hierarchy of coverage nodes and feeds it into
 * {@link IReportFormatter} instances.
 */
public class ReportStructureTestDriver {

	private final List<SessionInfo> sessions = Collections.emptyList();

	private final Collection<ExecutionData> executionData = Collections
			.emptyList();

	public final ISourceFileLocator sourceFileLocator = new ISourceFileLocator() {

		public Reader getSourceFile(String packageName, String fileName)
				throws IOException {
			return null;
		}
	};

	private final IMethodCoverage methodCoverage;

	private final IClassCoverage classCoverage;

	private final ISourceFileCoverage sourceFileCoverage;

	private final IPackageCoverage packageCoverage;

	private final BundleCoverageImpl bundleCoverage;

	private final CoverageNodeImpl groupCoverage;

	public ReportStructureTestDriver() {
		methodCoverage = new MethodCoverageImpl("fooMethod", "()V", null);

		final ClassCoverageImpl classCoverageImpl = new ClassCoverageImpl(
				"org/jacoco/example/FooClass", 1001, null, "java/lang/Object",
				new String[0]);
		classCoverageImpl.setSourceFileName("FooClass.java");
		classCoverageImpl.addMethod(methodCoverage);
		classCoverage = classCoverageImpl;

		sourceFileCoverage = new SourceFileCoverageImpl("FooClass.java",
				"org/jacoco/example");
		packageCoverage = new PackageCoverageImpl("org/jacoco/example",
				Collections.singleton(classCoverage),
				Collections.singleton(sourceFileCoverage));
		bundleCoverage = new BundleCoverageImpl("bundle",
				Collections.singleton(packageCoverage));
		groupCoverage = new CoverageNodeImpl(ElementType.GROUP, "group");
	}

	public void sendGroup(IReportFormatter formatter) throws IOException {
		final IReportVisitor child = formatter.createReportVisitor(
				groupCoverage, sessions, executionData);
		sendBundle(child);
		child.visitEnd(sourceFileLocator);
	}

	public void sendGroup(IReportVisitor parent) throws IOException {
		final IReportVisitor child = parent.visitChild(groupCoverage);
		sendBundle(child);
		child.visitEnd(sourceFileLocator);
	}

	public void sendBundle(IReportFormatter formatter) throws IOException {
		final IReportVisitor child = formatter.createReportVisitor(
				bundleCoverage, sessions, executionData);
		sendPackage(child);
		child.visitEnd(sourceFileLocator);
	}

	public void sendBundle(IReportVisitor parent) throws IOException {
		final IReportVisitor child = parent.visitChild(bundleCoverage);
		sendPackage(child);
		child.visitEnd(sourceFileLocator);
	}

	public void sendPackage(IReportVisitor parent) throws IOException {
		final IReportVisitor child = parent.visitChild(packageCoverage);
		sendClass(child);
		sendSourceFile(child);
		child.visitEnd(sourceFileLocator);
	}

	public void sendClass(IReportVisitor parent) throws IOException {
		final IReportVisitor child = parent.visitChild(classCoverage);
		sendMethod(child);
		child.visitEnd(sourceFileLocator);
	}

	public void sendMethod(IReportVisitor parent) throws IOException {
		parent.visitChild(methodCoverage).visitEnd(sourceFileLocator);
	}

	public void sendSourceFile(IReportVisitor parent) throws IOException {
		parent.visitChild(sourceFileCoverage).visitEnd(sourceFileLocator);
	}

}
