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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/**
 * This method visitor fixes two potential issues with Java byte code:
 *
 * <ul>
 * <li>Remove JSR/RET instructions by inlining subroutines which are deprecated
 * since Java 6. The RET statement complicates control flow analysis as the jump
 * target is not explicitly given.</li>
 * <li>Remove code attributes line number and local variable name if they point
 * to invalid offsets which some tools create. When writing out such invalid
 * labels with ASM class files do not verify any more.</li>
 * </ul>
 */
class MethodSanitizer extends JSRInlinerAdapter {

	MethodSanitizer(final MethodVisitor mv, final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions) {
		super(InstrSupport.ASM_API_VERSION, mv, access, name, desc, signature,
				exceptions);
	}

	@Override
	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
		// Here we rely on the usage of the info fields by the tree API. If the
		// labels have been properly used before the info field contains a
		// reference to the LabelNode, otherwise null.
		if (start.info != null && end.info != null) {
			super.visitLocalVariable(name, desc, signature, start, end, index);
		}
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
		// Here we rely on the usage of the info fields by the tree API. If the
		// labels have been properly used before the info field contains a
		// reference to the LabelNode, otherwise null.
		if (start.info != null) {
			super.visitLineNumber(line, start);
		}
	}

}
