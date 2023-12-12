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
import org.objectweb.asm.Opcodes;

/**
 * Internal class to remember the total number of probes required for a class.
 */
class ProbeCounter extends ClassProbesVisitor {

	private int count;
	private boolean methods;

	ProbeCounter() {
		count = 0;
		methods = false;
	}

	@Override
	public MethodProbesVisitor visitMethod(final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions) {
		if (!InstrSupport.CLINIT_NAME.equals(name)
				&& (access & Opcodes.ACC_ABSTRACT) == 0) {
			methods = true;
		}
		return null;
	}

	@Override
	public void visitTotalProbeCount(final int count) {
		this.count = count;
	}

	int getCount() {
		return count;
	}

	/**
	 * @return <code>true</code> if the class has non-abstract methods other
	 *         than a static initializer
	 */
	boolean hasMethods() {
		return methods;
	}

}
