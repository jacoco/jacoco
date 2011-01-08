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

import java.util.Collection;

/**
 * Coverage data of a Java package. The name of this data node is the package
 * name in VM notation (slash separated). The name of the default package is the
 * empty string.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class PackageCoverage extends CoverageNodeImpl {

	private final Collection<ClassCoverage> classes;

	private final Collection<SourceFileCoverage> sourceFiles;

	/**
	 * Creates package node instance for a package with the given name.
	 * 
	 * @param name
	 *            vm name of the package
	 * @param classes
	 *            collection of all classes in this package
	 * @param sourceFiles
	 *            collection of all source files in this package
	 */
	public PackageCoverage(final String name,
			final Collection<ClassCoverage> classes,
			final Collection<SourceFileCoverage> sourceFiles) {
		super(ElementType.PACKAGE, name);
		this.classes = classes;
		this.sourceFiles = sourceFiles;
		increment(sourceFiles);
		for (final ClassCoverage c : classes) {
			// We need to add only classes without a source file reference.
			// Classes associated with a source file are already included in the
			// SourceFileCoverage objects.
			if (c.getSourceFileName() == null) {
				increment(c);
			}
		}
	}

	/**
	 * Returns all classes contained in this package.
	 * 
	 * @return all classes
	 */
	public Collection<ClassCoverage> getClasses() {
		return classes;
	}

	/**
	 * Returns all source files in this package.
	 * 
	 * @return all source files
	 */
	public Collection<SourceFileCoverage> getSourceFiles() {
		return sourceFiles;
	}

}
