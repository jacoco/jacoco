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

import org.jacoco.core.internal.flow.MethodProbesBaseAdapter;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
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
		 * @return A {@link MethodVisitor} that wraps the provided delegate or
		 *         simply returns the provided {@link MethodProbesVisitor}
		 *         instance if no extra processing is required.
		 */
		public MethodProbesBaseAdapter visitMethod(
				MethodProbesBaseAdapter delegate);

		/**
		 * Simple {@link ICoverageFilter} which doesn't filter anything out.
		 */
		public static class NoFilter implements ICoverageFilter {
			public boolean includeClass(final String className) {
				return true;
			}

			public MethodProbesBaseAdapter visitMethod(
					final MethodProbesBaseAdapter delegate) {
				return delegate;
			}

			public boolean enabled() {
				return true;
			}
		}
	}
}
