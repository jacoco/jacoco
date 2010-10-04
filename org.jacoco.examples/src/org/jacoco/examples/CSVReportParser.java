/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;

/**
 * Simple parser for unpacking a line of the CSV report
 * 
 * @author Brock Janiczak
 * @version $qualified.bundle.version$
 */
public class CSVReportParser {

	/**
	 * Reads all files in the argument list and prints out their name and
	 * percentage of block coverage
	 * 
	 * @param args
	 *            List of files to parse
	 * @throws Exception
	 *             Unable to parse one of the input file
	 */
	public static void main(final String[] args) throws Exception {

		for (final String arg : args) {
			System.out.println("Parsing: " + arg);
			new CSVReportParser().parse(new FileReader(arg));
		}
	}

	/**
	 * Parse an individual CSV file line at a time
	 * 
	 * @param in
	 *            Stream to read from
	 * @throws IOException
	 *             Unable to parse source
	 */
	public void parse(final Reader in) throws IOException {

		final NumberFormat percentFormat = NumberFormat.getPercentInstance();

		final BufferedReader reader = new BufferedReader(in);

		// Skip header
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			final String[] entries = line.split(",");
			final ReportLine reportLine = new ReportLine(entries);

			final long totalBlocks = reportLine.getBlockCovered()
					+ reportLine.getBlockMissed();
			final double blockPercent = (double) reportLine.getBlockCovered()
					/ (double) totalBlocks;

			System.out.println(reportLine.getPackageName() + "."
					+ reportLine.getClassName() + " "
					+ percentFormat.format(blockPercent));
		}

	}

	/**
	 * Wrapper around a line of CSV data
	 * 
	 * @author Brock Janiczak
	 * @version $qualified.bundle.version$
	 */
	public static class ReportLine {
		private final String[] entries;

		/**
		 * Creates a new Report line wrapper for the given data
		 * 
		 * @param entries
		 *            Split line data
		 */
		public ReportLine(final String[] entries) {
			this.entries = entries;
		}

		/**
		 * Gets the containing group
		 * 
		 * @return Group name
		 */
		public String getGroup() {
			return entries[0];
		}

		/**
		 * Gets the package name. Will be an String if the class lives in the
		 * default package
		 * 
		 * @return Package Name
		 */
		public String getPackageName() {
			return entries[1];
		}

		/**
		 * Gets the name of the class
		 * 
		 * @return Class name
		 */
		public String getClassName() {
			return entries[2];
		}

		/**
		 * Gets the number of methods covered
		 * 
		 * @return methods covered
		 */
		public long getMethodCovered() {
			return Long.valueOf(entries[3]).longValue();
		}

		/**
		 * Gets the number of methods missed
		 * 
		 * @return methods missed
		 */
		public long getMethodMissed() {
			return Long.valueOf(entries[4]).longValue();
		}

		/**
		 * Gets the number of blocks covered
		 * 
		 * @return blocks covered
		 */
		public long getBlockCovered() {
			return Long.valueOf(entries[5]).longValue();
		}

		/**
		 * Gets the number of blocks missed
		 * 
		 * @return blocks missed
		 */
		public long getBlockMissed() {
			return Long.valueOf(entries[6]).longValue();
		}

		/**
		 * Gets the number of lines covered
		 * 
		 * @return lines covered
		 */
		public long getLineCovered() {
			return Long.valueOf(entries[7]).longValue();
		}

		/**
		 * Gets the number of lines missed
		 * 
		 * @return lines missed
		 */
		public long getLineMissed() {
			return Long.valueOf(entries[8]).longValue();
		}

		/**
		 * Gets the number of instructions covered
		 * 
		 * @return instructions covered
		 */
		public long getInstrCovered() {
			return Long.valueOf(entries[9]).longValue();
		}

		/**
		 * Gets the number of instructions missed
		 * 
		 * @return instructions missed
		 */
		public long getInstrMissed() {
			return Long.valueOf(entries[10]).longValue();
		}
	}
}
