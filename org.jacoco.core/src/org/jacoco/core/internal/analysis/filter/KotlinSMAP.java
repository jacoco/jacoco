/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsed representation of SourceDebugExtension attribute.
 */
public final class KotlinSMAP {

	/**
	 * Parsed representation of a single LineSection from SourceDebugExtension
	 * attribute.
	 */
	public static final class Mapping {
		private final String inputClassName;
		private final int inputStartLine;
		private final int repeatCount;
		private final int outputStartLine;

		/**
		 * Creates a new mapping.
		 *
		 * @param inputClassName
		 *            name of input class
		 * @param inputStartLine
		 *            starting line in input
		 * @param repeatCount
		 *            number of mapped lines
		 * @param outputStartLine
		 *            starting line in output
		 */
		Mapping(final String inputClassName, final int inputStartLine,
				final int repeatCount, final int outputStartLine) {
			this.inputClassName = inputClassName;
			this.inputStartLine = inputStartLine;
			this.repeatCount = repeatCount;
			this.outputStartLine = outputStartLine;
		}

		/**
		 * @return name of input class
		 */
		public String inputClassName() {
			return inputClassName;
		}

		/**
		 * @return starting line in input
		 */
		public int inputStartLine() {
			return inputStartLine;
		}

		/**
		 * @return number of mapped lines
		 */
		public int repeatCount() {
			return repeatCount;
		}

		/**
		 * @return starting line in output
		 */
		public int outputStartLine() {
			return outputStartLine;
		}
	}

	private final ArrayList<Mapping> mappings = new ArrayList<Mapping>();

	/**
	 * Returns list of mappings.
	 *
	 * @return list of mappings
	 */
	public List<Mapping> mappings() {
		return mappings;
	}

	/**
	 * Creates parsed representation of provided SourceDebugExtension attribute.
	 *
	 * @param sourceFileName
	 *            the name of the source file from which the class with SMAP was
	 *            compiled
	 * @param smap
	 *            value of SourceDebugExtension attribute to parse
	 */
	public KotlinSMAP(final String sourceFileName, final String smap) {
		try {
			final BufferedReader br = new BufferedReader(
					new StringReader(smap));
			// Header
			expectLine(br, "SMAP");
			// OutputFileName
			expectLine(br, sourceFileName);
			// DefaultStratumId
			expectLine(br, "Kotlin");
			// StratumSection
			expectLine(br, "*S Kotlin");
			// FileSection
			expectLine(br, "*F");
			final HashMap<Integer, String> inputClassNames = new HashMap<Integer, String>();
			String line;
			while (!"*L".equals(line = br.readLine())) {
				final Matcher m = FILE_INFO_PATTERN.matcher(line);
				if (!m.matches()) {
					throw new IllegalStateException(
							"Unexpected SMAP line: " + line);
				}
				final int id = Integer.parseInt(m.group(1));
				// See
				// https://github.com/JetBrains/kotlin/blob/2.0.0/compiler/backend/src/org/jetbrains/kotlin/codegen/inline/SMAP.kt#L120-L121
				// https://github.com/JetBrains/kotlin/blob/2.0.0/compiler/backend/src/org/jetbrains/kotlin/codegen/SourceInfo.kt#L38-L41
				final String className = br.readLine();
				inputClassNames.put(id, className);
			}
			// LineSection
			while (true) {
				line = br.readLine();
				if (line.equals("*E") || line.equals("*S KotlinDebug")) {
					break;
				}
				final Matcher m = LINE_INFO_PATTERN.matcher(line);
				if (!m.matches()) {
					throw new IllegalStateException(
							"Unexpected SMAP line: " + line);
				}
				final int inputStartLine = Integer.parseInt(m.group(1));
				final int lineFileID = Integer
						.parseInt(m.group(2).substring(1));
				final String repeatCountOptional = m.group(3);
				final int repeatCount = repeatCountOptional != null
						? Integer.parseInt(repeatCountOptional.substring(1))
						: 1;
				final int outputStartLine = Integer.parseInt(m.group(4));
				mappings.add(new Mapping(inputClassNames.get(lineFileID),
						inputStartLine, repeatCount, outputStartLine));
			}
		} catch (final IOException e) {
			// Must not happen with StringReader
			throw new AssertionError(e);
		}
	}

	private static void expectLine(final BufferedReader br,
			final String expected) throws IOException {
		final String line = br.readLine();
		if (!expected.equals(line)) {
			throw new IllegalStateException("Unexpected SMAP line: " + line);
		}
	}

	private static final Pattern LINE_INFO_PATTERN = Pattern.compile("" //
			+ "([0-9]++)" // InputStartLine
			+ "(#[0-9]++)?+" // LineFileID
			+ "(,[0-9]++)?+" // RepeatCount
			+ ":([0-9]++)" // OutputStartLine
			+ "(,[0-9]++)?+" // OutputLineIncrement
	);

	private static final Pattern FILE_INFO_PATTERN = Pattern.compile("" //
			+ "\\+ ([0-9]++)" // FileID
			+ " (.++)" // FileName
	);

}
