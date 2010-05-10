/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.analysis;

import java.util.Collection;

/**
 * Coverage data of a single class.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassCoverage extends CoverageNodeImpl {

	private final String signature;
	private final String superName;
	private final String[] interfaces;
	private final Collection<MethodCoverage> methods;
	private final String sourceFileName;

	/**
	 * Creates a class coverage data object with the given parameters.
	 * 
	 * @param name
	 *            vm name of the class
	 * @param signature
	 *            vm signature of the class
	 * @param superName
	 *            vm name of the superclass of this class
	 * @param interfaces
	 *            vm names of interfaces of this class
	 * @param sourceFileName
	 *            optional name of the corresponding source file
	 * @param methods
	 *            contained methods
	 */
	public ClassCoverage(final String name, final String signature,
			final String superName, final String[] interfaces,
			final String sourceFileName,
			final Collection<MethodCoverage> methods) {
		super(ElementType.CLASS, name, true);
		this.signature = signature;
		this.superName = superName;
		this.interfaces = interfaces;
		this.sourceFileName = sourceFileName;
		this.methods = methods;
		increment(methods);
		// As class is considered as covered when at least one method is
		// covered:
		final boolean covered = methodCounter.getCoveredCount() > 0;
		this.classCounter = CounterImpl.getInstance(covered);
	}

	/**
	 * Returns the VM signature of the class.
	 * 
	 * @return VM signature of the class (may be <code>null</code>)
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Returns the VM name of the superclass.
	 * 
	 * @return VM name of the super class (may be <code>null</code>, i.e.
	 *         <code>java/lang/Object</code>)
	 */
	public String getSuperName() {
		return superName;
	}

	/**
	 * Returns the VM names of implemented/extended interfaces
	 * 
	 * @return VM names of implemented/extended interfaces
	 */
	public String[] getInterfaceNames() {
		return interfaces;
	}

	/**
	 * Returns the VM name of the package this class belongs to.
	 * 
	 * @return VM name of the package
	 */
	public String getPackageName() {
		final int pos = getName().lastIndexOf('/');
		return pos == -1 ? "" : getName().substring(0, pos);
	}

	/**
	 * Returns the optional name of the corresponding source file.
	 * 
	 * @return name of the corresponding source file
	 */
	public String getSourceFileName() {
		return sourceFileName;
	}

	/**
	 * Returns the methods included in this class.
	 * 
	 * @return methods of this class
	 */
	public Collection<MethodCoverage> getMethods() {
		return methods;
	}

}
