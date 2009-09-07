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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IClassStructureVisitor;
import org.jacoco.core.data.IMethodStructureVisitor;
import org.jacoco.core.data.IStructureVisitor;

/**
 * Builder for hierarchical {@link ICoverageNode} structures based on execution
 * and structure information. The builder is constructed for a given
 * {@link ExecutionDataStore} and then feed with class structure information
 * through its {@link IStructureVisitor} interface. Afterwards the collected
 * data can be obtained with {@link #getClasses()}, {@link #getSourceFiles()} or
 * {@link #getBundle(String)}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageBuilder implements IStructureVisitor {

	private final ExecutionDataStore executionData;

	private final Map<Long, ClassCoverage> classes;

	private final Map<String, SourceFileCoverage> sourcefiles;

	/**
	 * Create a new builder based on the given execution data.
	 * 
	 * @param executionData
	 *            execution data
	 */
	public CoverageBuilder(final ExecutionDataStore executionData) {
		this.executionData = executionData;
		this.classes = new HashMap<Long, ClassCoverage>();
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

	public IClassStructureVisitor visitClassStructure(final long id,
			final String name) {
		final boolean[][] covered = executionData.get(id);
		final Collection<MethodCoverage> methods = new ArrayList<MethodCoverage>();
		final String[] sourcename = new String[1];
		return new IClassStructureVisitor() {
			public void visitSourceFile(final String name) {
				sourcename[0] = name;
			}

			public IMethodStructureVisitor visitMethodStructure(final int id,
					final String name, final String desc, final String signature) {
				final boolean[] c = covered == null ? null : covered[id];
				return createMethodVisitor(name, desc, signature, methods, c);
			}

			public void visitEnd() {
				final ClassCoverage classData = new ClassCoverage(name,
						sourcename[0], methods);
				classes.put(Long.valueOf(id), classData);
				if (sourcename[0] != null) {
					final SourceFileCoverage sourceFile = getSourceFile(
							sourcename[0], classData.getPackageName());
					sourceFile.increment(classData);
				}
			}
		};
	}

	private IMethodStructureVisitor createMethodVisitor(final String name,
			final String desc, final String signature,
			final Collection<MethodCoverage> container, final boolean[] covered) {
		final MethodCoverage method = new MethodCoverage(name, desc, signature);
		return new IMethodStructureVisitor() {
			public void block(final int id, final int instructions,
					final int[] lineNumbers) {
				final boolean c = covered == null ? false : covered[id];
				method.addBlock(instructions, lineNumbers, c);
			}

			public void visitEnd() {
				container.add(method);
			}
		};
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
