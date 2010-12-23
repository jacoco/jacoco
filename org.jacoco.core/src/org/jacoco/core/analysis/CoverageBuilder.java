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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.data.ExecutionDataStore;

/**
 * Builder for hierarchical {@link ICoverageNode} structures based on execution
 * and structure information. The builder is constructed for a given
 * {@link ExecutionDataStore} and then feed with class structure information
 * through its {@link ICoverageVisitor} interface. Afterwards the collected data
 * can be obtained with {@link #getClasses()}, {@link #getSourceFiles()} or
 * {@link #getBundle(String)}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class CoverageBuilder implements ICoverageVisitor {

	private final Map<String, ClassCoverage> classes;

	private final Map<String, SourceFileCoverage> sourcefiles;

	/**
	 * Create a new builder.
	 * 
	 */
	public CoverageBuilder() {
		this.classes = new HashMap<String, ClassCoverage>();
		this.sourcefiles = new HashMap<String, SourceFileCoverage>();
	}

	/**
	 * Returns all class nodes currently contained in this builder.
	 * 
	 * @return all class nodes
	 */
	public Collection<ClassCoverage> getClasses() {
		return Collections.unmodifiableCollection(classes.values());
	}

	/**
	 * Returns all source file nodes currently contained in this builder.
	 * 
	 * @return all source file nodes
	 */
	public Collection<SourceFileCoverage> getSourceFiles() {
		return Collections.unmodifiableCollection(sourcefiles.values());
	}

	/**
	 * Creates a bundle from all nodes currently contained in this bundle.
	 * 
	 * @param name
	 *            Name of the bundle
	 * @return bundle containing all classes and source files
	 */
	public BundleCoverage getBundle(final String name) {
		return new BundleCoverage(name, classes.values(), sourcefiles.values());
	}

	// === IStructureVisitor ===

	public void visitCoverage(final ClassCoverage coverage) {
		// Only consider classes that actually contain code:
		if (coverage.getInstructionCounter().getTotalCount() > 0) {
			final String name = coverage.getName();
			if (classes.put(name, coverage) != null) {
				throw new IllegalStateException(
						"Duplicate class name in same group: " + name);
			}
			final String source = coverage.getSourceFileName();
			if (source != null) {
				final SourceFileCoverage sourceFile = getSourceFile(source,
						coverage.getPackageName());
				sourceFile.increment(coverage);
			}
		}
	}

	private SourceFileCoverage getSourceFile(final String filename,
			final String packagename) {
		final String key = packagename + '/' + filename;
		SourceFileCoverage sourcefile = sourcefiles.get(key);
		if (sourcefile == null) {
			sourcefile = new SourceFileCoverage(filename, packagename);
			sourcefiles.put(key, sourcefile);
		}
		return sourcefile;
	}

}
