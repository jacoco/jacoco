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

import java.util.List;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters classes and methods annotated with
 * {@link java.lang.annotation.RetentionPolicy#RUNTIME runtime visible} and
 * {@link java.lang.annotation.RetentionPolicy#CLASS invisible} annotation whose
 * simple name is <code>Generated</code>.
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
		return "Generated;".equals(
				annotation.substring(Math.max(annotation.lastIndexOf('/'),
						annotation.lastIndexOf('$')) + 1));
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
