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
package org.jacoco.report.html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.jacoco.report.xml.XMLDocument;

/**
 * {@link XMLDocument} that declares its content type to be XHTML 1.0 Strict and
 * produces {@link HTMLElement}s as children.
 * 
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class HTMLDocument extends XMLDocument {

	private static final String ROOT = "html";

	private static final String PUBID = "-//W3C//DTD XHTML 1.0 Strict//EN";

	private static final String SYSTEM = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";

	/**
	 * Creates a new HTML document based on the given writer.
	 * 
	 * @param writer
	 *            writer for content output
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLDocument(final Writer writer) throws IOException {
		super(ROOT, PUBID, SYSTEM, writer);
	}

	/**
	 * Creates a new HTML document based on the given stream.
	 * 
	 * @param output
	 *            stream for content output
	 * @throws IOException
	 *             in case of problems with the stream
	 */
	public HTMLDocument(final OutputStream output) throws IOException {
		super(ROOT, PUBID, SYSTEM, output);
		attr("xmlns", "http://www.w3.org/1999/xhtml");
	}

	@Override
	public HTMLElement element(final String name) throws IOException {
		final HTMLElement element = new HTMLElement(writer, name);
		addChildElement(element);
		return element;
	}

	/**
	 * Creates a 'head' element.
	 * 
	 * @return 'head' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement head() throws IOException {
		return element("head");
	}

	/**
	 * Creates a 'body' element.
	 * 
	 * @return 'body' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement body() throws IOException {
		return element("body");
	}

}
