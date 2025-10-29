/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods that Kotlin compiler generates for <a href=
 * "https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-jvm-expose-boxed/">@JvmExposeBoxed</a>.
 */
final class KotlinExposeBoxedFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (shouldFilter(methodNode)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private static boolean shouldFilter(final MethodNode methodNode) {
		if (!hasAnnotation(methodNode)) {
			return false;
		}
		for (final AbstractInsnNode i : methodNode.instructions) {
			if (i.getType() == AbstractInsnNode.METHOD_INSN) {
				final MethodInsnNode mi = (MethodInsnNode) i;
				if (Opcodes.INVOKEVIRTUAL == i.getOpcode()
						&& "unbox-impl".equals(mi.name)) {
					return mi.getPrevious().getOpcode() == Opcodes.ALOAD;
				} else if (Opcodes.INVOKESTATIC == i.getOpcode()
						&& "box-impl".equals(mi.name)) {
					return mi.getNext().getOpcode() == Opcodes.ARETURN;
				}
			}
		}
		return false;
	}

	private static boolean hasAnnotation(final MethodNode methodNode) {
		if (methodNode.invisibleAnnotations != null) {
			for (final AnnotationNode annotationNode : methodNode.invisibleAnnotations) {
				if ("Lkotlin/jvm/JvmExposeBoxed;".equals(annotationNode.desc)) {
					return true;
				}
			}
		}
		return false;
	}

}
