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
import org.objectweb.asm.MethodVisitor;

/**
 * This strategy for Java 11+ class files uses {@link ConstantDynamic} to hold
 * the probe array and adds bootstrap method requesting the probe array from the
 * runtime.
 */
class CondyProbeArrayStrategy implements IProbeArrayStrategy {

	public int storeInstance(final MethodVisitor mv, final boolean clinit,
			final int variable) {
		return 0;
	}

	public void addMembers(final ClassVisitor cv, final int probeCount) {
	}

}
