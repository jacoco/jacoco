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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.junit.Test;

/**
 * Unit tests for {@link MultiReportVisitor}.
 */
public class MultiReportVisitorTest {

	private static class MockVisitor extends MockGroupVisitor
			implements IReportVisitor {

		MockVisitor() {
			super("Report");
		}

		private boolean visitInfosCalled = false;

		private boolean visitEndCalled = false;

		public void visitInfo(List<SessionInfo> sessionInfos,
				Collection<ExecutionData> executionData) throws IOException {
			visitInfosCalled = true;
		}

		public void visitEnd() throws IOException {
			visitEndCalled = true;
		}

		@Override
		public String toString() {
			assertTrue("visitInfos() has not been called", visitInfosCalled);
			assertTrue("visitEnd() has not been called", visitEndCalled);
			return super.toString();
		}

	}

	private static class MockGroupVisitor implements IReportGroupVisitor {

		private final String name;

		private final List<MockGroupVisitor> children = new ArrayList<MockGroupVisitor>();

		MockGroupVisitor(String name) {
			this.name = name;
		}

		public void visitBundle(IBundleCoverage bundle,
				ISourceFileLocator locator) throws IOException {
			children.add(new MockGroupVisitor(bundle.getName()));
		}

		public IReportGroupVisitor visitGroup(String name) throws IOException {
			MockGroupVisitor child = new MockGroupVisitor(name);
			children.add(child);
			return child;
		}

		@Override
		public String toString() {
			return name + children;
		}
	}

	private IBundleCoverage createBundle(String name) {
		final Collection<IPackageCoverage> packages = Collections.emptyList();
		return new BundleCoverageImpl(name, packages);
	}

	private static final String MOCK_REPORT = "Report[g1[b1[], b2[]], g2[]]";

	private void createMockReport(IReportVisitor visitor) throws IOException {
		final List<SessionInfo> sessions = Collections.emptyList();
		final List<ExecutionData> executionData = Collections.emptyList();
		visitor.visitInfo(sessions, executionData);
		IReportGroupVisitor g1 = visitor.visitGroup("g1");
		g1.visitBundle(createBundle("b1"), null);
		g1.visitBundle(createBundle("b2"), null);
		visitor.visitGroup("g2");
		visitor.visitEnd();
	}

	@Test
	public void testMockFormatter() throws IOException {
		MockVisitor visitor = new MockVisitor();
		createMockReport(visitor);
		assertEquals(MOCK_REPORT, visitor.toString());
	}

	@Test
	public void testMultiFormatter() throws IOException {
		IReportVisitor mock1 = new MockVisitor();
		IReportVisitor mock2 = new MockVisitor();
		IReportVisitor mock3 = new MockVisitor();
		List<IReportVisitor> visitors = Arrays.asList(mock1, mock2, mock3);
		MultiReportVisitor multi = new MultiReportVisitor(visitors);
		createMockReport(multi);
		assertEquals(MOCK_REPORT, mock1.toString());
		assertEquals(MOCK_REPORT, mock2.toString());
		assertEquals(MOCK_REPORT, mock3.toString());
	}
}
