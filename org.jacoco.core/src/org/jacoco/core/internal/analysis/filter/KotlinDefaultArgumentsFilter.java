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
package org.jacoco.core.internal.analysis.filter;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Filters branches that Kotlin compiler generates for default arguments.
 * 
 * For each default argument Kotlin compiler generates following bytecode to
 * determine if it should be used or not:
 * 
 * <pre>
 * ILOAD maskVar
 * ICONST_x, BIPUSH, SIPUSH, LDC or LDC_W
 * IAND
 * IFEQ label
 * default argument
 * label:
 * </pre>
 * 
 * Where <code>maskVar</code> is penultimate argument of synthetic method with
 * suffix "$default". And its value can't be zero - invocation with all
 * arguments uses original non synthetic method, thus <code>IFEQ</code>
 * instructions should be ignored.
 */
public final class KotlinDefaultArgumentsFilter implements IFilter {

	static boolean isDefaultArgumentsMethodName(final String methodName) {
		return methodName.endsWith("$default");
	}

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if ((methodNode.access & Opcodes.ACC_SYNTHETIC) == 0) {
			return;
		}
		if (!isDefaultArgumentsMethodName(methodNode.name)) {
			return;
		}
		if (!KotlinGeneratedFilter.isKotlinClass(context)) {
			return;
		}

		new Matcher().match(methodNode, output);
	}

	private static class Matcher extends AbstractMatcher {
		public void match(final MethodNode methodNode,
				final IFilterOutput output) {
			cursor = methodNode.instructions.getFirst();

			final Set<AbstractInsnNode> ignore = new HashSet<AbstractInsnNode>();
			final int maskVar = Type.getMethodType(methodNode.desc)
					.getArgumentTypes().length - 2;
			while (true) {
				if (cursor.getOpcode() != Opcodes.ILOAD) {
					break;
				}
				if (((VarInsnNode) cursor).var != maskVar) {
					break;
				}
				next();
				nextIs(Opcodes.IAND);
				nextIs(Opcodes.IFEQ);
				if (cursor == null) {
					return;
				}
				ignore.add(cursor);
				cursor = ((JumpInsnNode) cursor).label;
				skipNonOpcodes();
			}

			for (AbstractInsnNode i : ignore) {
				output.ignore(i, i);
			}
		}
	}

}
