/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import static java.lang.String.format;

import java.io.IOException;
import java.io.Writer;

/**
 * Simple API to create well formed XML streams. A {@link XMLElement} instance
 * represents a single element in a XML document.
 * 
 * @see XMLDocument
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
	 */
	protected XMLElement(final Writer writer, final String name) {
		this.writer = writer;
		this.name = name;
		this.openTagDone = false;
		this.closed = false;
		this.lastchild = null;
	}

	/**
	 * Emits the beginning of the open tag. This method has to be called before
	 * other other methods are called on this element.
	 * 
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	protected void beginOpenTag() throws IOException {
		writer.write(LT);
		writer.write(name);
	}

	private void finishOpenTag() throws IOException {
		if (!openTagDone) {
			writer.append(GT);
			openTagDone = true;
		}
	}

	/**
	 * Adds the given child to this element. This will close all previous child
	 * elements.
	 * 
	 * @param child
	 *            child element to add
	 * @throws IOException
	 *             in case of invalid nesting or problems with the writer
	 */
	protected void addChildElement(final XMLElement child) throws IOException {
		if (closed) {
			throw new IOException(format("Element %s already closed.", name));
		}
		finishOpenTag();
		if (lastchild != null) {
			lastchild.close();
		}
		child.beginOpenTag();
		lastchild = child;
	}

	private void quote(final String text) throws IOException {
		final int len = text.length();
		for (int i = 0; i < len; i++) {
			final char c = text.charAt(i);
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
				break;
			}
		}
	}

	/**
	 * Adds an attribute to this element. May only be called before an child
	 * element is added or this element has been closed. The attribute value
	 * will be quoted. If the value is <code>null</code> the attribute will not
	 * be added.
	 * 
	 * @param name
	 *            attribute name
	 * @param value
	 *            attribute value or <code>null</code>
	 * 
	 * @return this element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public XMLElement attr(final String name, final String value)
			throws IOException {
		if (value == null) {
			return this;
		}
		if (closed || openTagDone) {
			throw new IOException(format("Element %s already closed.",
					this.name));
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
	 * Adds an attribute to this element. May only be called before an child
	 * element is added or this element has been closed. The attribute value is
	 * the decimal representation of the given int value.
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
	public XMLElement attr(final String name, final int value)
			throws IOException {
		return attr(name, String.valueOf(value));
	}

	/**
	 * Adds an attribute to this element. May only be called before an child
	 * element is added or this element has been closed. The attribute value is
	 * the decimal representation of the given long value.
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
	public XMLElement attr(final String name, final long value)
			throws IOException {
		return attr(name, String.valueOf(value));
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
	public XMLElement text(final String text) throws IOException {
		if (closed) {
			throw new IOException(format("Element %s already closed.", name));
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
	public XMLElement element(final String name) throws IOException {
		final XMLElement element = new XMLElement(writer, name);
		addChildElement(element);
		return element;
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
