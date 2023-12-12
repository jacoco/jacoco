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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Filters bytecode that Kotlin compiler generates for <code>when</code>
 * expressions with a <code>String</code>.
 */
public final class KotlinWhenStringFilter implements IFilter {

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

			if (Opcodes.ALOAD != start.getOpcode()) {
				return;
			}
			cursor = start;
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
					"()I");
			nextIsSwitch();
			if (cursor == null) {
				return;
			}
			vars.put("s", (VarInsnNode) start);

			final AbstractInsnNode s = cursor;
			final int hashCodes;
			final LabelNode defaultLabel;
			if (s.getOpcode() == Opcodes.LOOKUPSWITCH) {
				final LookupSwitchInsnNode lookupSwitch = (LookupSwitchInsnNode) cursor;
				defaultLabel = lookupSwitch.dflt;
				hashCodes = lookupSwitch.labels.size();
			} else {
				final TableSwitchInsnNode tableSwitch = (TableSwitchInsnNode) cursor;
				defaultLabel = tableSwitch.dflt;
				hashCodes = tableSwitch.labels.size();
			}

			if (hashCodes == 0) {
				return;
			}

			final Set<AbstractInsnNode> replacements = new HashSet<AbstractInsnNode>();
			replacements.add(skipNonOpcodes(defaultLabel));

			for (int i = 1; i <= hashCodes; i++) {
				while (true) {
					nextIsVar(Opcodes.ALOAD, "s");
					nextIs(Opcodes.LDC);
					nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/String",
							"equals", "(Ljava/lang/Object;)Z");
					// jump to next comparison or default case
					nextIs(Opcodes.IFEQ);
					final JumpInsnNode jump = (JumpInsnNode) cursor;
					next();
					if (cursor == null) {
						return;
					} else if (cursor.getOpcode() == Opcodes.GOTO) {
						// jump to case body
						replacements.add(
								skipNonOpcodes(((JumpInsnNode) cursor).label));
						if (jump.label == defaultLabel) {
							// end of comparisons for same hashCode
							break;
						}
					} else if (i == hashCodes && jump.label == defaultLabel) {
						// case body
						replacements.add(cursor);
						cursor = jump;
						break;
					} else {
						return;
					}
				}
			}

			output.ignore(s.getNext(), cursor);
			output.replaceBranches(s, replacements);
		}
	}

}
