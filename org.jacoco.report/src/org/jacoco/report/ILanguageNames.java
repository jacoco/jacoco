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
package org.jacoco.report;

/**
 * Interface to create programming language specific names from VM names.
 */
public interface ILanguageNames {

	/**
	 * Calculates the language specific name of a package.
	 *
	 * @param vmname
	 *            vm name of a package
	 * @return language specific notation for the package
	 */
	String getPackageName(String vmname);

	/**
	 * Calculates the language specific name of a class.
	 *
	 * @param vmname
	 *            vm name of a class
	 * @param vmsignature
	 *            vm signature of the class (may be <code>null</code>)
	 * @param vmsuperclass
	 *            vm name of the superclass of the class (may be
	 *            <code>null</code>)
	 * @param vminterfaces
	 *            vm names of interfaces of the class (may be <code>null</code>)
	 * @return language specific notation of the class
	 */
	String getClassName(String vmname, String vmsignature, String vmsuperclass,
			String[] vminterfaces);

	/**
	 * Calculates the language specific qualified name of a class.
	 *
	 * @param vmname
	 *            vm name of a class
	 * @return language specific qualified notation of the class
	 */
	String getQualifiedClassName(String vmname);

	/**
	 * Calculates the language specific name of a method.
	 *
	 * @param vmclassname
	 *            vm name of a containing class
	 * @param vmmethodname
	 *            vm name of the method
	 * @param vmdesc
	 *            vm method descriptor
	 * @param vmsignature
	 *            vm signature of the method (may be <code>null</code>)
	 * @return language specific notation for the method
	 */
	String getMethodName(String vmclassname, String vmmethodname, String vmdesc,
			String vmsignature);

	/**
	 * Calculates the language specific fully qualified name of a method.
	 *
	 * @param vmclassname
	 *            vm name of a containing class
	 * @param vmmethodname
	 *            vm name of the method
	 * @param vmdesc
	 *            vm method descriptor
	 * @param vmsignature
	 *            vm signature of the method (may be <code>null</code>)
	 * @return language specific notation for the method
	 */
	String getQualifiedMethodName(String vmclassname, String vmmethodname,
			String vmdesc, String vmsignature);

}
