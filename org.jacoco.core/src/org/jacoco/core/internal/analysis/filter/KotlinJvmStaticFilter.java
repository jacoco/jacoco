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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods that Kotlin compiler generates for functions annotated with
 * <a href=
 * "https://kotlinlang.org/docs/java-to-kotlin-interop.html#static-methods">
 * {@code JvmStatic}</a>.
 */
final class KotlinJvmStaticFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if ((methodNode.access & Opcodes.ACC_STATIC) == 0) {
			return;
		}
		if (!isJvmStatic(methodNode)) {
			return;
		}
		if (!isGetStaticCompanion(methodNode.instructions.getFirst(),
				context.getClassName())) {
			return;
		}
		output.ignore(methodNode.instructions.getFirst(),
				methodNode.instructions.getLast());
	}

	private static boolean isGetStaticCompanion(final AbstractInsnNode i,
			final String owner) {
		if (i == null || i.getOpcode() != Opcodes.GETSTATIC) {
			return false;
		}
		final FieldInsnNode f = (FieldInsnNode) i;
		return f.owner.equals(owner) && f.name.equals("Companion");
	}

	private boolean isJvmStatic(final MethodNode methodNode) {
		if (methodNode.visibleAnnotations != null) {
			for (AnnotationNode annotation : methodNode.visibleAnnotations) {
				if ("Lkotlin/jvm/JvmStatic;".equals(annotation.desc)) {
					return true;
				}
			}
		}
		return false;
	}

}
