/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report;

/**
 * Interface to create programming language specific names from VM names.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface ILanguageNames {

	/**
	 * Calculates the language specific name of a package.
	 * 
	 * @param vmname
	 *            vm name of a package
	 * @return language specific notation for the package
	 */
	public String getPackageName(String vmname);

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
	public String getClassName(String vmname, String vmsignature,
			String vmsuperclass, String[] vminterfaces);

	/**
	 * Calculates the language specific name of a method.
	 * 
	 * @param vmclassname
	 *            vm name of a containing class
	 * @param vmmethodname
	 *            vm name of the method
	 * @param vmdesc
	 *            vm parameter description of the method
	 * @param vmsignature
	 *            vm signature of the method (may be <code>null</code>)
	 * @return language specific notation for the method
	 */
	public String getMethodName(String vmclassname, String vmmethodname,
			String vmdesc, String vmsignature);

}
