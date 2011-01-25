/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/**
 * A {@link ClassVisitor} that calculates probes for every method.
 */
public class ClassProbesAdapter extends ClassAdapter implements
		IProbeIdGenerator {

	private static final IMethodProbesVisitor EMPTY_METHOD_PROBES_VISITOR;

	static {
		class Impl extends EmptyVisitor implements IMethodProbesVisitor {

			public void visitProbe(final int probeId) {
			}

			public void visitJumpInsnWithProbe(final int opcode,
					final Label label, final int probeId) {
			}

			public void visitInsnWithProbe(final int opcode, final int probeId) {
			}

			public void visitTableSwitchInsnWithProbes(final int min,
					final int max, final Label dflt, final Label[] labels) {
			}

			public void visitLookupSwitchInsnWithProbes(final Label dflt,
					final int[] keys, final Label[] labels) {
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

	private final IClassProbesVisitor cv;

	private int counter = 0;

	private boolean interfaceType;

	/**
	 * Creates a new adapter that delegates to the given visitor.
	 * 
	 * @param cv
	 *            instance to delegate to
	 */
	public ClassProbesAdapter(final IClassProbesVisitor cv) {
		super(cv);
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
		final IMethodProbesVisitor methodProbes;
		final IMethodProbesVisitor mv = cv.visitMethod(access, name, desc,
				signature, exceptions);
		if (mv == null) {
			// We need to visit the method in any case, otherwise probe ids
			// are not reproducible
			methodProbes = EMPTY_METHOD_PROBES_VISITOR;
		} else {
			methodProbes = mv;
		}
		return new JSRInlinerAdapter(null, access, name, desc, signature,
				exceptions) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				this.accept(new LabelFlowAnalyzer());
				if (interfaceType) {
					final ProbeCounter counter = new ProbeCounter();
					this.accept(new MethodProbesAdapter(
							EMPTY_METHOD_PROBES_VISITOR, counter));
					cv.visitTotalProbeCount(counter.count);
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
