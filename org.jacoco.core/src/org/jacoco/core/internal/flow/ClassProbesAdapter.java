/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AnalyzerAdapter;

/**
 * A {@link org.objectweb.asm.ClassVisitor} that calculates probes for every
 * method.
 */
public class ClassProbesAdapter extends ClassVisitor
		implements IProbeIdGenerator {

	private static final MethodProbesVisitor EMPTY_METHOD_PROBES_VISITOR = new MethodProbesVisitor() {
	};

	/**
	 * Value for the boundary probe base that disables boundary probes.
	 */
	public static final int NO_BOUNDARY_PROBES = -1;

	private final ClassProbesVisitor cv;

	private final boolean trackFrames;

	private final int boundaryProbeBase;

	private int counter = 0;

	private int boundaryCounter;

	private String name;

	/**
	 * Creates a new adapter that delegates to the given visitor and does not
	 * generate boundary probes.
	 *
	 * @param cv
	 *            instance to delegate to
	 * @param trackFrames
	 *            if <code>true</code> stackmap frames are tracked and provided
	 */
	public ClassProbesAdapter(final ClassProbesVisitor cv,
			final boolean trackFrames) {
		this(cv, trackFrames, NO_BOUNDARY_PROBES);
	}

	/**
	 * Creates a new adapter that delegates to the given visitor.
	 *
	 * @param cv
	 *            instance to delegate to
	 * @param trackFrames
	 *            if <code>true</code> stackmap frames are tracked and provided
	 * @param boundaryProbeBase
	 *            first id to use for boundary probes, which must be the total
	 *            number of regular probes of the class as determined by
	 *            {@link #countRegularProbes(ClassReader)}, or
	 *            {@link #NO_BOUNDARY_PROBES} to not generate boundary probes
	 */
	public ClassProbesAdapter(final ClassProbesVisitor cv,
			final boolean trackFrames, final int boundaryProbeBase) {
		super(InstrSupport.ASM_API_VERSION, cv);
		this.cv = cv;
		this.trackFrames = trackFrames;
		this.boundaryProbeBase = boundaryProbeBase;
		this.boundaryCounter = boundaryProbeBase;
	}

	/**
	 * Determines how many regular probes the given class requires. Boundary
	 * probes are allocated after these, which keeps the ids of regular probes
	 * independent of the boundary metric.
	 *
	 * @param reader
	 *            reader for the class definition
	 * @return number of regular probes
	 */
	public static int countRegularProbes(final ClassReader reader) {
		final ProbeCountVisitor counter = new ProbeCountVisitor();
		reader.accept(new ClassProbesAdapter(counter, false), 0);
		return counter.count;
	}

	private static class ProbeCountVisitor extends ClassProbesVisitor {

		int count;

		@Override
		public MethodProbesVisitor visitMethod(final int access,
				final String name, final String desc, final String signature,
				final String[] exceptions) {
			return null;
		}

		@Override
		public void visitTotalProbeCount(final int count) {
			this.count = count;
		}
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		this.name = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public final MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions) {
		final MethodProbesVisitor methodProbes;
		final MethodProbesVisitor mv = cv.visitMethod(access, name, desc,
				signature, exceptions);
		if (mv == null) {
			// We need to visit the method in any case, otherwise probe ids
			// are not reproducible
			methodProbes = EMPTY_METHOD_PROBES_VISITOR;
		} else {
			methodProbes = mv;
		}
		return new MethodSanitizer(null, access, name, desc, signature,
				exceptions) {

			@Override
			public void visitEnd() {
				super.visitEnd();
				LabelFlowAnalyzer.markLabels(this);
				final MethodProbesAdapter probesAdapter = new MethodProbesAdapter(
						methodProbes, ClassProbesAdapter.this);
				if (trackFrames) {
					final AnalyzerAdapter analyzer = new AnalyzerAdapter(
							ClassProbesAdapter.this.name, access, name, desc,
							probesAdapter);
					probesAdapter.setAnalyzer(analyzer);
					methodProbes.accept(this, analyzer);
				} else {
					methodProbes.accept(this, probesAdapter);
				}
			}
		};
	}

	@Override
	public void visitEnd() {
		cv.visitTotalProbeCount(Math.max(counter, boundaryCounter));
		super.visitEnd();
	}

	// === IProbeIdGenerator ===

	public int nextId() {
		return counter++;
	}

	public int nextBoundaryId() {
		if (boundaryProbeBase == NO_BOUNDARY_PROBES) {
			return LabelInfo.NO_PROBE;
		}
		return boundaryCounter++;
	}

}
