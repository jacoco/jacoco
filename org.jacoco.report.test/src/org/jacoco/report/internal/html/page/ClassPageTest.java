/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
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
		node = new ClassCoverageImpl("org/jacoco/example/Foo", 123, false);
		node.addMethod(new MethodCoverageImpl("a", "()V", null));
		final MethodCoverageImpl methodB = new MethodCoverageImpl("b", "()V", null);
		methodB.setAccess(Opcodes.ACC_PUBLIC);
		node.addMethod(methodB);
		final MethodCoverageImpl methodC = new MethodCoverageImpl("c", "()V", null);
		methodC.setAccess(Opcodes.ACC_PRIVATE);
		node.addMethod(methodC);
		final MethodCoverageImpl methodD = new MethodCoverageImpl("d", "()V", null);
		methodD.setAccess(Opcodes.ACC_PROTECTED);
		node.addMethod(methodD);
	}

	@Test
	public void testContents() throws Exception {
		page = new ClassPage(node, null, null, rootFolder, context);
		page.render();

		final Document doc = support.parse(output.getFile("Foo.html"));
		assertEquals("el_method_default", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/span/@class"));
		assertEquals("a()", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[1]/td[1]/span"));
		assertEquals("el_method_public", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[2]/td[1]/span/@class"));
		assertEquals("b()", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[2]/td[1]/span"));
		assertEquals("el_method_private", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[3]/td[1]/span/@class"));
		assertEquals("c()", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[3]/td[1]/span"));
		assertEquals("el_method_protected", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[4]/td[1]/span/@class"));
		assertEquals("d()", support.findStr(doc,
				"/html/body/table[1]/tbody/tr[4]/td[1]/span"));
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
