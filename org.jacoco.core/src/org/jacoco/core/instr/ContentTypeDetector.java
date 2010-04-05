/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.instr;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Detector for content types of binary streams based on a magic headers.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
class ContentTypeDetector {

	/** Header of Java class files */
	public static final int CLASSFILE = 0xcafebabe;

	/** Header of ZIP files */
	public static final int ZIPFILE = 0x504b0304;

	private static final int HEADER_SIZE = 4;

	private final InputStream in;

	private final int header;

	/**
	 * Creates a new detector based on the given input. To process the complete
	 * original input afterwards use the stream returned by
	 * {@link #getInputStream()}.
	 * 
	 * @param in
	 *            input to read the header from
	 * @throws IOException
	 */
	ContentTypeDetector(final InputStream in) throws IOException {
		if (in.markSupported()) {
			this.in = in;
		} else {
			this.in = new BufferedInputStream(in, HEADER_SIZE);
		}
		this.in.mark(HEADER_SIZE);
		this.header = readHeader(this.in);
		this.in.reset();
	}

	private static int readHeader(final InputStream in) throws IOException {
		return in.read() << 24 | in.read() << 16 | in.read() << 8 | in.read();
	}

	/**
	 * Returns an input stream instance to read the complete content (including
	 * the header) of the underlying stream.
	 * 
	 * @return input stream containing the complete content
	 */
	public InputStream getInputStream() {
		return in;
	}

	/**
	 * Returns the file header containing the magic number.
	 * 
	 * @return file header
	 */
	public int getHeader() {
		return header;
	}

}
