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
package org.jacoco.core.test.perf;

import static java.lang.String.format;

import java.io.PrintWriter;

/**
 * Formatted text output.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class PerfOutputWriter implements IPerfOutput {

	private final PrintWriter writer;

	public PerfOutputWriter(final PrintWriter writer) {
		this.writer = writer;
		writeHeader();
	}

	private void writeHeader() {
		writer
				.println("scenario                         instr     ref    overhead");
		writer
				.println("----------------------------------------------------------");
	}

	public void writeTimeResult(final String description, final long duration,
			final long reference) {
		final double dms = (double) duration / 1000000;
		if (reference == NO_REFERENCE) {
			writeResult(description, dms, "%.2f", "ms");
		} else {
			final double rms = (double) reference / 1000000;
			writeResult(description, dms, rms, "%.2f", "ms");
		}
	}

	public void writeByteResult(String description, long size, long reference) {
		if (size == 0) {
			return;
		}
		if (reference == NO_REFERENCE) {
			writeResult(description, size, "%.0f", "bytes");
		} else {
			writeResult(description, size, reference, "%.0f", "bytes");
		}
	}

	private void writeResult(final String description, final double subject,
			String fmt, String unit) {
		writer.printf("%-30s%8s         %-6s", description, format(fmt, Double
				.valueOf(subject)), unit);
		writer.println();
	}

	private void writeResult(final String description, final double subject,
			final double reference, String fmt, String unit) {
		double overhead = 100 * (subject - reference) / reference;
		writer.printf("%-30s%8s%8s %-6s%4.0f%%", description, format(fmt,
				Double.valueOf(subject)),
				format(fmt, Double.valueOf(reference)), unit, Double
						.valueOf(overhead));
		writer.println();
	}

}
