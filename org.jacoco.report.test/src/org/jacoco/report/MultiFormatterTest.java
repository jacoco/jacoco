/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.junit.Test;

/**
 * Unit tests for {@link MultiFormatter}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class MultiFormatterTest {

	private static class MockFormatter implements IReportFormatter {

		private MockVisitor visitor;

		public IReportVisitor createReportVisitor(ICoverageNode session)
				throws IOException {
			visitor = new MockVisitor(session);
			return visitor;
		}

		@Override
		public String toString() {
			return visitor.toString();
		}

	}

	private static class MockVisitor implements IReportVisitor {

		private final String name;

		private final List<MockVisitor> children = new ArrayList<MockVisitor>();

		private boolean visitEndCalled = false;

		MockVisitor(ICoverageNode node) {
			name = node.getName();
		}

		public IReportVisitor visitChild(ICoverageNode node) throws IOException {
			assertFalse("visitEnd() already called", visitEndCalled);
			MockVisitor child = new MockVisitor(node);
			children.add(child);
			return child;
		}

		public void visitEnd(ISourceFileLocator sourceFileLocator)
				throws IOException {
			assertFalse("visitEnd() already called", visitEndCalled);
			visitEndCalled = true;
		}

		@Override
		public String toString() {
			assertTrue("visitEnd() has not been called", visitEndCalled);
			return name + children;
		}
	}

	private CoverageNodeImpl createNode(String name) {
		return new CoverageNodeImpl(ElementType.GROUP, name, false);
	}

	private static final String MOCK_SESSION = "Session[b1[p1[], p2[]], b2[]]";

	private void createMockSession(IReportFormatter formatter)
			throws IOException {
		IReportVisitor session = formatter
				.createReportVisitor(createNode("Session"));
		{
			IReportVisitor b1 = session.visitChild(createNode("b1"));
			{
				IReportVisitor p1 = b1.visitChild(createNode("p1"));
				p1.visitEnd(null);
			}
			{
				IReportVisitor p2 = b1.visitChild(createNode("p2"));
				p2.visitEnd(null);
			}
			b1.visitEnd(null);
		}
		{
			IReportVisitor b2 = session.visitChild(createNode("b2"));
			b2.visitEnd(null);
		}
		session.visitEnd(null);
	}

	@Test
	public void testMockFormatter() throws IOException {
		MockFormatter formatter = new MockFormatter();
		createMockSession(formatter);
		assertEquals(MOCK_SESSION, formatter.toString());
	}

	@Test
	public void testMultiFormatter() throws IOException {
		MockFormatter mock1 = new MockFormatter();
		MockFormatter mock2 = new MockFormatter();
		MockFormatter mock3 = new MockFormatter();
		MultiFormatter multi = new MultiFormatter();
		multi.add(mock1);
		multi.add(mock2);
		multi.add(mock3);
		createMockSession(multi);
		assertEquals(MOCK_SESSION, mock1.toString());
		assertEquals(MOCK_SESSION, mock2.toString());
		assertEquals(MOCK_SESSION, mock3.toString());
	}

}
