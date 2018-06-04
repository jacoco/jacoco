/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nikolay Krasko - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;
import java.util.Set;

public class KotlinNoSourceLinesFilter implements IFilter {
	static final String KOTLIN_METADATA_DESC = "Lkotlin/Metadata;";

	public void filter(String className,
			String superClassName,
			Set<String> classAnnotations,
			String sourceFileName,
			MethodNode methodNode,
			IFilterOutput output) {
		if (sourceFileName == null) {
			// probably full debug information is missing
			// disabled filtering as all methods might be erroneously skipped
			return;
		}

		if (!classAnnotations.contains(KOTLIN_METADATA_DESC)) {
			return;
		}

		if (hasLineNumber(methodNode)) return;

		output.ignore(
				methodNode.instructions.getFirst(),
				methodNode.instructions.getLast());
	}

	private boolean hasLineNumber(MethodNode methodNode) {
		ListIterator<AbstractInsnNode> insnIterator =
				methodNode.instructions.iterator();
		while (insnIterator.hasNext()) {
			AbstractInsnNode insnNode = insnIterator.next();
			if (insnNode instanceof LineNumberNode) {
				return true;
			}
		}

		return false;
	}
}
