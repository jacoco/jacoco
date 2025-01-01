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

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters out instructions that were inlined by Kotlin compiler.
 */
final class KotlinInlineFilter implements IFilter {

	private int firstGeneratedLineNumber = -1;

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (context.getSourceDebugExtension() == null) {
			return;
		}

		if (firstGeneratedLineNumber == -1) {
			firstGeneratedLineNumber = getFirstGeneratedLineNumber(
					context.getClassName(), context.getSourceFileName(),
					context.getSourceDebugExtension());
		}

		int line = 0;
		for (final AbstractInsnNode i : methodNode.instructions) {
			if (AbstractInsnNode.LINE == i.getType()) {
				line = ((LineNumberNode) i).line;
			}
			if (line >= firstGeneratedLineNumber) {
				output.ignore(i, i);
			}
		}
	}

	private static int getFirstGeneratedLineNumber(final String className,
			final String sourceFileName, final String smap) {
		int min = Integer.MAX_VALUE;
		for (final KotlinSMAP.Mapping mapping : new KotlinSMAP(sourceFileName,
				smap).mappings()) {
			if (className.equals(mapping.inputClassName())
					&& mapping.inputStartLine() == mapping.outputStartLine()) {
				continue;
			}
			min = Math.min(mapping.outputStartLine(), min);
		}
		return min;
	}

}
