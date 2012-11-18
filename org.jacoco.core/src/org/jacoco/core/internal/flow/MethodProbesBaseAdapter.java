/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Hare Robertson - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Base adapter class which exposes a {@link #visitProbe()} method
 */
public abstract class MethodProbesBaseAdapter extends MethodVisitor {

	/**
	 * New visitor instance that delegates to the given visitor.
	 * 
	 * @param mv
	 *            optional next visitor in chain
	 */
	public MethodProbesBaseAdapter(final MethodVisitor mv) {
		super(Opcodes.ASM4, mv);
	}

	/**
	 * Generate a new probe
	 */
	public abstract void visitProbe();
}
