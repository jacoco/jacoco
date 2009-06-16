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
package org.jacoco.core.analysis;

import java.util.Collection;

/**
 * Coverage data of a single class.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassNode extends CoverageDataNodeImpl {

	/**
	 * Creates a class coverage data object with the given parameters.
	 * 
	 * @param name
	 *            vm name of the class
	 * @param methods
	 *            contained methods
	 */
	public ClassNode(final String name,
			final Collection<ICoverageDataNode> methods) {
		super(ElementType.CLASS, name, true);
		addNodes(methods);
		// As class is considered as covered when at least one method is
		// covered:
		final boolean covered = methodCounter.getCoveredCount() > 0;
		this.classCounter = CounterImpl.getInstance(covered);
	}

	/**
	 * Returns the vm name of the package this class belongs to.
	 * 
	 * @return vm name of the package
	 */
	public String getPackagename() {
		final int pos = getName().lastIndexOf('/');
		return pos == -1 ? "" : getName().substring(0, pos);
	}

}
