/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Hare Robertson - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.util.Iterator;
import java.util.List;

import org.jacoco.core.analysis.ICoverageFilterStatus.ICoverageFilter;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Filter that combines multiple filters
 */
public class CompositeCoverageFilter implements ICoverageFilter {

	private final List<ICoverageFilter> filters;

	/**
	 * @param filters
	 */
	public CompositeCoverageFilter(final List<ICoverageFilter> filters) {
		this.filters = filters;
	}

	public boolean enabled() {
		boolean enabled = true;
		final Iterator<ICoverageFilter> iterator = filters.iterator();
		while (enabled && iterator.hasNext()) {
			final ICoverageFilter filter = iterator.next();
			enabled = enabled && filter.enabled();
		}
		return enabled;
	}

	public boolean includeClass(final String className) {
		boolean include = true;
		final Iterator<ICoverageFilter> iterator = filters.iterator();
		while (iterator.hasNext()) {
			final ICoverageFilter filter = iterator.next();
			include = include && filter.includeClass(className);
		}
		return include;
	}

	public ClassVisitor visitClass(final ClassVisitor delegate) {
		ClassVisitor visitor = delegate;
		final Iterator<ICoverageFilter> iterator = filters.iterator();
		while (iterator.hasNext()) {
			final ICoverageFilter filter = iterator.next();
			visitor = filter.visitClass(visitor);
		}
		return visitor;
	}

	public MethodVisitor preVisitMethod(final String name,
			final String desc, final MethodVisitor delegate) {
		MethodVisitor visitor = delegate;
		final Iterator<ICoverageFilter> iterator = filters.iterator();
		while (iterator.hasNext()) {
			final ICoverageFilter filter = iterator.next();
			visitor = filter.preVisitMethod(name, desc, visitor);
		}
		return visitor;
	}

	public MethodProbesVisitor visitMethod(final String name,
			final String desc, final MethodProbesVisitor delegate) {
		MethodProbesVisitor visitor = delegate;
		final Iterator<ICoverageFilter> iterator = filters.iterator();
		while (iterator.hasNext()) {
			final ICoverageFilter filter = iterator.next();
			visitor = filter.visitMethod(name, desc, visitor);
		}
		return visitor;
	}

}
