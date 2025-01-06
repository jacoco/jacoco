/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
 * For methods and constructors with default arguments Kotlin compiler generates
 * synthetic method with suffix "$default" or a synthetic constructor with last
 * argument "kotlin.jvm.internal.DefaultConstructorMarker" respectively. And in
 * this synthetic method for each default argument Kotlin compiler generates
 * following bytecode to determine if it should be used or not:
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
 * If original method has <code>X</code> arguments, then in synthetic method
 * <code>maskVar</code> is one of arguments from <code>X+1</code> to
 * <code>X+1+(X/32)</code>.
 *
 * At least one of such arguments is not zero - invocation without default
 * arguments uses original non synthetic method.
 *
 * This filter marks <code>IFEQ</code> instructions as ignored.
 */
final class KotlinDefaultArgumentsFilter implements IFilter {

	private static boolean isDefaultArgumentsMethod(
			final MethodNode methodNode) {
		return methodNode.name.endsWith("$default");
	}

	private static boolean isDefaultArgumentsConstructor(
			final MethodNode methodNode) {
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

			for (final AbstractInsnNode i : ignore) {
				output.ignore(i, i);
			}
		}

		private static int maskVar(final String desc,
				final boolean constructor) {
			final Type[] argumentTypes = Type.getMethodType(desc)
					.getArgumentTypes();
			int slot = 0;
			if (constructor) {
				// one slot for reference to current object
				slot++;
			}
			final int firstMaskArgument = argumentTypes.length - 1
					- computeNumberOfMaskArguments(argumentTypes.length);
			for (int i = 0; i < firstMaskArgument; i++) {
				slot += argumentTypes[i].getSize();
			}
			return slot;
		}
	}

	/**
	 * @param arguments
	 *            number of arguments of synthetic method
	 * @return number of arguments holding mask
	 */
	static int computeNumberOfMaskArguments(final int arguments) {
		return (arguments - 2) / 33 + 1;
	}

}
