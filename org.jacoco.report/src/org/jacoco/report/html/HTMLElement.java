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

}
