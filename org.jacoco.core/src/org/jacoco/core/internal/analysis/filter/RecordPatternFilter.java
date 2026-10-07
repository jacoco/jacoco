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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Filters code that is generated for record patterns.
 */
final class RecordPatternFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher();
		for (final TryCatchBlockNode t : methodNode.tryCatchBlocks) {
			if ("java/lang/Throwable".equals(t.type)) {
				matcher.match(t, output);
			}
		}
	}

	private static class Matcher extends AbstractMatcher {

		/**
		 * <pre>
		 * record R(String c1, Object c2, int c3, ...)
		 *
		 * void example(Object o) {
		 *     switch (o) {
		 *     ...
		 *     case R(String c1, String c2, int c3, ...) // pattern
		 *     when ... // guard
		 *     -> ... // case body
		 *     ...
		 *     }
		 * }
		 * </pre>
		 *
		 * <pre>
		 * labelTypeSwitch:
		 *   ALOAD
		 *   ILOAD
		 *   INVOKEDYNAMIC typeSwitch
		 *   TABLESWITCH
		 *     ...
		 *     index: labelPattern
		 *     ...
		 *
		 * labelPattern:
		 *   ALOAD
		 *   CHECKCAST R
		 *   ASTORE
		 *
		 *   // extraction of c1
		 *   ALOAD
		 *   INVOKEVIRTUAL R.c1
		 *   ASTORE
		 *   ALOAD // TODO
		 *   ASTORE
		 *
		 *   // extraction of c2
		 *   ALOAD
		 *   INVOKEVIRTUAL R.c2
		 *   ASTORE
		 *   ALOAD
		 *   INSTANCEOF String
		 *   IFEQ labelRestart
		 *   ALOAD // TODO
		 *   CHECKCAST
		 *   ASTORE
		 *
		 *   // extraction of c3
		 *   ALOAD
		 *   INVOKEVIRTUAL R.c3
		 *   ISTORE
		 *   ICONST_1
		 *   IFEQ labelRestart
		 *
		 *   ... // extraction of remaining components
		 *
		 *   ... // guard
		 *
		 *   GOTO labelCaseBody
		 * labelRestart:
		 *   ICONST index+1
		 *   ISTORE
		 *   GOTO labelTypeSwitch
		 * labelCaseBody:
		 *   ... // case body
		 * </pre>
		 */
		void match(final TryCatchBlockNode t, final IFilterOutput output) {
			cursor = t.start;
			nextIs(Opcodes.INVOKEVIRTUAL);
			final MethodInsnNode invokeInstruction = (MethodInsnNode) cursor;
			if (cursor == null || cursor.getNext() != t.end) {
				return;
			}

			cursor = t.handler;
			nextIsVar(Opcodes.ASTORE, "cause");
			nextIsType(Opcodes.NEW, "java/lang/MatchException");
			nextIs(Opcodes.DUP);
			nextIsVar(Opcodes.ALOAD, "cause");
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"toString", "()Ljava/lang/String;");
			nextIsVar(Opcodes.ALOAD, "cause");
			nextIsInvoke(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
					"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return;
			}
			output.ignore(t.handler, cursor);

			cursor = t.end;
			final Type componentType = Type
					.getReturnType(invokeInstruction.desc);
			if (isPrimitive(componentType)) {
				nextIs(componentType.getOpcode(Opcodes.ISTORE));
				nextIs(Opcodes.ICONST_1);
				if (cursor == null) {
					// JDK 23 - 25
					cursor = t.end;
					nextIs(componentType.getOpcode(Opcodes.ISTORE));
					// TODO depends on type?
					nextIs(Opcodes.ILOAD);
					nextIs(Opcodes.ISTORE);
					nextIs(Opcodes.ICONST_1);
				}
			} else {
				nextIs(Opcodes.ASTORE);
				nextIs(Opcodes.ALOAD);
				nextIs(Opcodes.INSTANCEOF);
			}
			nextIs(Opcodes.IFEQ);
			if (cursor == null) {
				return;
			}
			final JumpInsnNode jump = (JumpInsnNode) cursor;

			nextIs(Opcodes.GOTO);
			final AbstractInsnNode componentEnd = cursor != null ? cursor
					: jump;
			output.ignore(t.start, componentEnd);

			cursor = jump.label;
			next(/* ICONST_x, BIPUSH, SIPUSH */);
			final Integer index = getIndex(cursor);
			if (index == null) {
				return;
			}
			nextIs(Opcodes.ISTORE);
			nextIs(Opcodes.GOTO);
			if (cursor == null) {
				return;
			}
			final JumpInsnNode jumpToSwitch = (JumpInsnNode) cursor;

			cursor = jumpToSwitch.label;
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.ILOAD);
			nextIsInvokeDynamicTypeSwitch();
			nextIsSwitch();
			final LabelNode labelNode = getLabel(cursor, index - 1);
			if (labelNode != null) {
				output.ignore(jump.label, jumpToSwitch);
				output.ignore(labelNode, componentEnd);
			}
		}

		private void nextIsInvokeDynamicTypeSwitch() {
			nextIs(Opcodes.INVOKEDYNAMIC);
			final InvokeDynamicInsnNode i = (InvokeDynamicInsnNode) cursor;
			if (i != null && "typeSwitch".equals(i.name)
					&& "java/lang/runtime/SwitchBootstraps"
							.equals(i.bsm.getOwner())
					&& "typeSwitch".equals(i.bsm.getName())) {
				return;
			}
			cursor = null;
		}

	}

	private static boolean isPrimitive(final Type type) {
		switch (type.getSort()) {
		case Type.BOOLEAN:
		case Type.CHAR:
		case Type.BYTE:
		case Type.SHORT:
		case Type.INT:
		case Type.FLOAT:
		case Type.LONG:
		case Type.DOUBLE:
			return true;
		default:
			return false;
		}
	}

	private static LabelNode getLabel(final AbstractInsnNode instruction,
			final int i) {
		if (instruction == null) {
			return null;
		} else if (Opcodes.TABLESWITCH == instruction.getOpcode()) {
			final TableSwitchInsnNode switchNode = (TableSwitchInsnNode) instruction;
			return switchNode.min <= i && i <= switchNode.max
					? switchNode.labels.get(i - switchNode.min)
					: null;
		} else if (Opcodes.LOOKUPSWITCH == instruction.getOpcode()) {
			final LookupSwitchInsnNode switchNode = (LookupSwitchInsnNode) instruction;
			return 0 <= i && i < switchNode.labels.size()
					? switchNode.labels.get(i)
					: null;
		} else {
			return null;
		}
	}

	private static Integer getIndex(final AbstractInsnNode i) {
		if (i == null) {
			return null;
		}
		switch (i.getOpcode()) {
		case Opcodes.ICONST_0:
			return 0;
		case Opcodes.ICONST_1:
			return 1;
		case Opcodes.ICONST_2:
			return 2;
		case Opcodes.ICONST_3:
			return 3;
		case Opcodes.ICONST_4:
			return 4;
		case Opcodes.ICONST_5:
			return 5;
		case Opcodes.BIPUSH:
		case Opcodes.SIPUSH:
			return ((IntInsnNode) i).operand;
		default:
			return null;
		}
	}

}
