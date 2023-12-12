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
package org.jacoco.core.runtime;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This implementation of {@link IExecutionDataAccessorGenerator} generate a
 * direct dependency to the JaCoCo runtime agent to initialize the runtime and
 * obtain probe arrays. This generator is designed for offline instrumentation
 * only.
 */
public class OfflineInstrumentationAccessGenerator
		implements IExecutionDataAccessorGenerator {

	private final String runtimeClassName;

	/**
	 * Creates a new instance for offline instrumentation.
	 */
	public OfflineInstrumentationAccessGenerator() {
		this(JaCoCo.RUNTIMEPACKAGE.replace('.', '/') + "/Offline");
	}

	/**
	 * Creates a new instance with the given runtime class name for testing
	 * purposes
	 *
	 * @param runtimeClassName
	 *            VM name of the runtime class
	 */
	OfflineInstrumentationAccessGenerator(final String runtimeClassName) {
		this.runtimeClassName = runtimeClassName;
	}

	public int generateDataAccessor(final long classid, final String classname,
			final int probecount, final MethodVisitor mv) {
		mv.visitLdcInsn(Long.valueOf(classid));
		mv.visitLdcInsn(classname);
		InstrSupport.push(mv, probecount);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeClassName, "getProbes",
				"(JLjava/lang/String;I)[Z", false);
		return 4;
	}

}
