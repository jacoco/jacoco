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

import java.io.IOException;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.ILinkable;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link ClassPage}.
 */
public class ClassPageTest extends PageTestBase {

	private ClassCoverageImpl node;

	private ClassPage page;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		final MethodCoverageImpl m = new MethodCoverageImpl("a", "()V", null);
		m.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 42);
		node = new ClassCoverageImpl("org/jacoco/example/Foo", 123, false);
		node.addMethod(m);
		node.addMethod(new MethodCoverageImpl("b", "()V", null));
		node.addMethod(new MethodCoverageImpl("c", "()V", null));
	}

	@Test
	public void testContents() throws Exception {
		page = new ClassPage(node, null, null, rootFolder, context);
		page.render();

		final Document doc = support.parse(output.getFile("Foo.html"));
		assertEquals("", support.findStr(doc, "doc/body/p[1]"));
		assertEquals("el_method", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/span/@class"));
		assertEquals("a()", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/span"));
		assertEquals("b()", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[2]/td[1]/span"));
		assertEquals("c()", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[3]/td[1]/span"));
	}

	@Test
	public void should_generate_message_when_SourceFileName_not_present()
			throws Exception {
		page = new ClassPage(node, null, null, rootFolder, context);
		page.render();

		final Document doc = support.parse(output.getFile("Foo.html"));
		assertEquals(
				"Class files must be compiled with debug information to link with source files.",
				support.findStr(doc, "/html/body/p[1]"));
	}

	@Test
	public void should_generate_message_when_SourceFileName_present_but_no_SourceFilePage()
			throws Exception {
		node.setSourceFileName("Foo.java");

		page = new ClassPage(node, null, null, rootFolder, context);
		page.render();

		final Document doc = support.parse(output.getFile("Foo.html"));
		assertEquals(
				"Source file \"org/jacoco/example/Foo.java\" was not found during generation of report.",
				support.findStr(doc, "/html/body/p[1]"));
	}

	@Test
	public void should_generate_message_with_default_package_when_SourceFileName_present_but_no_SourceFilePage()
			throws Exception {
		final MethodCoverageImpl m = new MethodCoverageImpl("a", "()V", null);
		m.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 42);
		node = new ClassCoverageImpl("Foo", 123, false);
		node.addMethod(m);
		node.setSourceFileName("Foo.java");

		page = new ClassPage(node, null, null, rootFolder, context);
		page.render();

		final Document doc = support.parse(output.getFile("Foo.html"));
		assertEquals(
				"Source file \"Foo.java\" was not found during generation of report.",
				support.findStr(doc, "/html/body/p[1]"));
	}

	@Test
	public void should_not_generate_message_when_SourceFileName_and_SourceFilePage_present()
			throws Exception {
		node.setSourceFileName("Foo.java");

		page = new ClassPage(node, null, new SourceLink(), rootFolder, context);
		page.render();

		final Document doc = support.parse(output.getFile("Foo.html"));
		assertEquals("", support.findStr(doc, "/html/body/p[1]"));
	}

	@Test
	public void should_generate_message_when_no_lines() throws Exception {
		node = new ClassCoverageImpl("Foo", 123, false);
		node.addMethod(new MethodCoverageImpl("m", "()V", null));

		page = new ClassPage(node, null, new SourceLink(), rootFolder, context);
		page.render();

		final Document doc = support.parse(output.getFile("Foo.html"));
		assertEquals(
				"Class files must be compiled with debug information to show line coverage.",
				support.findStr(doc, "/html/body/p[1]"));
	}

	@Test
	public void should_generate_message_when_class_id_mismatch()
			throws Exception {
		node = new ClassCoverageImpl("Foo", 123, true);
		node.addMethod(new MethodCoverageImpl("m", "()V", null));

		page = new ClassPage(node, null, new SourceLink(), rootFolder, context);
		page.render();

		final Document doc = support.parse(output.getFile("Foo.html"));
		assertEquals("A different version of class was executed at runtime.",
				support.findStr(doc, "/html/body/p[1]"));
	}

	private class SourceLink implements ILinkable {

		public String getLink(final ReportOutputFolder base) {
			return "Source.java.html";
		}

		public String getLinkLabel() {
			return "";
		}

		public String getLinkStyle() {
			return null;
		}

	}

	@Test
	public void testGetFileName() throws IOException {
		page = new ClassPage(node, null, null, rootFolder, context);
		assertEquals("Foo.html", page.getFileName());
	}

	@Test
	public void testGetFileNameDefault() throws IOException {
		IClassCoverage defaultNode = new ClassCoverageImpl("Foo", 123, false);
		page = new ClassPage(defaultNode, null, null, rootFolder, context);
		assertEquals("Foo.html", page.getFileName());
	}

	@Test
	public void testGetLinkLabel() throws IOException {
		page = new ClassPage(node, null, null, rootFolder, context);
		assertEquals("Foo", page.getLinkLabel());
	}

}
