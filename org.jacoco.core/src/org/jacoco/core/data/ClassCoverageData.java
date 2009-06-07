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
package org.jacoco.core.data;

import java.util.Collection;

/**
 * Coverage data of a single class.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassCoverageData extends CoverageDataImpl {

	private final String name;

	private final String bundle;

	/**
	 * Creates a class coverage data object with the given parameters.
	 * 
	 * @param name
	 *            vm name of the class
	 * @param bundle
	 *            optional bundle oder <code>null</code>
	 * @param methods
	 *            contained methods
	 */
	public ClassCoverageData(final String name, final String bundle,
			final Collection<ICoverageData> methods) {
		super(ElementType.CLASS, true);
		this.name = name;
		this.bundle = bundle;
		addAll(methods);
		// As class is considered as covered when at least one method is
		// covered:
		final boolean covered = methodCounter.getCoveredCount() > 0;
		this.classCounter = CounterImpl.getInstance(covered);
	}

	/**
	 * Return the vm name of this class.
	 * 
	 * @return vm name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the vm name of the package this class belongs to.
	 * 
	 * @return vm name of the package
	 */
	public String getPackagename() {
		final int pos = name.lastIndexOf('/');
		return pos == -1 ? "" : name.substring(0, pos);
	}

	/**
	 * Returns the optional bundle identifier for this class.
	 * 
	 * @return bundle or <code>null</code>
	 */
	public String getBundle() {
		return bundle;
	}

}
