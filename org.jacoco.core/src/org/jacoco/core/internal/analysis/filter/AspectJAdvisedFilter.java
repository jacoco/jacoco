/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lars Grefer - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * Filter for ByteCode created at AspectJ join-points.
 */
public class AspectJAdvisedFilter implements IFilter {

	public void filter(MethodNode methodNode, IFilterContext context,
			IFilterOutput output) {

		if (!AspectJUtil.isAspectJClass(context)) {
			return;
		}

		ignoreInitCode(methodNode, output);

		for (AbstractInsnNode instruction : methodNode.instructions) {
			if (instruction instanceof FieldInsnNode) {
				if (((FieldInsnNode) instruction).name.startsWith("ajp$")) {
					AbstractInsnNode end = instruction;

					MethodInsnNode nextMethod = findNext(MethodInsnNode.class,
							instruction);

					if (nextMethod != null && nextMethod.name.equals("makeJP")
							&& nextMethod.owner.equals(
									"org/aspectj/runtime/reflect/Factory")) {
						end = nextMethod;
					}

					output.ignore(instruction, end);
				}
			}

			if (instruction instanceof MethodInsnNode) {
				String owner = ((MethodInsnNode) instruction).owner;
				String name = ((MethodInsnNode) instruction).name;

				if (owner.equals("org/aspectj/runtime/reflect/Factory")
						&& name.equals("makeJP")) {
					output.ignore(instruction, instruction);
				}

				if (owner.contains("$AjcClosure") && name.equals("<init>")) {
					output.ignore(instruction, instruction);
				}

				if (owner.equals("org/aspectj/runtime/internal/AroundClosure")
						&& name.equals("linkClosureAndJoinPoint")) {
					output.ignore(instruction, instruction);
				}
			}
		}

		for (TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
			checkAfterThrowingHandler(output, tryCatchBlock);
		}
	}

	/**
	 * In some advised methods, ajc generates code before the first line number.
	 * <p>
	 * If we know the correct first line from ajcs attribute, we can ignore all
	 * instructions before the real first line.
	 */
	private void ignoreInitCode(MethodNode methodNode, IFilterOutput output) {
		int methodDeclarationLineNumber = AspectJUtil
				.getMethodDeclarationLineNumber(methodNode);

		if (methodDeclarationLineNumber > 0) {

			LineNumberNode firstLineAfter = findFirstLineAfter(methodNode,
					methodDeclarationLineNumber);

			if (firstLineAfter == null) {

				output.ignore(methodNode.instructions.getFirst(),
						methodNode.instructions.getLast());
			} else if (methodNode.instructions.indexOf(firstLineAfter) > 1) {
				output.ignore(methodNode.instructions.getFirst(),
						firstLineAfter.getPrevious());
			}

		}
	}

	/**
	 * Ignores the exception handler generated for after throwing advices.
	 */
	private static void checkAfterThrowingHandler(IFilterOutput output,
			TryCatchBlockNode tryCatchBlock) {
		if (tryCatchBlock.type == null) {
			return;
		}

		MethodInsnNode aspectOf = findNext(MethodInsnNode.class,
				tryCatchBlock.handler);

		if (aspectOf == null || !"aspectOf".equals(aspectOf.name)
				|| aspectOf.getOpcode() != Opcodes.INVOKESTATIC) {
			return;
		}

		String aspectClass = aspectOf.owner;

		MethodInsnNode aspectCall = findNext(MethodInsnNode.class, aspectOf);

		if (aspectCall != null && aspectCall.owner.equals(aspectClass)) {

			AbstractInsnNode throwNode = findNext(Opcodes.ATHROW, aspectCall);

			if (throwNode != null) {
				output.ignore(tryCatchBlock.handler, throwNode);
			}

		}

	}

	private LineNumberNode findFirstLineAfter(MethodNode methodNode, int line) {
		for (AbstractInsnNode instruction : methodNode.instructions) {
			if (instruction instanceof LineNumberNode) {
				if (((LineNumberNode) instruction).line >= line) {
					return (LineNumberNode) instruction;
				}
			}
		}
		return null;
	}

	public static <T extends AbstractInsnNode> T findNext(Class<T> type,
			AbstractInsnNode start) {

		AbstractInsnNode cursor = start;
		while (cursor != null) {
			cursor = cursor.getNext();

			if (type.isInstance(cursor)) {
				return type.cast(cursor);
			}
		}

		return null;

	}

	public static AbstractInsnNode findNext(int opcode,
			AbstractInsnNode start) {
		AbstractInsnNode cursor = start;
		while (cursor != null) {
			cursor = cursor.getNext();

			if (cursor.getOpcode() == opcode) {
				return cursor;
			}
		}

		return null;
	}
}
