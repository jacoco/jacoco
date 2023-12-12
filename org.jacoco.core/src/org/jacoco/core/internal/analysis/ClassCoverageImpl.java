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
package org.jacoco.core.internal.analysis;

import java.util.ArrayList;
import java.util.Collection;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;

/**
 * Implementation of {@link IClassCoverage}.
 */
public class ClassCoverageImpl extends SourceNodeImpl
		implements IClassCoverage {

	private final long id;
	private final boolean noMatch;
	private final Collection<IMethodCoverage> methods;
	private String signature;
	private String superName;
	private String[] interfaces;
	private String sourceFileName;

	/**
	 * Creates a class coverage data object with the given parameters.
	 *
	 * @param name
	 *            VM name of the class
	 * @param id
	 *            class identifier
	 * @param noMatch
	 *            <code>true</code>, if class id does not match with execution
	 *            data
	 */
	public ClassCoverageImpl(final String name, final long id,
			final boolean noMatch) {
		super(ElementType.CLASS, name);
		this.id = id;
		this.noMatch = noMatch;
		this.methods = new ArrayList<IMethodCoverage>();
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
		// Class is considered as covered when at least one method is covered:
		if (methodCounter.getCoveredCount() > 0) {
			this.classCounter = CounterImpl.COUNTER_0_1;
		} else {
			this.classCounter = CounterImpl.COUNTER_1_0;
		}
	}

	/**
	 * Sets the VM signature of the class.
	 *
	 * @param signature
	 *            VM signature of the class (may be <code>null</code>)
	 */
	public void setSignature(final String signature) {
		this.signature = signature;
	}

	/**
	 * Sets the VM name of the superclass.
	 *
	 * @param superName
	 *            VM name of the super class (may be <code>null</code>, i.e.
	 *            <code>java/lang/Object</code>)
	 */
	public void setSuperName(final String superName) {
		this.superName = superName;
	}

	/**
	 * Sets the VM names of implemented/extended interfaces.
	 *
	 * @param interfaces
	 *            VM names of implemented/extended interfaces
	 */
	public void setInterfaces(final String[] interfaces) {
		this.interfaces = interfaces;
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
