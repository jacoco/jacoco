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
package org.jacoco.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

/**
 * Configurable output formats for the report goals.
 */
public enum ReportFormat {

	/**
	 * Multi-page html report.
	 */
	HTML() {
		@Override
		IReportVisitor createVisitor(final AbstractReportMojo mojo,
				final Locale locale) throws IOException {
			final HTMLFormatter htmlFormatter = new HTMLFormatter();
			htmlFormatter.setOutputEncoding(mojo.outputEncoding);
			htmlFormatter.setLocale(locale);
			if (mojo.footer != null) {
				htmlFormatter.setFooterText(mojo.footer);
			}
			return htmlFormatter.createVisitor(
					new FileMultiReportOutput(mojo.getOutputDirectory()));
		}
	},

	/**
	 * Single-file XML report.
	 */
	XML() {
		@Override
		IReportVisitor createVisitor(final AbstractReportMojo mojo,
				final Locale locale) throws IOException {
			final XMLFormatter xml = new XMLFormatter();
			xml.setOutputEncoding(mojo.outputEncoding);
			return xml.createVisitor(new FileOutputStream(
					new File(mojo.getOutputDirectory(), "jacoco.xml")));
		}
	},

	/**
	 * Single-file CSV report.
	 */
	CSV() {
		@Override
		IReportVisitor createVisitor(final AbstractReportMojo mojo,
				final Locale locale) throws IOException {
			final CSVFormatter csv = new CSVFormatter();
			csv.setOutputEncoding(mojo.outputEncoding);
			return csv.createVisitor(new FileOutputStream(
					new File(mojo.getOutputDirectory(), "jacoco.csv")));
		}
	};

	abstract IReportVisitor createVisitor(AbstractReportMojo mojo,
			final Locale locale) throws IOException;

}
