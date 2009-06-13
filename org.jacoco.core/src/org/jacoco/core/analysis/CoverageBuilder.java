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
 * Builder for hierarchical {@link ICoverageDataNode} structures based on
 * execution and structure information. The builder is constructed for a given
 * {@link ExecutionDataStore} and then feed with class structure information
 * through its {@link IStructureVisitor} interface.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageBuilder implements IStructureVisitor {

	private final ExecutionDataStore executionData;

	private final Map<Long, ClassNode> classes;

	private final Map<String, SourceFileNode> sourcefiles;

	/**
	 * Create a new builder based on the given execution data.
	 * 
	 * @param executionData
	 *            execution data
	 */
	public CoverageBuilder(final ExecutionDataStore executionData) {
		this.executionData = executionData;
		this.classes = new HashMap<Long, ClassNode>();
		this.sourcefiles = new HashMap<String, SourceFileNode>();
	}

	/**
	 * Returns all class nodes currently contained in this builder.
	 * 
	 * @return all class nodes
	 */
	public Collection<ClassNode> getClasses() {
		return Collections.unmodifiableCollection(classes.values());
	}

	/**
	 * Returns all source file nodes currently contained in this builder.
	 * 
	 * @return all source file nodes
	 */
	public Collection<SourceFileNode> getSourceFiles() {
		return Collections.unmodifiableCollection(sourcefiles.values());
	}

	public IClassStructureVisitor visitClassStructure(final long id,
			final String name) {
		final boolean[][] covered = executionData.getBlockdata(id);
		final Collection<ICoverageDataNode> methods = new ArrayList<ICoverageDataNode>();
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
				final ClassNode classData = new ClassNode(name, methods);
				classes.put(Long.valueOf(id), classData);
				if (sourcename[0] != null) {
					final SourceFileNode sourceFile = getSourceFile(
							sourcename[0], classData.getPackagename());
					sourceFile.add(classData);
				}
			}
		};
	}

	private IMethodStructureVisitor createMethodVisitor(final String name,
			final String desc, final String signature,
			final Collection<ICoverageDataNode> container,
			final boolean[] covered) {
		final MethodNode method = new MethodNode(name, desc, signature);
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

	private SourceFileNode getSourceFile(final String filename,
			final String packagename) {
		final String key = packagename + '/' + filename;
		SourceFileNode sourcefile = sourcefiles.get(key);
		if (sourcefile == null) {
			sourcefile = new SourceFileNode(filename, packagename);
			sourcefiles.put(key, sourcefile);
		}
		return sourcefile;
	}

	public void visitEnd() {
	}

}
