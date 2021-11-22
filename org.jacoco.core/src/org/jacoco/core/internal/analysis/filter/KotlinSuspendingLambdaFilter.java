/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lukas Rössler - initial implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

public class KotlinSuspendingLambdaFilter implements IFilter {
	@Override
	public void filter(MethodNode methodNode, IFilterContext context,
			IFilterOutput output) {
		final Matcher matcher = new Matcher();
		for (final AbstractInsnNode i : methodNode.instructions) {
			matcher.match(i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		public void match(final AbstractInsnNode start,
				final IFilterOutput output) {
			if (Opcodes.TABLESWITCH != start.getOpcode()) {
				return;
			}
			TableSwitchInsnNode switchInsnNode = (TableSwitchInsnNode) start;

			// follow the default jump to check whether this is our "call to
			// 'resume' before 'invoke' with coroutine" IllegalStateException
			cursor = switchInsnNode.dflt;
			AbstractInsnNode startOfThrowBlock = cursor;

			nextIsType(Opcodes.NEW, "java/lang/IllegalStateException");
			nextIs(Opcodes.DUP);
			nextIsLdc("call to 'resume' before 'invoke' with coroutine");
			nextIsInvoke(Opcodes.INVOKESPECIAL,
					"java/lang/IllegalStateException", "<init>",
					"(Ljava/lang/String;)V");
			nextIs(Opcodes.ATHROW);

			if (cursor == null) {
				return;
			}
			output.ignore(switchInsnNode, switchInsnNode);
			output.ignore(startOfThrowBlock, cursor);
		}
	}
}
