/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Filters private empty constructors that do not have arguments.
 */
public final class PrivateEmptyNoArgConstructorFilter implements IFilter {

	public void filter(final String className, final String superClassName,
			final MethodNode methodNode, final IFilterOutput output) {
		if ((methodNode.access & Opcodes.ACC_PRIVATE) != 0
				&& "<init>".equals(methodNode.name)
				&& "()V".equals(methodNode.desc)
				&& new Matcher().match(methodNode, superClassName)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private static class Matcher extends AbstractMatcher {
		private boolean match(final MethodNode methodNode,
				final String superClassName) {
			cursor = methodNode.instructions.getFirst();
			skipNonOpcodes();
			if (cursor.getOpcode() != Opcodes.ALOAD
					|| ((VarInsnNode) cursor).var != 0) {
				return false;
			}
			nextIs(Opcodes.INVOKESPECIAL);
			MethodInsnNode m = (MethodInsnNode) cursor;
			if (m != null && superClassName.equals(m.owner)
					&& "<init>".equals(m.name) && ("()V").equals(m.desc)) {
				nextIs(Opcodes.RETURN);
				return cursor != null;
			}
			return false;
		}
	}

}
