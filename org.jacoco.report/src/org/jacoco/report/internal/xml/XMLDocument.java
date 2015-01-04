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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Root element of an XML document. Each instance represents a separate output
 * document.
 * 
 * @see XMLElement
 */
public class XMLDocument extends XMLElement {

	/** XML header template */
	private static final String HEADER = "<?xml version=\"1.0\" encoding=\"%s\"?>";

	/** XML header template for standalone documents */
	private static final String HEADER_STANDALONE = "<?xml version=\"1.0\" encoding=\"%s\" standalone=\"yes\"?>";

	/** DOCTYPE declaration template */
	private static final String DOCTYPE = "<!DOCTYPE %s PUBLIC \"%s\" \"%s\">";

	/**
	 * Writes a new document to the given writer. The document might contain a
	 * document type declaration.
	 * 
	 * @param rootnode
	 *            name of the root node
	 * @param pubId
	 *            optional doctype identifier or <code>null</code>
	 * @param system
	 *            system reference, required if doctype is given
	 * @param encoding
	 *            encoding that will be specified in the header
	 * @param standalone
	 *            <code>true</code> if this is a standalone document
	 * @param writer
	 *            writer for content output
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public XMLDocument(final String rootnode, final String pubId,
			final String system, final String encoding,
			final boolean standalone, final Writer writer) throws IOException {
		super(writer, rootnode);
		writeHeader(rootnode, pubId, system, encoding, standalone, writer);
		beginOpenTag();
	}

	/**
	 * Writes a new document to the given binary stream. The document might
	 * contain a document type declaration.
	 * 
	 * @param rootnode
	 *            name of the root node
	 * @param pubId
	 *            optional doctype identifier or <code>null</code>
	 * @param system
	 *            system reference, required if doctype is given
	 * @param encoding
	 *            encoding of the XML document
	 * @param standalone
	 *            <code>true</code> if this is a standalone document
	 * @param output
	 *            output for content output
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public XMLDocument(final String rootnode, final String pubId,
			final String system, final String encoding,
			final boolean standalone, final OutputStream output)
			throws IOException {
		this(rootnode, pubId, system, encoding, standalone,
				new OutputStreamWriter(output, encoding));
	}

	@Override
	public void close() throws IOException {
		super.close();
		writer.close();
	}

	private static void writeHeader(final String rootnode, final String pubId,
			final String system, final String encoding,
			final boolean standalone, final Writer writer) throws IOException {
		if (standalone) {
			writer.write(format(HEADER_STANDALONE, encoding));
		} else {
			writer.write(format(HEADER, encoding));
		}
		if (pubId != null) {
			writer.write(format(DOCTYPE, rootnode, pubId, system));
		}
	}

}
