/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Hare Robertson - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.jacoco.core.analysis.CommentExclusionsCoverageFilter.IDirectivesParser.Directive;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.Label;

/**
 * Turn coverage on/off using ///JACOCO:OFF and ///JACOCO:ON directives.
 */
public class CommentExclusionsCoverageFilter implements ICoverageFilter {

	/**
	 * Parser of coverage directives
	 */
	public static interface IDirectivesParser {

		/**
		 * Data class representing a directive
		 */
		public static class Directive {
			/**
			 * @param lineNum
			 * @param coverageOn
			 */
			public Directive(final int lineNum, final boolean coverageOn) {
				this.lineNum = lineNum;
				this.coverageOn = coverageOn;
			}

			/**
			 * Line number of the directive
			 */
			public final int lineNum;
			/**
			 * Whether to switch coverage on/off
			 */
			public final boolean coverageOn;
		}

		/**
		 * Return coverage directives associated with the specified className
		 * 
		 * @param className
		 * @return Queue of directives in the order which they apply.
		 */
		public Queue<Directive> parseDirectives(String className);
	}

	private final IDirectivesParser parser;

	private Queue<Directive> directives;
	private boolean enabled = true;

	/**
	 * @param parser
	 *            Interface for parsing source directives.
	 */
	public CommentExclusionsCoverageFilter(final IDirectivesParser parser) {
		this.parser = parser;
	}

	public boolean enabled() {
		return enabled;
	}

	public boolean includeClass(final String className) {
		enabled = true;

		directives = parser.parseDirectives(className);
		return true;
	}

	/**
	 * Parser for directives in source code
	 */
	public static class SourceFileDirectivesParser implements IDirectivesParser {
		private final String baseDir;

		/**
		 * @param baseDir
		 *            Base directory to look for source code within
		 */
		public SourceFileDirectivesParser(final String baseDir) {
			this.baseDir = baseDir;
		}

		public Queue<Directive> parseDirectives(final String className) {

			final File sourceFile = new File(baseDir, className + ".java");

			final Queue<Directive> directives = new LinkedList<Directive>();

			if (sourceFile.exists() && sourceFile.canRead()) {
				try {
					final BufferedReader sourceReader = new BufferedReader(
							new FileReader(sourceFile));
					int lineNum = 1;
					String line;
					while ((line = sourceReader.readLine()) != null) {
						if (line.trim().equals("///JACOCO:OFF")) {
							directives.add(new Directive(lineNum, false));
						} else if (line.trim().equals("///JACOCO:ON")) {
							directives.add(new Directive(lineNum, true));
						}
						lineNum++;
					}
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}

			return directives;
		}

	}

	public MethodProbesVisitor getVisitor(final MethodProbesVisitor delegate) {

		if (directives.size() == 0) {
			return delegate;
		} else {
			return new LineNumberMethodVisitor(delegate);
		}
	}

	private class LineNumberMethodVisitor extends MethodProbesVisitor {

		private final MethodProbesVisitor delegate;

		private Directive nextDirective;

		private LineNumberMethodVisitor(final MethodProbesVisitor delegate) {
			super(delegate);
			this.delegate = delegate;
			this.nextDirective = directives.remove();
		}

		@Override
		public void visitLineNumber(final int line, final Label start) {

			if ((nextDirective != null) && (nextDirective.lineNum <= line)) {
				enabled = nextDirective.coverageOn;
				nextDirective = directives.poll();
			}

			super.visitLineNumber(line, start);
		}

		// --- Simple methods that pass on to the delegate ---

		@Override
		public void visitProbe(final int probeId) {
			delegate.visitProbe(probeId);
		}

		@Override
		public void visitJumpInsnWithProbe(final int opcode, final Label label,
				final int probeId) {
			delegate.visitJumpInsnWithProbe(opcode, label, probeId);
		}

		@Override
		public void visitInsnWithProbe(final int opcode, final int probeId) {
			delegate.visitInsnWithProbe(opcode, probeId);
		}

		@Override
		public void visitTableSwitchInsnWithProbes(final int min,
				final int max, final Label dflt, final Label[] labels) {
			delegate.visitTableSwitchInsnWithProbes(min, max, dflt, labels);
		}

		@Override
		public void visitLookupSwitchInsnWithProbes(final Label dflt,
				final int[] keys, final Label[] labels) {
			delegate.visitLookupSwitchInsnWithProbes(dflt, keys, labels);
		}
	}
}
