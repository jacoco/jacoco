/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal.xml;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Test utility to parse, validate and query XML documents.
 */
public class XMLSupport {

	private final DocumentBuilder builder;

	private XPath xpath;

	public XMLSupport(Class<?> resourceDelegate)
			throws ParserConfigurationException {
		final DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		builderFactory.setValidating(true);
		builder = builderFactory.newDocumentBuilder();
		builder.setEntityResolver(new LocalEntityResolver(resourceDelegate));
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
	}

	private XPath getXPath() {
		if (xpath == null) {
			xpath = XPathFactory.newInstance().newXPath();
		}
		return xpath;
	}

	public Document parse(ByteArrayOutputStream buffer)
			throws SAXException, IOException, ParserConfigurationException {
		return parse(buffer.toByteArray());
	}

	public Document parse(byte[] document)
			throws SAXException, IOException, ParserConfigurationException {
		return builder
				.parse(new InputSource(new ByteArrayInputStream(document)));
	}

	public String findStr(final Document doc, final String query)
			throws XPathExpressionException {
		return (String) getXPath().evaluate(query, doc, XPathConstants.STRING);
	}

}
