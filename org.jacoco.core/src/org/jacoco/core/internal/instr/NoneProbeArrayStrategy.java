/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * The strategy does not emit any code at all. This is used for interface types
 * without any code.
 */
class NoneProbeArrayStrategy implements IProbeArrayStrategy {

	public int storeInstance(final MethodVisitor mv, final boolean clinit,
			final int variable) {
		throw new UnsupportedOperationException();
	}

	public void addMembers(final ClassVisitor delegate, final int probeCount) {
		// nothing to do
	}

}
