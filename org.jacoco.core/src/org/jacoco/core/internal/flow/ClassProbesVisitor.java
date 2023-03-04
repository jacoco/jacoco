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
package org.jacoco.core.internal.flow;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassVisitor;

/**
 * A {@link ClassVisitor} with additional methods to get probe insertion
 * information for each method
 */
public abstract class ClassProbesVisitor extends ClassVisitor {

	/**
	 * New visitor instance without delegate visitor.
	 */
	public ClassProbesVisitor() {
		this(null);
	}

	/**
	 * New visitor instance that delegates to the given visitor.
	 *
	 * @param cv
	 *            optional next visitor in chain
	 */
	public ClassProbesVisitor(final ClassVisitor cv) {
		super(InstrSupport.ASM_API_VERSION, cv);
	}

	/**
	 * When visiting a method we need a {@link MethodProbesVisitor} to handle
	 * the probes of that method.
	 */
	@Override
	public abstract MethodProbesVisitor visitMethod(int access, String name,
			String desc, String signature, String[] exceptions);

	/**
	 * Reports the total number of encountered probes. For classes this method
	 * is called just before {@link ClassVisitor#visitEnd()}. For interfaces
	 * this method is called before the first method (the static initializer) is
	 * emitted.
	 *
	 * @param count
	 *            total number of probes
	 */
	public abstract void visitTotalProbeCount(int count);

}
