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

/**
 * Coverage data of a single source file. As source file contains one or more
 * classes.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class SourceFileCoverageData extends CoverageDataImpl {

	private final String bundle;

	private final String packagename;

	private final String filename;

	/**
	 * Creates a source file data object with the given parameters.
	 * 
	 * @param filename
	 *            name of the source file
	 * @param packagename
	 *            vm name of the package the source file belongs to
	 * @param bundle
	 *            optional bundle name
	 */
	public SourceFileCoverageData(final String filename,
			final String packagename, final String bundle) {
		super(ElementType.SOURCEFILE, true);
		this.bundle = bundle;
		this.packagename = packagename;
		this.filename = filename;
	}

	/**
	 * Returns the source file name.
	 * 
	 * @return source file name
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns the vm name of the package the source file belongs to.
	 * 
	 * @return package name
	 */
	public String getPackagename() {
		return packagename;
	}

	/**
	 * Returns an optional bundle identifier.
	 * 
	 * @return bundle identifier or <code>null</code>
	 */
	public String getBundle() {
		return bundle;
	}

}
