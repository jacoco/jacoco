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
package org.jacoco.report;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Implementation of {@link IMultiReportOutput} that writes files into a
 * {@link ZipOutputStream}.
 */
public class ZipMultiReportOutput implements IMultiReportOutput {

	private final ZipOutputStream zip;

	private OutputStream currentEntry;

	/**
	 * Creates a new instance based on the given {@link ZipOutputStream}.
	 *
	 * @param zip
	 *            stream to write file entries to
	 */
	public ZipMultiReportOutput(final ZipOutputStream zip) {
		this.zip = zip;
	}

	/**
	 * Creates a new instance based on the given {@link OutputStream}.
	 *
	 * @param out
	 *            stream to write file entries to
	 */
	public ZipMultiReportOutput(final OutputStream out) {
		this(new ZipOutputStream(out));
	}

	public OutputStream createFile(final String path) throws IOException {
		if (currentEntry != null) {
			currentEntry.close();
		}
		final ZipEntry entry = new ZipEntry(path);
		zip.putNextEntry(entry);
		currentEntry = new EntryOutput();
		return currentEntry;
	}

	public void close() throws IOException {
		zip.close();
	}

	private final class EntryOutput extends OutputStream {

		private boolean closed = false;

		@Override
		public void write(final byte[] b, final int off, final int len)
				throws IOException {
			ensureNotClosed();
			zip.write(b, off, len);
		}

		@Override
		public void write(final byte[] b) throws IOException {
			ensureNotClosed();
			zip.write(b);
		}

		@Override
		public void write(final int b) throws IOException {
			ensureNotClosed();
			zip.write(b);
		}

		@Override
		public void flush() throws IOException {
			ensureNotClosed();
			zip.flush();
		}

		@Override
		public void close() throws IOException {
			if (!closed) {
				closed = true;
				zip.closeEntry();
			}
		}

		private void ensureNotClosed() throws IOException {
			if (closed) {
				throw new IOException("Zip entry already closed.");
			}
		}

	}

}
