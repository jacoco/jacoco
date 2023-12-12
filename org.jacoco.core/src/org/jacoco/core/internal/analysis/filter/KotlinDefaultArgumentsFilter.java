/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.objectweb.asm.tree.LdcInsnNode;
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
 * suffix "$default" or of synthetic constructor with last argument
 * "kotlin.jvm.internal.DefaultConstructorMarker". And its value can't be zero -
 * invocation with all arguments uses original non synthetic method, thus
 * <code>IFEQ</code> instructions should be ignored.
 */
public final class KotlinDefaultArgumentsFilter implements IFilter {

	static boolean isDefaultArgumentsMethod(final MethodNode methodNode) {
		return methodNode.name.endsWith("$default");
	}

	static boolean isDefaultArgumentsConstructor(final MethodNode methodNode) {
		if (!"<init>".equals(methodNode.name)) {
			return false;
		}
		final Type[] argumentTypes = Type.getMethodType(methodNode.desc)
				.getArgumentTypes();
		if (argumentTypes.length < 2) {
			return false;
		}
		return "kotlin.jvm.internal.DefaultConstructorMarker"
				.equals(argumentTypes[argumentTypes.length - 1].getClassName());
	}

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if ((methodNode.access & Opcodes.ACC_SYNTHETIC) == 0) {
			return;
		}
		if (!KotlinGeneratedFilter.isKotlinClass(context)) {
			return;
		}

		if (isDefaultArgumentsMethod(methodNode)) {
			new Matcher().match(methodNode, output, false);
		} else if (isDefaultArgumentsConstructor(methodNode)) {
			new Matcher().match(methodNode, output, true);
		}
	}

	private static class Matcher extends AbstractMatcher {
		public void match(final MethodNode methodNode,
				final IFilterOutput output, final boolean constructor) {
			cursor = skipNonOpcodes(methodNode.instructions.getFirst());

			nextIs(Opcodes.IFNULL);
			nextIsType(Opcodes.NEW, "java/lang/UnsupportedOperationException");
			nextIs(Opcodes.DUP);
			nextIs(Opcodes.LDC);
			if (cursor == null
					|| !(((LdcInsnNode) cursor).cst instanceof String)
					|| !(((String) ((LdcInsnNode) cursor).cst).startsWith(
							"Super calls with default arguments not supported in this target"))) {
				cursor = null;
			}
			nextIsInvoke(Opcodes.INVOKESPECIAL,
					"java/lang/UnsupportedOperationException", "<init>",
					"(Ljava/lang/String;)V");
			nextIs(Opcodes.ATHROW);
			if (cursor != null) {
				output.ignore(methodNode.instructions.getFirst(), cursor);
				next();
			} else {
				cursor = skipNonOpcodes(methodNode.instructions.getFirst());
			}

			final Set<AbstractInsnNode> ignore = new HashSet<AbstractInsnNode>();
			final int maskVar = maskVar(methodNode.desc, constructor);
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

		private static int maskVar(final String desc,
				final boolean constructor) {
			int slot = 0;
			if (constructor) {
				// one slot for reference to current object
				slot++;
			}
			final Type[] argumentTypes = Type.getMethodType(desc)
					.getArgumentTypes();
			final int penultimateArgument = argumentTypes.length - 2;
			for (int i = 0; i < penultimateArgument; i++) {
				slot += argumentTypes[i].getSize();
			}
			return slot;
		}
	}

}
