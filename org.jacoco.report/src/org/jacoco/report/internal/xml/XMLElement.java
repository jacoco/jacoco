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
package org.jacoco.report.internal.xml;

import static java.lang.String.format;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Simple API to create well formed XML streams with minimal memory overhead. A
 * {@link XMLElement} instance represents a single element in a XML document.
 * {@link XMLElement} can be used directly or might be subclassed for schema
 * specific convenience methods.
 */
public class XMLElement {

	/** XML header template */
	private static final String HEADER = "<?xml version=\"1.0\" encoding=\"%s\"?>";

	/** XML header template for standalone documents */
	private static final String HEADER_STANDALONE = "<?xml version=\"1.0\" encoding=\"%s\" standalone=\"yes\"?>";

	/** DOCTYPE declaration template */
	private static final String DOCTYPE = "<!DOCTYPE %s PUBLIC \"%s\" \"%s\">";

	/** Writer for content output */
	protected final Writer writer;

	private final String name;

	private boolean openTagDone;

	private boolean closed;

	private XMLElement lastchild;

	private final boolean root;

	private XMLElement(final Writer writer, final String name,
			final boolean root) throws IOException {
		this.writer = writer;
		this.name = name;
		this.openTagDone = false;
		this.closed = false;
		this.lastchild = null;
		this.root = root;
	}

	/**
	 * Creates a root element of a XML document.
	 *
	 * @param name
	 *            element name
	 * @param pubId
	 *            optional schema public identifier
	 * @param system
	 *            optional schema system identifier
	 * @param standalone
	 *            if <code>true</code> the document is declared as standalone
	 * @param encoding
	 *            character encoding used for output
	 * @param output
	 *            output stream will be closed if the root element is closed
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public XMLElement(final String name, final String pubId,
			final String system, final boolean standalone,
			final String encoding, final OutputStream output)
			throws IOException {
		this(new OutputStreamWriter(output, encoding), name, true);
		if (standalone) {
			writer.write(format(HEADER_STANDALONE, encoding));
		} else {
			writer.write(format(HEADER, encoding));
		}
		if (pubId != null) {
			writer.write(format(DOCTYPE, name, pubId, system));
		}
		writer.write('<');
		writer.write(name);
	}

	/**
	 * Creates a new child element within a XML document. May only be called
	 * before the parent element has been closed.
	 *
	 * @param name
	 *            element name
	 * @param parent
	 *            parent of this element
	 * @throws IOException
	 *             in case of problems with the underlying output or if the
	 *             parent element is already closed
	 */
	protected XMLElement(final String name, final XMLElement parent)
			throws IOException {
		this(parent.writer, name, false);
		parent.addChildElement(this);
		writer.write('<');
		writer.write(name);
	}

	private void addChildElement(final XMLElement child) throws IOException {
		if (closed) {
			throw new IOException(format("Element %s already closed.", name));
		}
		finishOpenTag();
		if (lastchild != null) {
			lastchild.close();
		}
		lastchild = child;
	}

	private void finishOpenTag() throws IOException {
		if (!openTagDone) {
			writer.append('>');
			openTagDone = true;
		}
	}

	private void quote(final String text) throws IOException {
		final int len = text.length();
		for (int i = 0; i < len; i++) {
			final char c = text.charAt(i);
			switch (c) {
			case '<':
				writer.write("&lt;");
				break;
			case '>':
				writer.write("&gt;");
				break;
			case '"':
				writer.write("&quot;");
				break;
			case '&':
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
	 * @throws IOException
	 *             in case of problems with the underlying output or if the
	 *             element is already closed.
	 */
	public final void attr(final String name, final String value)
			throws IOException {
		if (value == null) {
			return;
		}
		if (closed || openTagDone) {
			throw new IOException(
					format("Element %s already closed.", this.name));
		}
		writer.write(' ');
		writer.write(name);
		writer.write('=');
		writer.write('"');
		quote(value);
		writer.write('"');
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
	 * @throws IOException
	 *             in case of problems with the underlying output or if the
	 *             element is already closed.
	 */
	public final void attr(final String name, final int value)
			throws IOException {
		attr(name, String.valueOf(value));
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
	 * @throws IOException
	 *             in case of problems with the underlying output or if the
	 *             element is already closed.
	 */
	public final void attr(final String name, final long value)
			throws IOException {
		attr(name, String.valueOf(value));
	}

	/**
	 * Adds the given text as a child to this node. The text will be quoted. May
	 * only be called before this element has been closed.
	 *
	 * @param text
	 *            text to add
	 * @throws IOException
	 *             in case of problems with the underlying output or if the
	 *             element is already closed.
	 */
	public final void text(final String text) throws IOException {
		if (closed) {
			throw new IOException(format("Element %s already closed.", name));
		}
		finishOpenTag();
		if (lastchild != null) {
			lastchild.close();
		}
		quote(text);
	}

	/**
	 * Creates a new child element for this element. Might be overridden in
	 * subclasses to return a instance of the subclass.
	 *
	 * @param name
	 *            name of the child element
	 * @return child element instance
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public XMLElement element(final String name) throws IOException {
		return new XMLElement(name, this);
	}

	/**
	 * Closes this element if it has not been closed before.
	 *
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public final void close() throws IOException {
		if (!closed) {
			if (lastchild != null) {
				lastchild.close();
			}
			if (openTagDone) {
				writer.write('<');
				writer.write('/');
				writer.write(name);
			} else {
				writer.write('/');
			}
			writer.write('>');
			closed = true;
			openTagDone = true;
			if (root) {
				writer.close();
			}
		}
	}

}
