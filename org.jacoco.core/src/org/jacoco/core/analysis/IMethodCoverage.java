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
package org.jacoco.core.analysis;

/**
 * Coverage data of a single method. The name of this node is the local method
 * name.
 */
public interface IMethodCoverage extends ISourceNode {

	/**
	 * Returns the descriptor of the method.
	 *
	 * @return descriptor
	 */
	String getDesc();

	/**
	 * Returns the generic signature of the method if defined.
	 *
	 * @return generic signature or <code>null</code>
	 */
	String getSignature();

}
