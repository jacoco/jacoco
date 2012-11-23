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
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Queue;

import org.jacoco.core.analysis.CommentExclusionsCoverageFilter.IDirectivesParser.Directive;
import org.jacoco.core.analysis.ICoverageFilterStatus.ICoverageFilter;
import org.jacoco.core.data.ISourceFileLocator;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
		 * @param packageName
		 * @param sourceFilename
		 * @return Queue of directives in the order which they apply.
		 */
		public Queue<Directive> parseDirectives(String packageName,
				String sourceFilename);
	}

	private final IDirectivesParser parser;

	private Queue<Directive> directives = new LinkedList<Directive>();
	private Directive nextDirective;
	private boolean enabled = true;
	private String packageName = "";
	private String sourceFilename;

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

		if (className.indexOf('/') > -1) {
			this.packageName = className.substring(0,
					className.lastIndexOf('/'));
		} else {
			this.packageName = "";
		}

		return true;
	}

	public ClassVisitor visitClass(final ClassVisitor delegate) {
		return new SourcefileClassVisitor(delegate);
	}

	/**
	 * Simple Visitor for storing the source filename
	 */
	public class SourcefileClassVisitor extends ClassVisitor {
		/**
		 * @param cv
		 */
		public SourcefileClassVisitor(final ClassVisitor cv) {
			super(Opcodes.ASM4, cv);
		}

		@Override
		public void visitSource(final String source, final String debug) {
			sourceFilename = source;
			directives = parser.parseDirectives(packageName, sourceFilename);
			nextDirective = directives.poll();
			super.visitSource(source, debug);
		}
	}

	/**
	 * Parser for directives in source code
	 */
	public static class SourceFileDirectivesParser implements IDirectivesParser {
		private final ISourceFileLocator sourceLocator;

		/**
		 * @param sourceLocator
		 *            Object for locating source code
		 */
		public SourceFileDirectivesParser(final ISourceFileLocator sourceLocator) {
			this.sourceLocator = sourceLocator;
		}

		public Queue<Directive> parseDirectives(final String packageName,
				final String sourceFilename) {
			final Queue<Directive> directives = new LinkedList<Directive>();

			try {
				final Reader sourceReader = sourceLocator.getSourceFile(
						packageName, sourceFilename);

				if (sourceReader != null) {
					final BufferedReader bufSourceReader = new BufferedReader(
							sourceReader);
					try {
						int lineNum = 1;
						String line;
						while ((line = bufSourceReader.readLine()) != null) {
							if (line.trim().equals("///JACOCO:OFF")) {
								directives.add(new Directive(lineNum, false));
							} else if (line.trim().equals("///JACOCO:ON")) {
								directives.add(new Directive(lineNum, true));
							}
							lineNum++;
						}
					} finally {
						bufSourceReader.close();
					}
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}

			return directives;
		}

	}

	public MethodVisitor preVisitMethod(final String name,
			final String signature, final MethodVisitor delegate) {
		return delegate;
	}

	public MethodProbesVisitor visitMethod(final String name,
			final String signature, final MethodProbesVisitor delegate) {
		if ((directives.size() == 0) && (nextDirective == null)) {
			return delegate;
		} else {
			return new LineNumberMethodVisitor(delegate);
		}
	}

	private class LineNumberMethodVisitor extends MethodProbesVisitor {

		private final MethodProbesVisitor delegate;

		private LineNumberMethodVisitor(final MethodProbesVisitor delegate) {
			super(delegate);
			this.delegate = delegate;
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
