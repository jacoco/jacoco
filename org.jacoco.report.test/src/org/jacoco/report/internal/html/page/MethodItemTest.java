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
package org.jacoco.report.internal.html.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.ILinkable;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link MethodItem}.
 */
public class MethodItemTest {

	private MethodCoverageImpl node;

	@Before
	public void setup() {
		node = new MethodCoverageImpl("test", "()V", null);
	}

	@Test
	public void testGetNode() {
		final MethodItem item = new MethodItem(node, "test()", null);
		assertSame(node, item.getNode());
	}

	@Test
	public void testGetLinkLabel() {
		final MethodItem item = new MethodItem(node, "test()", null);
		assertEquals("test()", item.getLinkLabel());
	}

	@Test
	public void testGetLinkStyle() {
		final MethodItem item = new MethodItem(node, "test()", null);
		assertEquals("el_method", item.getLinkStyle());
	}

	@Test
	public void testGetLinkNone() {
		final MethodItem item = new MethodItem(node, "test()", null);
		assertNull(item.getLink(null));
	}

	@Test
	public void testGetLink() {
		final MethodItem item = new MethodItem(node, "test()",
				new SourceLink());
		assertEquals("../Source.java", item.getLink(null));
	}

	@Test
	public void testGetLinkWithLine() {
		node.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 15);
		final MethodItem item = new MethodItem(node, "test()",
				new SourceLink());
		assertEquals("../Source.java#L15", item.getLink(null));
	}

	private static class SourceLink implements ILinkable {

		public String getLinkStyle() {
			return "el_source";
		}

		public String getLinkLabel() {
			return "Source.java";
		}

		public String getLink(ReportOutputFolder base) {
			return "../Source.java";
		}
	}

}
