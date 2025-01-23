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
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods that Kotlin compiler generates for functions and constructors
 * annotated with {@code JvmOverloads}. They are not filtered by
 * {@link KotlinGeneratedFilter} due to <a href=
 * "https://youtrack.jetbrains.com/issue/KT-74091/JvmOverloads-produced-overloads-have-generated-line-number-table-since-2.0">
 * regression in Kotlin compiler version 2.0</a>, which at best might be fixed
 * in version <a href=
 * "https://github.com/jacoco/jacoco/issues/1752#issuecomment-2560095584">2.2.0</a>.
 */
final class KotlinJvmOverloadsFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (methodNode.instructions.size() < 5) {
			return;
		}
		AbstractInsnNode i = methodNode.instructions.getLast();
		if (i.getType() == AbstractInsnNode.LABEL) {
			// lvt label
			i = i.getPrevious();
		}
		if (i.getOpcode() != Opcodes.RETURN) {
			return;
		}
		i = i.getPrevious();
		if (i.getType() != AbstractInsnNode.LINE) {
			return;
		}
		i = i.getPrevious();
		if (i != ((LineNumberNode) i.getNext()).start) {
			return;
		}
		i = i.getPrevious();
		if (!invokeDefault(i)) {
			return;
		}
		for (; i != null; i = i.getPrevious()) {
			if (i.getType() == AbstractInsnNode.LINE) {
				return;
			}
		}
		output.ignore(methodNode.instructions.getFirst(),
				methodNode.instructions.getLast());
	}

	private static boolean invokeDefault(final AbstractInsnNode i) {
		if (i.getOpcode() == Opcodes.INVOKESTATIC) {
			return ((MethodInsnNode) i).name.endsWith("$default");
		} else if (i.getOpcode() == Opcodes.INVOKESPECIAL) {
			return ((MethodInsnNode) i).desc.endsWith(
					"Lkotlin/jvm/internal/DefaultConstructorMarker;)V");
		}
		return false;
	}

}
