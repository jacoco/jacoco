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
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Coverage data of a single class.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class ClassCoverage extends SourceNodeImpl implements ISourceNode {

	private final long id;
	private final String signature;
	private final String superName;
	private final String[] interfaces;
	private final Collection<MethodCoverage> methods;
	private String sourceFileName;

	/**
	 * Creates a class coverage data object with the given parameters.
	 * 
	 * @param name
	 *            vm name of the class
	 * @param id
	 *            class identifier
	 * @param signature
	 *            vm signature of the class
	 * @param superName
	 *            vm name of the superclass of this class
	 * @param interfaces
	 *            vm names of interfaces of this class
	 */
	public ClassCoverage(final String name, final long id,
			final String signature, final String superName,
			final String[] interfaces) {
		super(ElementType.CLASS, name);
		this.id = id;
		this.signature = signature;
		this.superName = superName;
		this.interfaces = interfaces;
		this.methods = new ArrayList<MethodCoverage>();
		this.classCounter = CounterImpl.COUNTER_1_0;
	}

	/**
	 * Add a method to this class.
	 * 
	 * @param method
	 *            method data to add
	 */
	public void addMethod(final MethodCoverage method) {
		this.methods.add(method);
		increment(method);
		// As class is considered as covered when at least one method is
		// covered:
		if (methodCounter.getCoveredCount() > 0) {
			this.classCounter = CounterImpl.COUNTER_0_1;
		}
	}

	/**
	 * Sets the name of the corresponding source file for this class.
	 * 
	 * @param sourceFileName
	 *            name of the source file
	 */
	public void setSourceFileName(final String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	/**
	 * Returns the identifier for this class which is the CRC64 signature of the
	 * class definition.
	 * 
	 * @return class identifier
	 */
	public long getId() {
		return id;
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
