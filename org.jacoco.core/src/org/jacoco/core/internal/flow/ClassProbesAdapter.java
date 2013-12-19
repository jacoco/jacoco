/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import org.jacoco.core.JaCoCo;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A {@link org.objectweb.asm.ClassVisitor} that calculates probes for every
 * method.
 */
public class ClassProbesAdapter extends ClassVisitor implements
		IProbeIdGenerator {

	private static final MethodProbesVisitor EMPTY_METHOD_PROBES_VISITOR;

	static {
		class Impl extends MethodProbesVisitor {

			@Override
			public void visitProbe(final int probeId) {
				// nothing to do
			}

			@Override
			public void visitJumpInsnWithProbe(final int opcode,
					final Label label, final int probeId) {
				// nothing to do
			}

			@Override
			public void visitInsnWithProbe(final int opcode, final int probeId) {
				// nothing to do
			}

			@Override
			public void visitTableSwitchInsnWithProbes(final int min,
					final int max, final Label dflt, final Label[] labels) {
				// nothing to do
			}

			@Override
			public void visitLookupSwitchInsnWithProbes(final Label dflt,
					final int[] keys, final Label[] labels) {
				// nothing to do
			}
		}
		EMPTY_METHOD_PROBES_VISITOR = new Impl();
	}

	private static class ProbeCounter implements IProbeIdGenerator {
		int count = 0;

		public int nextId() {
			return count++;
		}
	}

	private final ClassProbesVisitor cv;

	private int counter = 0;

	private boolean interfaceType;

	/**
	 * Creates a new adapter that delegates to the given visitor.
	 * 
	 * @param cv
	 *            instance to delegate to
	 */
	public ClassProbesAdapter(final ClassProbesVisitor cv) {
		super(JaCoCo.ASM_API_VERSION, cv);
		this.cv = cv;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		interfaceType = (access & Opcodes.ACC_INTERFACE) != 0;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public final MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
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
				if (interfaceType) {
					final ProbeCounter probeCounter = new ProbeCounter();
					final MethodProbesAdapter adapter = new MethodProbesAdapter(
							EMPTY_METHOD_PROBES_VISITOR, probeCounter);
					// We do not use the accept() method as ASM resets labels
					// after every call to accept()
					instructions.accept(adapter);
					cv.visitTotalProbeCount(probeCounter.count);
				}
				this.accept(new MethodProbesAdapter(methodProbes,
						ClassProbesAdapter.this));
			}
		};
	}

	@Override
	public void visitEnd() {
		if (!interfaceType) {
			cv.visitTotalProbeCount(counter);
		}
		super.visitEnd();
	}

	// === IProbeIdGenerator ===

	public int nextId() {
		return counter++;
	}

}
