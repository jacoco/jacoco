/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.instr;

import org.jacoco.core.internal.flow.IProbeIdGenerator;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * A {@link ClassVisitor} that calculates block boundaries for every
 * method.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */

class BlockClassAdapter extends ClassAdapter implements IProbeIdGenerator {

	private static final IBlockMethodVisitor EMPTY_BLOCK_METHOD_VISITOR;

	static {
		class Impl extends EmptyVisitor implements IBlockMethodVisitor {
			public void visitBlockEndBeforeJump(final int id) {
			}

			public void visitBlockEnd(final int id) {
			}
		}
		EMPTY_BLOCK_METHOD_VISITOR = new Impl();
	}

	private final IBlockClassVisitor cv;

	private int counter = 0;

	/**
	 * Creates a new adapter that delegates to the given visitor.
	 * 
	 * @param cv
	 *            instance to delegate to
	 */
	public BlockClassAdapter(final IBlockClassVisitor cv) {
		super(cv);
		this.cv = cv;
	}

	@Override
	public final MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		IBlockMethodVisitor mv = cv.visitMethod(access, name, desc, signature,
				exceptions);
		if (mv == null) {
			// We need to visit the method in any case, otherwise probe ids
			// are not reproducible
			mv = EMPTY_BLOCK_METHOD_VISITOR;
		}
		return new BlockMethodAdapter(mv, this, access, name, desc, signature,
				exceptions);
	}

	@Override
	public void visitEnd() {
		cv.visitTotalProbeCount(counter);
		super.visitEnd();
	}

	// === IProbeIdGenerator ===

	public int nextId() {
		return counter++;
	}

}
