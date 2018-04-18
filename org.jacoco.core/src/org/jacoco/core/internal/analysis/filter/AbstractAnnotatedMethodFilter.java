/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
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

import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Pattern;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters annotated methods.
 */
abstract class AbstractAnnotatedMethodFilter implements IFilter {

	public void filter(final String className, final String superClassName,
			final MethodNode methodNode, final IFilterOutput output) {
		if (hasAnnotation(methodNode)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private boolean hasAnnotation(final MethodNode methodNode) {
		final List<AnnotationNode> annotations = getAnnotations(methodNode);
		if (annotations != null) {
			for (final AnnotationNode annotation : annotations) {
				if (isMatchingAnnotation(annotation)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Test if the annotation matches certain criteria for a method to be filtered out.
	 *
	 * @param annotationNode
	 *            the annontation to test against
	 * @return true if the annotation matches. Otherwise, false.
	 */
	protected abstract boolean isMatchingAnnotation(AnnotationNode annotationNode);

	/**
	 * Retrieves the annotations to search from a method. Depending on the
	 * retention of the annotation this is either
	 * <code>visibleAnnotations</code> or <code>invisibleAnnotations</code>.
	 * 
	 * @param methodNode
	 *            method to retrieve annotations from
	 * @return list of annotations
	 */
	abstract List<AnnotationNode> getAnnotations(final MethodNode methodNode);

}
