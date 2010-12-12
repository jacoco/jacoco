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
package org.jacoco.report.xml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.jacoco.core.analysis.CounterImpl;
import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.analysis.MethodCoverage;
import org.jacoco.report.IReportVisitor;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Unit tests for {@link XMLReportNodeHandler}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class XMLReportNodeHandlerTest {

	private XMLElement root;

	private StringWriter buffer;

	private XMLSupport support;

	private XMLReportNodeHandler handler;

	@Before
	public void setup() throws Exception {
		buffer = new StringWriter();
		support = new XMLSupport(XMLReportNodeHandler.class);
		root = new XMLDocument("report", "-//JACOCO//DTD Report 1.0//EN",
				"report.dtd", "UTF-8", true, buffer);
		handler = new XMLReportNodeHandler(root, new CoverageNodeImpl(
				ElementType.GROUP, "Sample", false));
	}

	@Test
	public void testRoot() throws Exception {
		final Document doc = getDocument();
		assertEquals("Sample", support.findStr(doc, "//report/@name"));
	}

	@Test
	public void testGroup() throws Exception {
		handler.visitChild(
				new CoverageNodeImpl(ElementType.GROUP, "Group1", false))
				.visitEnd(null);
		final Document doc = getDocument();
		assertEquals("Group1", support.findStr(doc, "//report/group/@name"));
	}

	@Test
	public void testCounters() throws Exception {
		final CoverageNodeImpl node = new CoverageNodeImpl(ElementType.GROUP,
				"Group1", false) {
			{
				classCounter = CounterImpl.getInstance(10, 1);
				methodCounter = CounterImpl.getInstance(20, 2);
				blockCounter = CounterImpl.getInstance(30, 3);
				instructionCounter = CounterImpl.getInstance(40, 4);
				lineCounter = CounterImpl.getInstance(50, 5);
			}
		};
		handler.visitChild(node).visitEnd(null);
		final Document doc = getDocument();
		assertEquals("1", support.findStr(doc,
				"//report/group/counter[@type='CLASS']/@covered"));
		assertEquals("9", support.findStr(doc,
				"//report/group/counter[@type='CLASS']/@missed"));
		assertEquals("2", support.findStr(doc,
				"//report/group/counter[@type='METHOD']/@covered"));
		assertEquals("18", support.findStr(doc,
				"//report/group/counter[@type='METHOD']/@missed"));
		assertEquals("3", support.findStr(doc,
				"//report/group/counter[@type='BLOCK']/@covered"));
		assertEquals("27", support.findStr(doc,
				"//report/group/counter[@type='BLOCK']/@missed"));
		assertEquals("4", support.findStr(doc,
				"//report/group/counter[@type='INSTRUCTION']/@covered"));
		assertEquals("36", support.findStr(doc,
				"//report/group/counter[@type='INSTRUCTION']/@missed"));
		assertEquals("5", support.findStr(doc,
				"//report/group/counter[@type='LINE']/@covered"));
		assertEquals("45", support.findStr(doc,
				"//report/group/counter[@type='LINE']/@missed"));
	}

	@Test
	public void testPackage() throws Exception {
		handler.visitChild(
				new CoverageNodeImpl(ElementType.PACKAGE, "org.jacoco.example",
						false)).visitEnd(null);
		final Document doc = getDocument();
		assertEquals("org.jacoco.example",
				support.findStr(doc, "//report/package/@name"));
	}

	@Test
	public void testClass() throws Exception {
		final IReportVisitor packageHandler = handler
				.visitChild(new CoverageNodeImpl(ElementType.PACKAGE,
						"org.jacoco.example", false));
		packageHandler.visitChild(
				new CoverageNodeImpl(ElementType.CLASS, "Foo", true)).visitEnd(
				null);
		packageHandler.visitEnd(null);
		final Document doc = getDocument();
		assertEquals("Foo",
				support.findStr(doc, "//report/package/class/@name"));
	}

	@Test
	public void testMethod() throws Exception {
		final IReportVisitor packageHandler = handler
				.visitChild(new CoverageNodeImpl(ElementType.PACKAGE,
						"org.jacoco.example", false));
		final IReportVisitor classHandler = packageHandler
				.visitChild(new CoverageNodeImpl(ElementType.CLASS, "Foo", true));
		MethodCoverage node = new MethodCoverage("doit", "()V", null);
		node.addInsn(false, 15);
		node.addInsn(false, 16);
		node.addInsn(false, 16);
		classHandler.visitChild(node).visitEnd(null);
		classHandler.visitEnd(null);
		packageHandler.visitEnd(null);
		final Document doc = getDocument();
		assertEquals("doit",
				support.findStr(doc, "//report/package/class/method/@name"));
		assertEquals("()V",
				support.findStr(doc, "//report/package/class/method/@desc"));
		assertEquals("15",
				support.findStr(doc, "//report/package/class/method/@line"));
	}

	@Test
	public void testSourcefile() throws Exception {
		final IReportVisitor packageHandler = handler
				.visitChild(new CoverageNodeImpl(ElementType.PACKAGE,
						"org.jacoco.example", false));
		final CoverageNodeImpl node = new CoverageNodeImpl(
				ElementType.SOURCEFILE, "Foo.java", true) {
			{
				lines.increment(11, false);
				lines.increment(13, false);
				lines.increment(13, true);
				lines.increment(14, true);
			}
		};
		packageHandler.visitChild(node).visitEnd(null);
		packageHandler.visitEnd(null);
		final Document doc = getDocument();
		assertEquals("Foo.java",
				support.findStr(doc, "//report/package/sourcefile/@name"));
		assertEquals("11",
				support.findStr(doc, "//report/package/sourcefile/line[1]/@nr"));
		assertEquals("N", support.findStr(doc,
				"//report/package/sourcefile/line[1]/@status"));
		assertEquals("13",
				support.findStr(doc, "//report/package/sourcefile/line[2]/@nr"));
		assertEquals("P", support.findStr(doc,
				"//report/package/sourcefile/line[2]/@status"));
		assertEquals("14",
				support.findStr(doc, "//report/package/sourcefile/line[3]/@nr"));
		assertEquals("F", support.findStr(doc,
				"//report/package/sourcefile/line[3]/@status"));
	}

	private Document getDocument() throws SAXException, IOException,
			ParserConfigurationException {
		handler.visitEnd(null);
		return support.parse(buffer.toString());
	}

}
