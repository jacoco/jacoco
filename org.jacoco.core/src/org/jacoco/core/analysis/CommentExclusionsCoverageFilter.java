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

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.Label;

/**
 * Turn coverage on/off using ///JACOCO:OFF and ///JACOCO:ON directives.
 */
public class CommentExclusionsCoverageFilter implements ICoverageFilter {

	private boolean enabled = true;

	public boolean enabled() {
		return enabled;
	}

	private static class Directive {
		public Directive(final int lineNum, final boolean coverageOn) {
			this.lineNum = lineNum;
			this.coverageOn = coverageOn;
		}

		public final int lineNum;
		public final boolean coverageOn;
	}

	private final List<Directive> directives = new LinkedList<Directive>();

	public boolean includeClass(final String className) {
		enabled = true;
		directives.clear();

		final File sourceFile = new File("src", className + ".java");
		if (sourceFile.exists() && sourceFile.canRead()) {
			try {
				final List<String> lines = FileUtils.readLines(sourceFile);
				int lineNum = 1;
				for (final String line : lines) {
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

		return true;
	}

	public MethodProbesVisitor getVisitor(final MethodProbesVisitor delegate) {
		if (directives.size() == 0) {
			return delegate;
		} else {
			return new LineNumberVisitor(delegate);
		}
	}

	private class LineNumberVisitor extends MethodProbesVisitor {

		private final MethodProbesVisitor delegate;
		private Directive nextDirective;

		private LineNumberVisitor(final MethodProbesVisitor delegate) {
			super(delegate);
			this.delegate = delegate;
			this.nextDirective = directives.remove(0);
		}

		@Override
		public void visitLineNumber(final int line, final Label start) {

			if ((nextDirective != null) && (nextDirective.lineNum <= line)) {
				enabled = nextDirective.coverageOn;

				if (directives.size() > 0) {
					nextDirective = directives.remove(0);
				} else {
					nextDirective = null;
				}
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
