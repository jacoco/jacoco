/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

/**
 * Filters default branch of switch statement which covers all items of enum.
 */
public final class EnumSwitchFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher();
		for (final AbstractInsnNode i : methodNode.instructions) {
			matcher.match(i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		public void match(final AbstractInsnNode start,
				final IFilterOutput output) {
			cursor = start;

			if (cursor.getOpcode() != Opcodes.INVOKEVIRTUAL) {
				return;
			}
			final MethodInsnNode m = (MethodInsnNode) cursor;
			if (!"ordinal".equals(m.name) || !"()I".equals(m.desc)) {
				return;
			}

			// Aggressively search for the switch instruction, skipping
			// intermediate ops
			// (like array loads, checkcasts, etc) up to a limit.
			for (int i = 0; i < 20; i++) {
				next();
				if (cursor == null) {
					return;
				}
				final int opcode = cursor.getOpcode();
				if (opcode == Opcodes.TABLESWITCH
						|| opcode == Opcodes.LOOKUPSWITCH) {
					break;
				}
				// Stop at control flow boundaries
				if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
						|| opcode == Opcodes.ATHROW || opcode == Opcodes.GOTO) {
					return;
				}
			}
			// Re-verify we found a switch
			if (cursor.getOpcode() != Opcodes.TABLESWITCH
					&& cursor.getOpcode() != Opcodes.LOOKUPSWITCH) {
				return;
			}
			final List<LabelNode> labels;
			final LabelNode dflt;
			if (cursor.getOpcode() == Opcodes.TABLESWITCH) {
				final TableSwitchInsnNode s = (TableSwitchInsnNode) cursor;
				labels = s.labels;
				dflt = s.dflt;
			} else if (cursor.getOpcode() == Opcodes.LOOKUPSWITCH) {
				final LookupSwitchInsnNode s = (LookupSwitchInsnNode) cursor;
				labels = s.labels;
				dflt = s.dflt;
			} else {
				return;
			}

			if (labels.contains(dflt)) {
				return;
			}

			output.replaceBranches(cursor,
					Replacements.ignoreDefaultBranch(cursor));

			// Only ignore the default branch instructions when they
			// are a compiler-generated artifact (a single GOTO with no
			// real instructions). If the default path contains any
			// user-written code, it must still require coverage.
			ignoreCompilerGeneratedDefault(dflt, output);
		}

		/**
		 * Ignores the default branch only when it consists of a single GOTO
		 * instruction (a compiler-generated jump to the code after the switch).
		 * This handles ECJ which may assign the GOTO to the closing brace line
		 * that has no jacoco:ignore comment. If the default branch contains any
		 * other instructions, it is user-written code and is NOT ignored.
		 */
		private void ignoreCompilerGeneratedDefault(final LabelNode dflt,
				final IFilterOutput output) {
			// Walk past non-opcode nodes (labels, frames, line numbers)
			AbstractInsnNode node = dflt.getNext();
			while (node != null && node.getOpcode() == -1) {
				node = node.getNext();
			}
			if (node == null) {
				return;
			}
			// Only ignore if the first real instruction is a GOTO
			// (compiler-generated jump to after the switch block)
			if (node.getOpcode() == Opcodes.GOTO) {
				output.ignore(dflt, node);
			}
		}
	}

}
