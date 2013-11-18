/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Keeping - initial implementation
 *
 *******************************************************************************/
package org.jacoco.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import org.jacoco.core.runtime.WildcardMatcher;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Command-line "instrument" command implementation.
 */
public class Instrument {

	private final FilteredInstrumenter instrumenter;

	private final File srcdir;
	private final File destdir;

	/**
	 * Constructor.
	 * 
	 * @param options
	 *            the options to be applied when instrumenting the classes.
	 */
	public Instrument(final InstrumentOptions options) {
		this.srcdir = options.getSrcdir();
		this.destdir = options.getDestdir();
		instrumenter = new FilteredInstrumenter(
				new OfflineInstrumentationAccessGenerator(),
				new WildcardMatcher(options.getIncludeExpr()),
				new WildcardMatcher(options.getExcludeExpr()));
	}

	/**
	 * Runs the instrumentation task.
	 * 
	 * @return the number of classes instrumented.
	 * @throws IOException
	 *             if the task fails to read/write a class.
	 */
	public int instrumentAll() throws IOException {
		final int result = instrumentRecursive(srcdir, destdir);
		return result + instrumenter.getAdjustment();
	}

	private int instrumentRecursive(final File src, final File dest)
			throws IOException {
		int total = 0;
		if (src.isDirectory()) {
			for (final File child : src.listFiles()) {
				total += instrumentRecursive(child,
						new File(dest, child.getName()));
			}
		} else {
			total += instrument(src, dest);
		}
		return total;
	}

	private int instrument(final File src, final File dest) throws IOException {
		final File destParent = dest.getParentFile();
		// Be careful to avoid a race by trying to construct the output
		// directory regardless of whether it already exists. Only fail if
		// we did not create it *and* it does not exist.
		if (!destParent.mkdirs() && !destParent.exists()) {
			throw new IOException("failed to create directory: " + destParent);
		}
		final InputStream input = new FileInputStream(src);
		try {
			final OutputStream output = new FileOutputStream(dest);
			try {
				return instrumenter.instrumentAll(input, output);
			} finally {
				output.close();
			}
		} catch (final IOException e) {
			dest.delete();
			throw e;
		} finally {
			input.close();
		}
	}

	/**
	 * @param args
	 *            the command-line arguments.
	 */
	public static void main(final String[] args) {
		final InstrumentOptions options = new InstrumentOptions();
		final CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getLocalizedMessage());
			parser.printUsage(System.err);
			System.exit(1);
		}

		try {
			final int total = new Instrument(options).instrumentAll();
			System.out.println(MessageFormat.format("{0} classes instrumented",
					Integer.valueOf(total)));
		} catch (final IOException e) {
			System.err.println("Failed: " + e.getLocalizedMessage());
			System.exit(1);
		}
	}

}
