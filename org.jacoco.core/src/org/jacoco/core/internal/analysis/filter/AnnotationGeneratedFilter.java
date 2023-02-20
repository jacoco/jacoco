/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.List;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters classes and methods annotated with
 * {@link java.lang.annotation.RetentionPolicy#RUNTIME runtime visible} and
 * {@link java.lang.annotation.RetentionPolicy#CLASS invisible} annotation whose
 * simple name contains <code>Generated</code>.
 */
public final class AnnotationGeneratedFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {

		for (String annotation : context.getClassAnnotations()) {
			if (matches(annotation)) {
				output.ignore(methodNode.instructions.getFirst(),
						methodNode.instructions.getLast());
				return;
			}
		}

		if (presentIn(methodNode.invisibleAnnotations)
				|| presentIn(methodNode.visibleAnnotations)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}

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
