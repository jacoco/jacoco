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

import java.util.Set;

/**
 * Context information provided to filters.
 */
public interface IFilterContext {

	/**
	 * @return vm name of the enclosing class
	 */
	String getClassName();

	/**
	 * @return vm name of the super class of the enclosing class
	 */
	String getSuperClassName();

	/**
	 * @return vm names of the class annotations of the enclosing class
	 */
	Set<String> getClassAnnotations();

	/**
	 * @return file name of the corresponding source file or <code>null</code>
	 *         if not available
	 */
	String getSourceFileName();

	/**
	 * @return value of SourceDebugExtension attribute or <code>null</code> if
	 *         not available
	 */
	String getSourceDebugExtension();

}
