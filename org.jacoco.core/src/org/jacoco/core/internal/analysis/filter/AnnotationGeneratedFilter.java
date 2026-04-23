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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters classes and methods annotated with
 * {@link java.lang.annotation.RetentionPolicy#RUNTIME runtime visible} and
 * {@link java.lang.annotation.RetentionPolicy#CLASS invisible} annotation whose
 * simple name contains <code>Generated</code>.
 */
final class AnnotationGeneratedFilter implements IFilter {

	private final Set<String> ignoredMethods = new HashSet<String>();

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {

		if (isAnnotated(methodNode, context)) {
			// Find lambdas generated inside this method
			for (org.objectweb.asm.tree.AbstractInsnNode i : methodNode.instructions) {
				if (i.getType() == org.objectweb.asm.tree.AbstractInsnNode.INVOKE_DYNAMIC_INSN) {
					org.objectweb.asm.tree.InvokeDynamicInsnNode indy = (org.objectweb.asm.tree.InvokeDynamicInsnNode) i;
					if (indy.bsmArgs != null && indy.bsmArgs.length > 1) {
						Object arg1 = indy.bsmArgs[1];
						if (arg1 instanceof org.objectweb.asm.Handle) {
							ignoredMethods.add(((org.objectweb.asm.Handle) arg1)
									.getName());
						}
					}
				}
			}

			ignoredMethods.add(methodNode.name);
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
			return;
		}

		if (methodNode.name.startsWith("lambda$")) {
			if (ignoredMethods.contains(methodNode.name)) {
				output.ignore(methodNode.instructions.getFirst(),
						methodNode.instructions.getLast());
			}
		}
	}

	private boolean isAnnotated(final MethodNode methodNode,
			final IFilterContext context) {
		for (String annotation : context.getClassAnnotations()) {
			if (matches(annotation)) {
				return true;
			}
		}
		return presentIn(methodNode.invisibleAnnotations)
				|| presentIn(methodNode.visibleAnnotations);
	}

	private static boolean matches(final String annotation) {
		final String name = annotation
				.substring(Math.max(annotation.lastIndexOf('/'),
						annotation.lastIndexOf('$')) + 1);
		return name.contains("Generated");
	}

	private static boolean presentIn(final List<AnnotationNode> annotations) {
		if (annotations != null) {
			for (AnnotationNode annotation : annotations) {
				if (matches(annotation.desc)) {
					return true;
				}
			}
		}
		return false;
	}

}
