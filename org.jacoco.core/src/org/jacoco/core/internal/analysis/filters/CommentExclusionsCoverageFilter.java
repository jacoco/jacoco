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
package org.jacoco.core.internal.analysis.filters;

import java.util.LinkedList;
import java.util.Queue;

import org.jacoco.core.analysis.IDirectivesParser;
import org.jacoco.core.analysis.IDirectivesParser.Directive;
import org.jacoco.core.internal.analysis.filters.ICoverageFilterStatus.ICoverageFilter;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Turn coverage on/off using ///JACOCO:OFF and ///JACOCO:ON directives.
 */
public class CommentExclusionsCoverageFilter implements ICoverageFilter {

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
		return new SourceFileClassVisitor(delegate);
	}

	/**
	 * Simple Visitor for storing the source filename
	 */
	public class SourceFileClassVisitor extends ClassVisitor {
		/**
		 * @param cv
		 */
		public SourceFileClassVisitor(final ClassVisitor cv) {
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

	public MethodVisitor preVisitMethod(final String name, final String desc,
			final MethodVisitor delegate) {
		return delegate;
	}

	public MethodProbesVisitor visitMethod(final String name,
			final String desc, final MethodProbesVisitor delegate) {
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
