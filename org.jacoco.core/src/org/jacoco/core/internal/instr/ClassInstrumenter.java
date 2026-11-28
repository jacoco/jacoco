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
package org.jacoco.core.internal.instr;

import org.jacoco.core.internal.flow.ClassProbesVisitor;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Adapter that instruments a class for coverage tracing.
 */
public class ClassInstrumenter extends ClassProbesVisitor {

	private final IProbeArrayStrategy probeArrayStrategy;

	private String className;

	private boolean conditionalProbes;

	private boolean interfaceType;

	private boolean withFrames;

	private boolean hasMethods;

	/**
	 * Emits an instrumented version of this class to the given class visitor.
	 *
	 * @param probeArrayStrategy
	 *            this strategy will be used to access the probe array
	 * @param cv
	 *            next delegate in the visitor chain will receive the
	 *            instrumented class
	 */
	public ClassInstrumenter(final IProbeArrayStrategy probeArrayStrategy,
			final ClassVisitor cv) {
		this(probeArrayStrategy, cv, false);
	}

	/**
	 * Creates a class instrumenter with optional conditional probes.
	 *
	 * @param probeArrayStrategy
	 *            strategy used to access the probe array
	 * @param cv
	 *            delegate receiving the instrumented class
	 * @param conditionalProbes
	 *            whether probes should check before writing
	 */
	public ClassInstrumenter(final IProbeArrayStrategy probeArrayStrategy,
			final ClassVisitor cv, final boolean conditionalProbes) {
		super(cv);
		this.probeArrayStrategy = probeArrayStrategy;
		this.conditionalProbes = conditionalProbes;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		this.className = name;
		interfaceType = (access & Opcodes.ACC_INTERFACE) != 0;
		withFrames = InstrSupport.needsFrames(version);
		conditionalProbes &= !interfaceType
				|| (version & 0xFFFF) >= Opcodes.V1_8;
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
		hasMethods |= (access
				& (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0;
		final MethodVisitor frameEliminator = new DuplicateFrameEliminator(mv);
		final ProbeInserter probeVariableInserter = new ProbeInserter(access,
				name, desc, frameEliminator, probeArrayStrategy,
				conditionalProbes ? className : null, interfaceType);
		return new MethodInstrumenter(probeVariableInserter,
				probeVariableInserter);
	}

	@Override
	public void visitTotalProbeCount(final int count) {
		probeArrayStrategy.addMembers(cv, count);
		if (conditionalProbes && hasMethods && count > 0) {
			createHitMethod();
		}
	}

	private void createHitMethod() {
		final int access = Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC
				| (interfaceType ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PRIVATE);
		final MethodVisitor mv = cv.visitMethod(access,
				InstrSupport.HITMETHOD_NAME, InstrSupport.HITMETHOD_DESC, null,
				null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ILOAD, 1);
		mv.visitInsn(Opcodes.BALOAD);
		final Label alreadyHit = new Label();
		mv.visitJumpInsn(Opcodes.IFNE, alreadyHit);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ILOAD, 1);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitInsn(Opcodes.BASTORE);
		mv.visitLabel(alreadyHit);
		if (withFrames) {
			mv.visitFrame(
					Opcodes.F_NEW, 2, new Object[] {
							InstrSupport.DATAFIELD_DESC, Opcodes.INTEGER },
					0, new Object[0]);
		}
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(3, 2);
		mv.visitEnd();
	}

}
