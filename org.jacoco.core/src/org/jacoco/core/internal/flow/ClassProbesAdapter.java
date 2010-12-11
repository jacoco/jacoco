/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.MethodNode;

/**
 * A {@link ClassVisitor} that calculates probes for every method.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */

public class ClassProbesAdapter extends ClassAdapter implements
		IProbeIdGenerator {

	private static final IMethodProbesVisitor EMPTY_BLOCK_METHOD_VISITOR;

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
		EMPTY_BLOCK_METHOD_VISITOR = new Impl();
	}

	private final IClassProbesVisitor cv;

	private int counter = 0;

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
	public final MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		final IMethodProbesVisitor methodProbes;
		final IMethodProbesVisitor mv = cv.visitMethod(access, name, desc,
				signature, exceptions);
		if (mv == null) {
			// We need to visit the method in any case, otherwise probe ids
			// are not reproducible
			methodProbes = EMPTY_BLOCK_METHOD_VISITOR;
		} else {
			methodProbes = mv;
		}
		return new MethodNode() {
			@Override
			public void visitEnd() {
				super.visitEnd();
				this.accept(new LabelFlowAnalyzer());
				this.accept(new MethodProbesAdapter(methodProbes,
						ClassProbesAdapter.this));
			}
		};
	}

	@Override
	public void visitEnd() {
		cv.visitTotalProbeCount(counter);
		super.visitEnd();
	}

	// === IProbeIdGenerator ===

	public int nextId() {
		return counter++;
	}

}
