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
package org.jacoco.core.analysis.filters;

import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Restricted interface to an {@link ICoverageFilter} which can only return the
 * current status
 */
public interface ICoverageFilterStatus {

	/**
	 * @return True if coverage is enabled
	 */
	public boolean enabled();

	/**
	 * Coverage Filter base interface
	 */
	public interface ICoverageFilter extends ICoverageFilterStatus {

		/**
		 * @param className
		 * @return True if className should be included
		 */
		public boolean includeClass(String className);

		/**
		 * @param delegate
		 * @return A {@link ClassVisitor} that wraps the provided delegate or
		 *         simply returns the provided {@link ClassVisitor} instance if
		 *         no extra processing is required.
		 */
		public ClassVisitor visitClass(ClassVisitor delegate);

		/**
		 * @param name
		 *            Method name
		 * @param desc
		 *            Method signature
		 * @param delegate
		 *            may be null
		 * @return A {@link MethodVisitor} that wraps the provided delegate or
		 *         simply returns the provided {@link MethodVisitor} instance if
		 *         no extra processing is required.
		 */
		public MethodVisitor preVisitMethod(final String name,
				final String desc, MethodVisitor delegate);

		/**
		 * @param name
		 *            Method name
		 * @param desc
		 *            Method signature
		 * @param delegate
		 * @return A {@link MethodProbesVisitor} that wraps the provided
		 *         delegate or simply returns the provided
		 *         {@link MethodProbesVisitor} instance if no extra processing
		 *         is required.
		 */
		public MethodProbesVisitor visitMethod(String name, String desc,
				MethodProbesVisitor delegate);

		/**
		 * Simple {@link ICoverageFilter} which doesn't filter anything out.
		 */
		public static class NoFilter implements ICoverageFilter {
			public boolean includeClass(final String className) {
				return true;
			}

			public ClassVisitor visitClass(final ClassVisitor delegate) {
				return delegate;
			}

			public MethodVisitor preVisitMethod(final String name,
					final String desc, final MethodVisitor delegate) {
				return delegate;
			}

			public MethodProbesVisitor visitMethod(final String name,
					final String desc,

					final MethodProbesVisitor visitor) {
				return visitor;
			}

			public boolean enabled() {
				return true;
			}
		}
	}
}
