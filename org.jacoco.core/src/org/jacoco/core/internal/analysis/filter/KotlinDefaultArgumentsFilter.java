/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Filters branches that Kotlin compiler generates for default arguments.
 */
public final class KotlinDefaultArgumentsFilter implements IFilter {

	private static final String SUFFIX = "$default";

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if ((methodNode.access & Opcodes.ACC_SYNTHETIC) == 0) {
			return;
		}
		if (!methodNode.name.endsWith(SUFFIX)) {
			return;
		}
		if (!context.getClassAnnotations()
				.contains(KotlinGeneratedFilter.KOTLIN_METADATA_DESC)) {
			return;
		}

		final int maskVar = Type.getMethodType(methodNode.desc)
				.getArgumentTypes().length - 2;

		for (AbstractInsnNode i = methodNode.instructions
				.getFirst(); i != null; i = i.getNext()) {
			if (AbstractInsnNode.VAR_INSN != i.getType()) {
				continue;
			}
			if (maskVar != ((VarInsnNode) i).var) {
				continue;
			}

			final AbstractInsnNode jump = i.getNext().getNext().getNext();
			output.ignore(jump, jump);
		}

	}

}
