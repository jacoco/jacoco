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

import java.util.List;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods annotated with <code>@lombok.Generated</code>.
 */
public final class LombokGeneratedFilter extends AbstractAnnotatedMethodFilter {

	/**
	 * New filter.
	 */
	public LombokGeneratedFilter() {
		super("lombok/Generated");
	}

	@Override
	List<AnnotationNode> getAnnotations(final MethodNode methodNode) {
		return methodNode.invisibleAnnotations;
	}

}
