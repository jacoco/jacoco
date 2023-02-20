/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.csv;

import java.io.IOException;
import java.io.Writer;

/**
 * Helper class for writing out CSV or tab delimited files.
 * <p>
 * <strong>Example Usage:</strong>
 *
 * <pre>
 * delimitedWriter.writeFields(&quot;header1&quot;, &quot;header2&quot;, ...);
 * for each line to be written {
 *   delimitedWriter.writeField(value1);
 *   delimitedWriter.writeField(value2);
 *   delimitedWriter.nextLine();
 * }
 * delimitedWriter.close();
 * </pre>
 *
 * </p>
 */
class DelimitedWriter {
	private static final String QUOTE = "\"";
	private static final String ESCAPED_QUOTE = "\"\"";

	private static final char DEFAULT_DELIMITER = ',';
	private static final String NEW_LINE = System.getProperty("line.separator");
	private final char delimiter;
	private final Writer delegate;
	private int fieldPosition = 0;

	/**
	 * Creates a new Delimited writer using the default delimiter
	 *
	 * @param delegate
	 *            Writer to delegate all writes to
	 */
	public DelimitedWriter(final Writer delegate) {
		this(delegate, DEFAULT_DELIMITER);
	}

	/**
	 * Creates a new Delimited writer using the default delimiter
	 *
	 * @param delegate
	 *            Writer to delegate all writes to
	 * @param delimiter
	 *            delimiter to use (usually a comma, tab or space)
	 */
	public DelimitedWriter(final Writer delegate, final char delimiter) {
		this.delegate = delegate;
		this.delimiter = delimiter;
	}

	/**
	 * Write multiple fields at once. Values will be auto escaped and quoted as
	 * needed. Each value will be separated using the current delimiter
	 *
	 * @param fields
	 *            Values to write
	 * @throws IOException
	 *             Error writing to the underlying writer object
	 */
	public void write(final String... fields) throws IOException {
		for (final String field : fields) {
			write(field);
		}
	}

	/**
	 * Write a single value. Values will be auto escaped and quoted as needed.
	 * If this is not the first field of the current line the value will be
	 * prepended with the current delimiter
	 *
	 * @param field
	 *            Value to write
	 * @throws IOException
	 *             Error writing to the underlying writer object
	 */
	public void write(final String field) throws IOException {
		if (fieldPosition != 0) {
			delegate.write(delimiter);
		}
		delegate.write(escape(field));
		fieldPosition++;
	}

	/**
	 * Write a single integer value.
	 *
	 * @param value
	 *            Value to write
	 * @throws IOException
	 *             Error writing to the underlying writer object
	 */
	public void write(final int value) throws IOException {
		write(Integer.toString(value));
	}

	/**
	 * Write muliple integer values
	 *
	 * @param values
	 *            values to write
	 * @throws IOException
	 *             Error writing to the underlying writer object
	 */
	public void write(final int... values) throws IOException {
		for (final int value : values) {
			write(Integer.toString(value));
		}
	}

	/**
	 * Output a new line and advance the writer to the next line. The line
	 * delimiter is the default for the platform.
	 *
	 * @throws IOException
	 *             Error writing to the underlying writer object
	 */
	public void nextLine() throws IOException {
		delegate.write(NEW_LINE);
		fieldPosition = 0;
	}

	/**
	 * Close the underlying writer object. Once closed all write operations will
	 * fail
	 *
	 * @throws IOException
	 *             Error closing the underlying writer object
	 */
	public void close() throws IOException {
		delegate.close();
	}

	/**
	 * Escapes any occurrences of the quote character in value by replacing it
	 * with a double quote. Also Quotes the value if a quote or delimiter value
	 * is found.
	 *
	 * @param value
	 *            String that needs escaping
	 * @return New string with all values escaped
	 */
	private String escape(final String value) {
		String escapedValue = value;

		// Escape and quote if the source value contains the delimiter
		// or the quote character
		if (value.indexOf(QUOTE) != -1 || value.indexOf(delimiter) != -1) {
			escapedValue = value.replace(QUOTE, ESCAPED_QUOTE);
			escapedValue = QUOTE + escapedValue + QUOTE;
		}

		return escapedValue;
	}
}
