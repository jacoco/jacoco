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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

/**
 * Filters branches that Kotlin compiler generates for coroutines.
 */
public final class KotlinCoroutineFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {

		if (!KotlinGeneratedFilter.isKotlinClass(context)) {
			return;
		}

		if (!"invokeSuspend".equals(methodNode.name)) {
			return;
		}

		new Matcher().match(methodNode, output);

	}

	private static class Matcher extends AbstractMatcher {
		private void match(final MethodNode methodNode,
				final IFilterOutput output) {
			cursor = methodNode.instructions.getFirst();
			nextIsInvokeStatic("kotlin/coroutines/intrinsics/IntrinsicsKt",
					"getCOROUTINE_SUSPENDED");
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
			nextIs(Opcodes.DUP);
			nextIsType(Opcodes.INSTANCEOF, "kotlin/Result$Failure");
			nextIs(Opcodes.IFEQ);
			nextIsType(Opcodes.CHECKCAST, "kotlin/Result$Failure");
			nextIs(Opcodes.GETFIELD);
			nextIs(Opcodes.ATHROW);
			nextIs(Opcodes.POP);

			if (cursor == null) {
				return;
			}
			ignore.add(s);
			ignore.add(cursor);

			for (AbstractInsnNode i = methodNode.instructions
					.getFirst(); i != null; i = i.getNext()) {
				cursor = i;
				nextIsVar(Opcodes.ALOAD, "COROUTINE_SUSPENDED");
				nextIs(Opcodes.IF_ACMPNE);
				nextIsVar(Opcodes.ALOAD, "COROUTINE_SUSPENDED");
				nextIs(Opcodes.ARETURN);

				nextIs(Opcodes.ALOAD);
				nextIs(Opcodes.DUP);
				nextIsType(Opcodes.INSTANCEOF, "kotlin/Result$Failure");
				nextIs(Opcodes.IFEQ);
				nextIsType(Opcodes.CHECKCAST, "kotlin/Result$Failure");
				nextIs(Opcodes.GETFIELD);
				nextIs(Opcodes.ATHROW);
				nextIs(Opcodes.POP);

				nextIs(Opcodes.ALOAD);
				if (cursor != null) {
					ignore.add(i);
					ignore.add(cursor);
				}
			}

			if (ignore.size() != s.labels.size() * 2) {
				return;
			}

			cursor = s.dflt;
			nextIsType(Opcodes.NEW, "java/lang/IllegalStateException");
			nextIs(Opcodes.DUP);
			nextIs(Opcodes.LDC);
			if (!((LdcInsnNode) cursor).cst.equals(
					"call to 'resume' before 'invoke' with coroutine")) {
				return;
			}
			nextIsInvokeSuper("java/lang/IllegalStateException",
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
	}

}
