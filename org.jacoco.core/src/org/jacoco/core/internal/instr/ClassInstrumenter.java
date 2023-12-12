/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.jacoco.core.internal.flow.ClassProbesVisitor;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Adapter that instruments a class for coverage tracing.
 */
public class ClassInstrumenter extends ClassProbesVisitor {

	private final IProbeArrayStrategy probeArrayStrategy;

	private String className;

	/**
	 * Emits a instrumented version of this class to the given class visitor.
	 *
	 * @param probeArrayStrategy
	 *            this strategy will be used to access the probe array
	 * @param cv
	 *            next delegate in the visitor chain will receive the
	 *            instrumented class
	 */
	public ClassInstrumenter(final IProbeArrayStrategy probeArrayStrategy,
			final ClassVisitor cv) {
		super(cv);
		this.probeArrayStrategy = probeArrayStrategy;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		this.className = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public FieldVisitor visitField(final int access, final String name,
			final String desc, final String signature, final Object value) {
		InstrSupport.assertNotInstrumented(name, className);
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodProbesVisitor visitMethod(final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions) {

		InstrSupport.assertNotInstrumented(name, className);

		final MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
				exceptions);

		if (mv == null) {
			return null;
		}
		final MethodVisitor frameEliminator = new DuplicateFrameEliminator(mv);
		final ProbeInserter probeVariableInserter = new ProbeInserter(access,
				name, desc, frameEliminator, probeArrayStrategy);
		return new MethodInstrumenter(probeVariableInserter,
				probeVariableInserter);
	}

	@Override
	public void visitTotalProbeCount(final int count) {
		probeArrayStrategy.addMembers(cv, count);
	}

}
