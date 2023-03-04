/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
	 * @return names of the class attributes
	 */
	Set<String> getClassAttributes();

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
