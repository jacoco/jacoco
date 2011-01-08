/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.analysis.BundleCoverage;
import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.analysis.MethodCoverage;
import org.jacoco.core.analysis.PackageCoverage;
import org.jacoco.core.analysis.SourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;

/**
 * Creates a simple hierarchy of coverage nodes and feeds it into
 * {@link IReportFormatter} instances.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
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

	private final MethodCoverage methodCoverage = new MethodCoverage(
			"fooMethod", "()V", null);

	private final ClassCoverage classCoverage = new ClassCoverage(
			"org/jacoco/example/FooClass", 1001, null, "java/lang/Object",
			new String[0]);

	private final SourceFileCoverage sourceFileCoverage = new SourceFileCoverage(
			"FooClass.java", "org/jacoco/example");

	private final PackageCoverage packageCoverage = new PackageCoverage(
			"org/jacoco/example", Collections.singleton(classCoverage),
			Collections.singleton(sourceFileCoverage));

	private final BundleCoverage bundleCoverage = new BundleCoverage("bundle",
			Collections.singleton(packageCoverage));

	private final CoverageNodeImpl groupCoverage = new CoverageNodeImpl(
			ElementType.GROUP, "group");

	public ReportStructureTestDriver() {
		classCoverage.setSourceFileName("FooClass.java");
		classCoverage.addMethod(methodCoverage);
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
