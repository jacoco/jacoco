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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.data.ExecutionData;
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
 * @version $qualified.bundle.version$
 */
public class CoverageBuilder implements IStructureVisitor {

	private final ExecutionDataStore executionData;

	private final StringPool stringPool;

	private final Map<String, ClassCoverage> classes;

	private final Map<String, SourceFileCoverage> sourcefiles;

	/**
	 * Create a new builder based on the given execution data.
	 * 
	 * @param executionData
	 *            execution data
	 */
	public CoverageBuilder(final ExecutionDataStore executionData) {
		this(executionData, new StringPool());
	}

	/**
	 * Create a new builder based on the given execution data.
	 * 
	 * @param executionData
	 *            execution data
	 * @param stringPool
	 *            pool to optimize the number of {@link String} instances
	 */
	public CoverageBuilder(final ExecutionDataStore executionData,
			final StringPool stringPool) {
		this.executionData = executionData;
		this.stringPool = stringPool;
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
		return new BundleCoverage(name, classes.values(), sourcefiles.values(),
				stringPool);
	}

	// === IStructureVisitor ===

	public IClassStructureVisitor visitClassStructure(final long id) {
		final ExecutionData data = executionData.get(id);
		final boolean[] covered = data == null ? null : data.getData();
		final Collection<MethodCoverage> methods = new ArrayList<MethodCoverage>();
		return new IClassStructureVisitor() {
			String name;
			String signature;
			String superName;
			String[] interfaces;
			String sourcename;

			public void visit(final String name, final String signature,
					final String superName, final String[] interfaces) {
				this.name = stringPool.get(name);
				this.signature = stringPool.get(signature);
				this.superName = stringPool.get(superName);
				this.interfaces = stringPool.get(interfaces);
			}

			public void visitSourceFile(final String name) {
				sourcename = stringPool.get(name);
			}

			public IMethodStructureVisitor visitMethodStructure(
					final String name, final String desc, final String signature) {
				return createMethodVisitor(name, desc, signature, methods,
						covered);
			}

			public void visitEnd() {
				final ClassCoverage classData = new ClassCoverage(name, id,
						signature, superName, interfaces, sourcename, methods);
				// Only consider classes that actually contain code:
				if (classData.getInstructionCounter().getTotalCount() > 0) {
					if (classes.put(name, classData) != null) {
						throw new IllegalStateException(
								"Duplicate class name in same group: " + name);
					}
					if (sourcename != null) {
						final String packageName = stringPool.get(classData
								.getPackageName());
						final SourceFileCoverage sourceFile = getSourceFile(
								sourcename, packageName);
						sourceFile.increment(classData);
					}
				}
			}
		};
	}

	private IMethodStructureVisitor createMethodVisitor(final String name,
			final String desc, final String signature,
			final Collection<MethodCoverage> container, final boolean[] covered) {
		final MethodCoverage method = new MethodCoverage(stringPool.get(name),
				stringPool.get(desc), stringPool.get(signature));
		return new IMethodStructureVisitor() {
			public void block(final int id, final int instructions,
					final int[] lineNumbers) {
				final boolean c = covered == null ? false : covered[id];
				method.addBlock(instructions, lineNumbers, c);
			}

			public void visitEnd() {
				// Only consider methods that actually contain code:
				if (method.getInstructionCounter().getTotalCount() > 0) {
					container.add(method);
				}
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
