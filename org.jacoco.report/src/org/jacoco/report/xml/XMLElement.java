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

import java.io.IOException;
import java.io.Writer;

/**
 * Simple API to create well formed XML streams. A {@link XMLElement} instance
 * represents a single element in a XML document.
 * 
 * @see XMLDocument
 * @author Marc R. Hoffmann
 * @version $Revision: $
 * 
 * @param <E>
 *            Type of a subclass
 */
public class XMLElement {

	private static final char SPACE = ' ';

	private static final char EQ = '=';

	private static final char LT = '<';

	private static final char GT = '>';

	private static final char QUOT = '"';

	private static final char AMP = '&';

	private static final char SLASH = '/';

	/** Writer for content output */
	protected final Writer writer;

	private final String name;

	private boolean openTagDone;

	private boolean closed;

	private XMLElement lastchild;

	/**
	 * Creates a new element for a XML document.
	 * 
	 * @param writer
	 *            all output will be written directly to this
	 * @param name
	 *            element name
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	protected XMLElement(Writer writer, String name) throws IOException {
		this.writer = writer;
		this.name = name;
		this.openTagDone = false;
		this.closed = false;
		this.lastchild = null;
		writer.write(LT);
		writer.write(name);
	}

	private void finishOpenTag() throws IOException {
		if (!openTagDone) {
			writer.append(GT);
			openTagDone = true;
		}
	}

	private void quote(String text) throws IOException {
		int len = text.length();
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			switch (c) {
			case LT:
				writer.write("&lt;");
				break;
			case GT:
				writer.write("&gt;");
				break;
			case QUOT:
				writer.write("&quot;");
				break;
			case AMP:
				writer.write("&amp;");
				break;
			default:
				writer.write(c);
			}
		}
	}

	/**
	 * Adds an attribute to this element. May only be called before an child
	 * element is added or this element has been closed. The attribute value
	 * will be quoted.
	 * 
	 * @param name
	 *            attribute name
	 * @param value
	 *            attribute value
	 * 
	 * @return this element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public XMLElement attr(String name, String value) throws IOException {
		if (closed || openTagDone) {
			throw new IOException("Element " + this.name + " already closed.");
		}
		writer.write(SPACE);
		writer.write(name);
		writer.write(EQ);
		writer.write(QUOT);
		quote(value);
		writer.write(QUOT);
		return this;
	}

	/**
	 * Adds the given text as a child to this node. The text will be quoted.
	 * 
	 * @param text
	 *            text to add
	 * @return this element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public XMLElement text(String text) throws IOException {
		if (closed) {
			throw new IOException("Element " + name + " already closed.");
		}
		finishOpenTag();
		if (lastchild != null) {
			lastchild.close();
		}
		quote(text);
		return this;
	}

	/**
	 * Creates a new child element for this element,
	 * 
	 * @param name
	 *            name of the child element
	 * @return child element instance
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public XMLElement element(String name) throws IOException {
		if (closed) {
			throw new IOException("Element " + name + " already closed.");
		}
		finishOpenTag();
		if (lastchild != null) {
			lastchild.close();
		}
		return lastchild = new XMLElement(writer, name);
	}

	/**
	 * Closes this element if it has not been closed before.
	 * 
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public void close() throws IOException {
		if (!closed) {
			if (lastchild != null) {
				lastchild.close();
			}
			if (openTagDone) {
				writer.write(LT);
				writer.write(SLASH);
				writer.write(name);
			} else {
				writer.write(SLASH);
			}
			writer.write(GT);
			closed = true;
			openTagDone = true;
		}
	}

}
