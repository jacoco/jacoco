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


/**
 * Coverage data of a single source file. As source file contains one or more
 * classes.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class SourceFileCoverage extends SourceNodeImpl {

	private final String packagename;

	/**
	 * Creates a source file data object with the given parameters.
	 * 
	 * @param name
	 *            name of the source file
	 * @param packagename
	 *            vm name of the package the source file belongs to
	 */
	public SourceFileCoverage(final String name, final String packagename) {
		super(ElementType.SOURCEFILE, name);
		this.packagename = packagename;
	}

	/**
	 * Returns the vm name of the package the source file belongs to.
	 * 
	 * @return package name
	 */
	public String getPackageName() {
		return packagename;
	}

}
