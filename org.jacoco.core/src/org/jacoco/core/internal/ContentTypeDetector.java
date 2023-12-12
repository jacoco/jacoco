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
package org.jacoco.core.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

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

	/** File type GZIP compressed Data */
	public static final int GZFILE = 0x1f8b0000;

	/** File type Pack200 archive */
	public static final int PACK200FILE = 0xcafed00d;

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
	 *             if the stream can't be read
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
		final int header = readInt(in);
		switch (header) {
		case ZIPFILE:
			return ZIPFILE;
		case PACK200FILE:
			return PACK200FILE;
		case CLASSFILE:
			// Mach-O fat/universal binaries have the same magic header as Java
			// class files, number of architectures is stored in unsigned 4
			// bytes in the same place and in the same big-endian order as major
			// and minor version of class file. Hopefully on practice number of
			// architectures in single executable is less than 45, which is
			// major version of Java 1.1 class files:
			final int majorVersion = readInt(in) & 0xFFFF;
			if (majorVersion >= 45) {
				return CLASSFILE;
			}
		}
		if ((header & 0xffff0000) == GZFILE) {
			return GZFILE;
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
