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
package org.jacoco.core.test.perf;

import static java.lang.String.format;

import java.io.PrintWriter;

import org.jacoco.core.JaCoCo;

/**
 * Formatted text output.
 */
public class PerfOutputWriter implements IPerfOutput {

	private final PrintWriter writer;

	public PerfOutputWriter(final PrintWriter writer) {
		this.writer = writer;
		writeHeader();
	}

	private void writeHeader() {
		writer.printf("JaCoCo Performance Data%n%n");
		writer.printf("JaCoCo Version:  %s%n", JaCoCo.VERSION);
		writer.printf("JVM Vendor:      %s%n",
				System.getProperty("java.vm.vendor"));
		writer.printf("JVM Version:     %s%n%n",
				System.getProperty("java.vm.version"));
		writer.println(
				"scenario                         instr     ref    overhead");
		writer.println(
				"----------------------------------------------------------");
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
		writer.printf("%-30s%8s         %-6s%n", description,
				format(fmt, Double.valueOf(subject)), unit);
	}

	private void writeResult(final String description, final double subject,
			final double reference, String fmt, String unit) {
		double overhead = 100 * (subject - reference) / reference;
		writer.printf("%-30s%8s%8s %-6s%4.0f%%%n", description,
				format(fmt, Double.valueOf(subject)),
				format(fmt, Double.valueOf(reference)), unit,
				Double.valueOf(overhead));
	}

}
