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
package org.jacoco.report.xml;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Test utility to parse, validate and query XML documents.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class XMLSupport {

	private final DocumentBuilder builder;

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

	public void validate(String document) throws SAXException, IOException,
			ParserConfigurationException {
		builder.parse(new InputSource(new StringReader(document)));
	}

}
