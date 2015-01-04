/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import java.util.ArrayList;
import java.util.Collection;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;

/**
 * Implementation of {@link IClassCoverage}.
 */
public class ClassCoverageImpl extends SourceNodeImpl implements IClassCoverage {

	private final long id;
	private final boolean noMatch;
	private final String signature;
	private final String superName;
	private final String[] interfaces;
	private final Collection<IMethodCoverage> methods;
	private String sourceFileName;

	/**
	 * Creates a class coverage data object with the given parameters.
	 * 
	 * @param name
	 *            vm name of the class
	 * @param id
	 *            class identifier
	 * @param noMatch
	 *            <code>true</code>, if class id does not match with execution
	 *            data
	 * @param signature
	 *            vm signature of the class
	 * @param superName
	 *            vm name of the superclass of this class
	 * @param interfaces
	 *            vm names of interfaces of this class
	 */
	public ClassCoverageImpl(final String name, final long id,
			final boolean noMatch, final String signature,
			final String superName, final String[] interfaces) {
		super(ElementType.CLASS, name);
		this.id = id;
		this.noMatch = noMatch;
		this.signature = signature;
		this.superName = superName;
		this.interfaces = interfaces;
		this.methods = new ArrayList<IMethodCoverage>();
		this.classCounter = CounterImpl.COUNTER_1_0;
	}

	/**
	 * Add a method to this class.
	 * 
	 * @param method
	 *            method data to add
	 */
	public void addMethod(final IMethodCoverage method) {
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

	// === IClassCoverage implementation ===

	public long getId() {
		return id;
	}

	public boolean isNoMatch() {
		return noMatch;
	}

	public String getSignature() {
		return signature;
	}

	public String getSuperName() {
		return superName;
	}

	public String[] getInterfaceNames() {
		return interfaces;
	}

	public String getPackageName() {
		final int pos = getName().lastIndexOf('/');
		return pos == -1 ? "" : getName().substring(0, pos);
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public Collection<IMethodCoverage> getMethods() {
		return methods;
	}

}
