/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This strategy for Java 11+ class files uses {@link ConstantDynamic} to hold
 * the probe array and adds bootstrap method requesting the probe array from the
 * runtime.
 */
public class CondyProbeArrayStrategy implements IProbeArrayStrategy {

	/**
	 * Descriptor of the bootstrap method.
	 */
	public static final String B_DESC = "(Ljava/lang/invoke/MethodHandle$Lookup;Ljava/lang/String;Ljava/lang/Class;)[Z";

	private final String className;

	private final long classId;

	private final IExecutionDataAccessorGenerator accessorGenerator;

	CondyProbeArrayStrategy(final String className, final long classId,
			final IExecutionDataAccessorGenerator accessorGenerator) {
		this.className = className;
		this.classId = classId;
		this.accessorGenerator = accessorGenerator;
	}

	public int storeInstance(final MethodVisitor mv, final boolean clinit,
			final int variable) {
		return 0;
	}

	public void addMembers(final ClassVisitor cv, final int probeCount) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC,
				InstrSupport.INITMETHOD_NAME, B_DESC, null, null);
		final int maxStack = accessorGenerator.generateDataAccessor(classId,
				className, probeCount, mv);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(maxStack, 3);
		mv.visitEnd();
	}

}
