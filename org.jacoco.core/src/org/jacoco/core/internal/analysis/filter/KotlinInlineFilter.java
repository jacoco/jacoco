/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters out instructions that were inlined by Kotlin compiler.
 */
public final class KotlinInlineFilter implements IFilter {

	private int firstGeneratedLineNumber = -1;

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (context.getSourceDebugExtension() == null) {
			return;
		}

		if (!KotlinGeneratedFilter.isKotlinClass(context)) {
			return;
		}

		if (firstGeneratedLineNumber == -1) {
			firstGeneratedLineNumber = getFirstGeneratedLineNumber(
					context.getSourceFileName(),
					context.getSourceDebugExtension());
		}

		int line = 0;
		for (AbstractInsnNode i = methodNode.instructions
				.getFirst(); i != null; i = i.getNext()) {
			if (AbstractInsnNode.LINE == i.getType()) {
				line = ((LineNumberNode) i).line;
			}
			if (line >= firstGeneratedLineNumber) {
				output.ignore(i, i);
			}
		}
	}

	private static int getFirstGeneratedLineNumber(final String sourceFileName,
			final String smap) {
		try {
			final BufferedReader br = new BufferedReader(
					new StringReader(smap));
			expectLine(br, "SMAP");
			// OutputFileName
			expectLine(br, sourceFileName);
			// DefaultStratumId
			expectLine(br, "Kotlin");
			// StratumSection
			expectLine(br, "*S Kotlin");
			// FileSection
			expectLine(br, "*F");
			int sourceFileId = -1;
			String line;
			while (!"*L".equals(line = br.readLine())) {
				// AbsoluteFileName
				br.readLine();

				final Matcher m = FILE_INFO_PATTERN.matcher(line);
				if (!m.matches()) {
					throw new IllegalStateException(
							"Unexpected SMAP line: " + line);
				}
				final String fileName = m.group(2);
				if (fileName.equals(sourceFileName)) {
					sourceFileId = Integer.parseInt(m.group(1));
				}
			}
			// LineSection
			int min = Integer.MAX_VALUE;
			while (!"*E".equals(line = br.readLine())) {
				final Matcher m = LINE_INFO_PATTERN.matcher(line);
				if (!m.matches()) {
					throw new IllegalStateException(
							"Unexpected SMAP line: " + line);
				}
				final int inputStartLine = Integer.parseInt(m.group(1));
				final int lineFileID = Integer
						.parseInt(m.group(2).substring(1));
				final int outputStartLine = Integer.parseInt(m.group(4));
				if (sourceFileId == lineFileID
						&& inputStartLine == outputStartLine) {
					continue;
				}
				min = Math.min(outputStartLine, min);
			}
			return min;
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
