/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.List;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods annotated with <code>@lombok.Generated</code>.
 */
public class LombokGeneratedFilter implements IFilter {

	public void filter(final String className, final String superClassName,
			final MethodNode methodNode, final IFilterOutput output) {
		if (hasLombokGeneratedAnnotation(methodNode)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private boolean hasLombokGeneratedAnnotation(final MethodNode methodNode) {
		final List<AnnotationNode> runtimeInvisibleAnnotations = methodNode.invisibleAnnotations;
		if (runtimeInvisibleAnnotations != null) {
			for (final AnnotationNode annotation : runtimeInvisibleAnnotations) {
				if ("Llombok/Generated;".equals(annotation.desc)) {
					return true;
				}
			}
		}
		return false;
	}

}
