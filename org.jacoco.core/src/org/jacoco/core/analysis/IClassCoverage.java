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

import java.util.Collection;

/**
 * Coverage data of a single class containing methods. The name of this node is
 * the fully qualified class name in VM notation (slash separated).
 *
 * @see IMethodCoverage
 */
public interface IClassCoverage extends ISourceNode {

	/**
	 * Returns the identifier for this class which is the CRC64 signature of the
	 * class definition.
	 *
	 * @return class identifier
	 */
	long getId();

	/**
	 * Returns if the the analyzed class does match the execution data provided.
	 * More precisely if execution data is available for a class with the same
	 * qualified name but with a different class id.
	 *
	 * @return <code>true</code> if this class does not match to the provided
	 *         execution data.
	 */
	boolean isNoMatch();

	/**
	 * Returns the VM signature of the class.
	 *
	 * @return VM signature of the class (may be <code>null</code>)
	 */
	String getSignature();

	/**
	 * Returns the VM name of the superclass.
	 *
	 * @return VM name of the super class (may be <code>null</code>, i.e.
	 *         <code>java/lang/Object</code>)
	 */
	String getSuperName();

	/**
	 * Returns the VM names of implemented/extended interfaces.
	 *
	 * @return VM names of implemented/extended interfaces
	 */
	String[] getInterfaceNames();

	/**
	 * Returns the VM name of the package this class belongs to.
	 *
	 * @return VM name of the package
	 */
	String getPackageName();

	/**
	 * Returns the optional name of the corresponding source file.
	 *
	 * @return name of the corresponding source file
	 */
	String getSourceFileName();

	/**
	 * Returns the methods included in this class.
	 *
	 * @return methods of this class
	 */
	Collection<IMethodCoverage> getMethods();

}
