/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters branches in bytecode that the Kotlin compiler generates for
 * <code>for</code> loops as they are not coverable most of the time.
 */
public class KotlinForLoopFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher();
		for (final AbstractInsnNode node : methodNode.instructions) {
			matcher.match(node, output);
		}
	}

	private static class Matcher extends AbstractMatcher {

		public void match(final AbstractInsnNode start, IFilterOutput output) {
			if (start.getOpcode() != Opcodes.IF_ICMPGE
					&& start.getOpcode() != Opcodes.IF_ICMPGT) {
				return;
			}
			cursor = start;
			LabelNode jumpTarget = ((JumpInsnNode) cursor).label;
			if (isLoop(jumpTarget)) {
				output.ignore(start, start);
				output.ignore(jumpTarget.getPrevious(),
						jumpTarget.getPrevious());
			}
		}

		private boolean isLoop(LabelNode jumpTarget) {
			nextIs(Opcodes.ILOAD);
			nextIs(Opcodes.ISTORE);
			nextIs(Opcodes.IINC);
			// follow the jump node
			for (AbstractInsnNode j = cursor; j != null; j = j.getNext()) {
				if (j == jumpTarget) {
					// if the node prior to the jump target matches
					// we can be sure that this is the loop we are looking for
					int previousOpcode = j.getPrevious().getOpcode();
					return previousOpcode == Opcodes.IF_ICMPLT
							|| previousOpcode == Opcodes.IF_ICMPNE
							|| previousOpcode == Opcodes.IF_ICMPLE;
				}
			}
			return false;
		}
	}
}
