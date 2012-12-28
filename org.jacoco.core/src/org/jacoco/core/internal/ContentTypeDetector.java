/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.Opcodes;

/**
 * Detector for content types of binary streams based on a magic headers.
 */
public class ContentTypeDetector {

	/** Unknown file type */
	public static final int UNKNOWN = -1;

	/** File type Java class */
	public static final int CLASSFILE = 0xcafebabe;

	/** File type ZIP archive */
	public static final int ZIPFILE = 0x504b0304;

	private static final int BUFFER_SIZE = 8;

	private final InputStream in;

	private final int type;

	/**
	 * Creates a new detector based on the given input. To process the complete
	 * original input afterwards use the stream returned by
	 * {@link #getInputStream()}.
	 * 
	 * @param in
	 *            input to read the header from
	 * @throws IOException
	 */
	public ContentTypeDetector(final InputStream in) throws IOException {
		if (in.markSupported()) {
			this.in = in;
		} else {
			this.in = new BufferedInputStream(in, BUFFER_SIZE);
		}
		this.in.mark(BUFFER_SIZE);
		this.type = determineType(this.in);
		this.in.reset();
	}

	private static int determineType(final InputStream in) throws IOException {
		switch (readInt(in)) {
		case ZIPFILE:
			return ZIPFILE;
		case CLASSFILE:
			// also verify version to distinguish from Mach Object files:
			switch (readInt(in)) {
			case Opcodes.V1_1:
			case Opcodes.V1_2:
			case Opcodes.V1_3:
			case Opcodes.V1_4:
			case Opcodes.V1_5:
			case Opcodes.V1_6:
			case Opcodes.V1_7:
				return CLASSFILE;
			}
		}
		return UNKNOWN;
	}

	private static int readInt(final InputStream in) throws IOException {
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
	 * Returns the detected file type.
	 * 
	 * @return file type
	 */
	public int getType() {
		return type;
	}

}
