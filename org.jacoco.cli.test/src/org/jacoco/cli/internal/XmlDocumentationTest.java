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
package org.jacoco.cli.internal;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Unit tests for {@link XmlDocumentation}.
 */
public class XmlDocumentationTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private DocumentBuilder builder;
	private XPath xpath;

	@Before
	public void before() throws Exception {
		final DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		builder = builderFactory.newDocumentBuilder();
		builder.setErrorHandler(new ErrorHandler() {
			public void error(SAXParseException exception) throws SAXException {
				fail(exception.getMessage());
			}

			public void fatalError(SAXParseException exception)
					throws SAXException {
				fail(exception.getMessage());
			}

			public void warning(SAXParseException exception)
					throws SAXException {
				fail(exception.getMessage());
			}
		});

		xpath = XPathFactory.newInstance().newXPath();
	}

	@Test
	public void should_create_documentation() throws Exception {
		File file = new File(tmp.getRoot(), "doc.xml");

		XmlDocumentation.main(file.getAbsolutePath());

		Document doc = parse(file);

		assertContains("java -jar jacococli.jar report",
				"/documentation/command[@name='report']/usage/text()", doc);

		assertContains("Generate reports",
				"/documentation/command[@name='report']/description/text()",
				doc);

		assertContains("<execfiles>",
				"/documentation/command[@name='report']/option[1]/usage/text()",
				doc);

		assertContains("false",
				"/documentation/command[@name='report']/option[1]/@required",
				doc);

		assertContains("true",
				"/documentation/command[@name='report']/option[1]/@multiple",
				doc);

		assertContains("-classfiles <path>",
				"/documentation/command[@name='report']/option[2]/usage/text()",
				doc);

		assertContains("true",
				"/documentation/command[@name='report']/option[2]/@multiple",
				doc);

	}

	private Document parse(File file) throws Exception {
		InputStream in = new FileInputStream(file);
		try {
			return builder.parse(new InputSource(in));
		} finally {
			in.close();
		}
	}

	private void assertContains(String expected, String query, Document doc)
			throws XPathExpressionException {
		final String actual = eval(query, doc);
		assertTrue(actual, actual.contains(expected));
	}

	private String eval(String query, Document doc)
			throws XPathExpressionException {
		return (String) xpath.evaluate(query, doc, XPathConstants.STRING);
	}

}
