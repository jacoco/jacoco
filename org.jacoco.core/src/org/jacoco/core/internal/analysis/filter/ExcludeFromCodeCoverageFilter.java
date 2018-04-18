/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phi Lieu - add ExcludeFromCodeCoverage annotation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Filters methods annotated with <code>ExcludeFromCodeCoverage</code>. The annotation class can be from any
 * Java package.
 */
public class ExcludeFromCodeCoverageFilter extends AbstractAnnotatedMethodFilter {

    @Override
    protected boolean isMatchingAnnotation(AnnotationNode annotationNode) {
		return Pattern.matches("L[a-zA-Z0-9/]*ExcludeFromCodeCoverage;", annotationNode.desc);
    }

    @Override
    List<AnnotationNode> getAnnotations(final MethodNode methodNode) {
        return methodNode.invisibleAnnotations;
    }
}
