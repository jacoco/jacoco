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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

/**
 * Filters branches that Kotlin compiler generates for coroutines.
 */
public final class KotlinCoroutineFilter implements IFilter {

	static boolean isImplementationOfSuspendFunction(
			final MethodNode methodNode) {
		if (methodNode.name.startsWith("access$")) {
			return false;
		}
		final Type methodType = Type.getMethodType(methodNode.desc);
		final int lastArgument = methodType.getArgumentTypes().length - 1;
		return lastArgument >= 0 && "kotlin.coroutines.Continuation".equals(
				methodType.getArgumentTypes()[lastArgument].getClassName());
	}

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {

		if (!KotlinGeneratedFilter.isKotlinClass(context)) {
			return;
		}

		new Matcher().match(methodNode, output);
		new Matcher().matchOptimizedTailCall(methodNode, output);
	}

	private static class Matcher extends AbstractMatcher {

		private void matchOptimizedTailCall(final MethodNode methodNode,
				final IFilterOutput output) {
			for (final AbstractInsnNode i : methodNode.instructions) {
				cursor = i;
				nextIs(Opcodes.DUP);
				nextIsInvoke(Opcodes.INVOKESTATIC,
						"kotlin/coroutines/intrinsics/IntrinsicsKt",
						"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;");
				nextIs(Opcodes.IF_ACMPNE);
				nextIs(Opcodes.ARETURN);
				nextIs(Opcodes.POP);
				if (cursor != null) {
					output.ignore(i.getNext(), cursor);
				}
			}
		}

		private void match(final MethodNode methodNode,
				final IFilterOutput output) {
			cursor = skipNonOpcodes(methodNode.instructions.getFirst());
			if (cursor == null || cursor.getOpcode() != Opcodes.INVOKESTATIC) {
				cursor = null;
			} else {
				final MethodInsnNode m = (MethodInsnNode) cursor;
				if (!"kotlin/coroutines/intrinsics/IntrinsicsKt".equals(m.owner)
						|| !"getCOROUTINE_SUSPENDED".equals(m.name)
						|| !"()Ljava/lang/Object;".equals(m.desc)) {
					cursor = null;
				}
			}

			if (cursor == null) {
				cursor = skipNonOpcodes(methodNode.instructions.getFirst());

				nextIsCreateStateInstance();

				nextIsInvoke(Opcodes.INVOKESTATIC,
						"kotlin/coroutines/intrinsics/IntrinsicsKt",
						"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;");
			}

			nextIsVar(Opcodes.ASTORE, "COROUTINE_SUSPENDED");
			nextIsVar(Opcodes.ALOAD, "this");
			nextIs(Opcodes.GETFIELD);
			nextIs(Opcodes.TABLESWITCH);
			if (cursor == null) {
				return;
			}
			final TableSwitchInsnNode s = (TableSwitchInsnNode) cursor;
			final List<AbstractInsnNode> ignore = new ArrayList<AbstractInsnNode>(
					s.labels.size() * 2);

			nextIs(Opcodes.ALOAD);
			nextIsThrowOnFailure();

			if (cursor == null) {
				return;
			}
			ignore.add(methodNode.instructions.getFirst());
			ignore.add(cursor);

			int suspensionPoint = 1;
			for (AbstractInsnNode i = cursor; i != null
					&& suspensionPoint < s.labels.size(); i = i.getNext()) {
				cursor = i;
				nextIsVar(Opcodes.ALOAD, "COROUTINE_SUSPENDED");
				nextIs(Opcodes.IF_ACMPNE);
				if (cursor == null) {
					continue;
				}
				final AbstractInsnNode continuationAfterLoadedResult = skipNonOpcodes(
						((JumpInsnNode) cursor).label);
				nextIsVar(Opcodes.ALOAD, "COROUTINE_SUSPENDED");
				nextIs(Opcodes.ARETURN);
				if (cursor == null
						|| skipNonOpcodes(cursor.getNext()) != skipNonOpcodes(
								s.labels.get(suspensionPoint))) {
					continue;
				}

				for (AbstractInsnNode j = i; j != null; j = j.getNext()) {
					cursor = j;
					nextIs(Opcodes.ALOAD);
					nextIsThrowOnFailure();

					nextIs(Opcodes.ALOAD);
					if (cursor != null && skipNonOpcodes(cursor
							.getNext()) == continuationAfterLoadedResult) {
						ignore.add(i);
						ignore.add(cursor);
						suspensionPoint++;
						break;
					}
				}
			}

			cursor = s.dflt;
			nextIsType(Opcodes.NEW, "java/lang/IllegalStateException");
			nextIs(Opcodes.DUP);
			nextIs(Opcodes.LDC);
			if (cursor == null) {
				return;
			}
			if (!((LdcInsnNode) cursor).cst.equals(
					"call to 'resume' before 'invoke' with coroutine")) {
				return;
			}
			nextIsInvoke(Opcodes.INVOKESPECIAL,
					"java/lang/IllegalStateException", "<init>",
					"(Ljava/lang/String;)V");
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return;
			}

			output.ignore(s.dflt, cursor);
			for (int i = 0; i < ignore.size(); i += 2) {
				output.ignore(ignore.get(i), ignore.get(i + 1));
			}
		}

		private void nextIsThrowOnFailure() {
			final AbstractInsnNode c = cursor;
			nextIsInvoke(Opcodes.INVOKESTATIC, "kotlin/ResultKt",
					"throwOnFailure", "(Ljava/lang/Object;)V");
			if (cursor == null) {
				cursor = c;
				// Before resolution of
				// https://youtrack.jetbrains.com/issue/KT-28015 in
				// Kotlin 1.3.30
				nextIs(Opcodes.DUP);
				nextIsType(Opcodes.INSTANCEOF, "kotlin/Result$Failure");
				nextIs(Opcodes.IFEQ);
				nextIsType(Opcodes.CHECKCAST, "kotlin/Result$Failure");
				nextIs(Opcodes.GETFIELD);
				nextIs(Opcodes.ATHROW);
				nextIs(Opcodes.POP);
			}
		}

		private void nextIsCreateStateInstance() {
			nextIs(Opcodes.INSTANCEOF);

			nextIs(Opcodes.IFEQ);
			if (cursor == null) {
				return;
			}
			final AbstractInsnNode createStateInstance = skipNonOpcodes(
					((JumpInsnNode) cursor).label);

			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.CHECKCAST);
			nextIs(Opcodes.ASTORE);

			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.GETFIELD);

			nextIs(Opcodes.LDC);
			nextIs(Opcodes.IAND);
			nextIs(Opcodes.IFEQ);
			if (cursor == null || skipNonOpcodes(
					((JumpInsnNode) cursor).label) != createStateInstance) {
				return;
			}

			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.DUP);
			nextIs(Opcodes.GETFIELD);

			nextIs(Opcodes.LDC);
			nextIs(Opcodes.ISUB);
			nextIs(Opcodes.PUTFIELD);

			nextIs(Opcodes.GOTO);
			if (cursor == null) {
				return;
			}
			final AbstractInsnNode afterCoroutineStateCreated = skipNonOpcodes(
					((JumpInsnNode) cursor).label);

			if (skipNonOpcodes(cursor.getNext()) != createStateInstance) {
				return;
			}

			cursor = afterCoroutineStateCreated;
			nextIs(Opcodes.GETFIELD);
			nextIs(Opcodes.ASTORE);
		}
	}

}
