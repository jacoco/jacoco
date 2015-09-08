/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * The strategy for regular classes and Java 8 interfaces which adds a static
 * field to hold the probe array and a static initialization method requesting
 * the probe array from the runtime.
 */
class FieldProbeArrayStrategy implements IProbeArrayStrategy {

	private final String className;
	private final long classId;
	private final boolean withFrames;
	private final int fieldAccess;
	private final IExecutionDataAccessorGenerator accessorGenerator;

	FieldProbeArrayStrategy(final String className, final long classId,
			final boolean withFrames, final int fieldAccess,
			final IExecutionDataAccessorGenerator accessorGenerator) {
		this.className = className;
		this.classId = classId;
		this.withFrames = withFrames;
		this.fieldAccess = fieldAccess;
		this.accessorGenerator = accessorGenerator;
	}

	public int storeInstance(final MethodVisitor mv, final int variable) {
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
				InstrSupport.INITMETHOD_NAME,
				ProbeArrayService.getInitMethodDesc(), false);
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return 1;
	}

	public void addMembers(final ClassVisitor cv, final int probeCount) {
		createDataField(cv);
		ProbeArrayService.createInitMethod(cv, classId, className, withFrames,
				accessorGenerator, probeCount);
	}

	private void createDataField(final ClassVisitor cv) {
		cv.visitField(fieldAccess, InstrSupport.DATAFIELD_NAME,
				ProbeArrayService.getDatafieldDesc(), null, null);
	}

}