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
import java.io.Writer;

import org.jacoco.report.xml.XMLElement;

/**
 * A {@link XMLElement} with utility methods to create XHTML documents. It
 * provides methods of HTML tags to avoid magic strings in the generators.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class HTMLElement extends XMLElement {

	/**
	 * Creates a new element for a HTML document.
	 * 
	 * @param writer
	 *            all output will be written directly to this
	 * @param name
	 *            element name
	 */
	protected HTMLElement(final Writer writer, final String name) {
		super(writer, name);
	}

	@Override
	public HTMLElement element(final String name) throws IOException {
		final HTMLElement element = new HTMLElement(writer, name);
		addChildElement(element);
		return element;
	}

	/**
	 * Creates a 'title' element.
	 * 
	 * @return 'head' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement title() throws IOException {
		return element("title");
	}

	/**
	 * Creates a 'span' element.
	 * 
	 * @param classattr
	 *            value for the class attribute
	 * @return 'span' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement span(final String classattr) throws IOException {
		final HTMLElement pre = element("span");
		pre.attr("class", classattr);
		return pre;
	}

	/**
	 * Creates a 'div' element.
	 * 
	 * @param classattr
	 *            value for the class attribute
	 * @return 'div' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement div(final String classattr) throws IOException {
		final HTMLElement pre = element("div");
		pre.attr("class", classattr);
		return pre;
	}

	/**
	 * Creates a 'pre' element.
	 * 
	 * @param classattr
	 *            value for the class attribute
	 * @return 'pre' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement pre(final String classattr) throws IOException {
		final HTMLElement pre = element("pre");
		pre.attr("class", classattr);
		return pre;
	}

	/**
	 * Creates a empty 'br' element.
	 * 
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public void br() throws IOException {
		element("br");
	}

	/**
	 * Creates a 'td' element.
	 * 
	 * @param classattr
	 *            value for the class attribute
	 * @return 'td' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement td(final String classattr) throws IOException {
		return td(classattr, 1);
	}

	/**
	 * Creates a 'td' element.
	 * 
	 * @param colspanattr
	 *            value of the colspan attribute
	 * @return 'td' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement td(final int colspanattr) throws IOException {
		return td(null, colspanattr);
	}

	/**
	 * Creates a 'td' element.
	 * 
	 * @param classattr
	 *            value for the class attribute
	 * @param colspanattr
	 *            value of the colspan attribute
	 * @return 'td' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement td(final String classattr, final int colspanattr)
			throws IOException {
		final HTMLElement pre = element("td");
		if (classattr != null) {
			pre.attr("class", classattr);
		}
		if (colspanattr > 1) {
			pre.attr("colspan", String.valueOf(colspanattr));
		}
		return pre;
	}

	/**
	 * Creates a 'img' element.
	 * 
	 * @param srcattr
	 *            value for the src attribute
	 * @param widthattr
	 *            value for the width attribute
	 * @param heightattr
	 *            value for the height attribute
	 * @param altattr
	 *            value for the alt attribute
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public void img(final String srcattr, final int widthattr,
			final int heightattr, final String altattr) throws IOException {
		final HTMLElement pre = element("img");
		pre.attr("src", srcattr);
		pre.attr("width", String.valueOf(widthattr));
		pre.attr("height", String.valueOf(heightattr));
		pre.attr("alt", String.valueOf(altattr));
	}

}
