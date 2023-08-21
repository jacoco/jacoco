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

import org.objectweb.asm.tree.*;

import java.util.regex.Pattern;

public class AspectJAdvisedFilter implements IFilter {

	private static final Pattern ASPECTJ_AROUND_BODY_PATTERN = Pattern
			.compile(".*_aroundBody\\d+");

	private static final Pattern AJC_CLOSURE_PATTERN = Pattern
			.compile(".*\\$AjcClosure\\d+");

	@Override
	public void filter(MethodNode methodNode, IFilterContext context,
			IFilterOutput output) {

		if (!AspectJUtil.isAspectJClass(context)) {
			return;
		}

		if (isAroundClosureClass(context)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}

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

	private boolean isAroundClosureClass(IFilterContext context) {
		return context.getSuperClassName()
				.equals("org/aspectj/runtime/internal/AroundClosure")
				&& AJC_CLOSURE_PATTERN.matcher(context.getClassName())
						.matches();
	}

	public static boolean isAspectJAroundBody(MethodNode methodNode) {
		return ASPECTJ_AROUND_BODY_PATTERN.matcher(methodNode.name).matches()
				&& methodNode.desc.contains("Lorg/aspectj/lang/JoinPoint");
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
}
