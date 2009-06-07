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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for hierarchical {@link ICoverageData} structures based on execution
 * and structure information. The builder is constructed for a given
 * {@link ExecutionDataStore} and then feed with class structure information
 * through its {@link IStructureOutput} interface.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageDataBuilder implements IStructureOutput {

	private final ExecutionDataStore executionData;

	private final Map<Long, ClassCoverageData> classes;

	/**
	 * Create a new builder based on the given execution data.
	 * 
	 * @param executionData
	 *            execution data
	 */
	public CoverageDataBuilder(final ExecutionDataStore executionData) {
		this.executionData = executionData;
		this.classes = new HashMap<Long, ClassCoverageData>();
	}

	public IClassStructureOutput classStructure(final long id,
			final String name, final String bundle) {
		final boolean[][] covered = executionData.getBlockdata(id);
		final Collection<ICoverageData> methods = new ArrayList<ICoverageData>();
		final String[] sourcename = new String[1];
		return new IClassStructureOutput() {
			public void sourceFile(final String name) {
				sourcename[0] = name;
			}

			public IMethodStructureOutput methodStructure(final int id,
					final String name, final String desc, final String signature) {
				final boolean[] c = covered == null ? null : covered[id];
				return createMethodOutput(name, desc, signature, methods, c);
			}

			public void end() {
				final ClassCoverageData classData = new ClassCoverageData(name,
						bundle, methods);
				classes.put(Long.valueOf(id), classData);
				if (sourcename[0] != null) {
					final SourceFileCoverageData sourceFile = getSourceFile(
							sourcename[0], classData.getPackagename(), bundle);
					sourceFile.add(classData);
				}
			}
		};
	}

	private IMethodStructureOutput createMethodOutput(final String name,
			final String desc, final String signature,
			final Collection<ICoverageData> container, final boolean[] covered) {
		final Collection<ICoverageData> blocks = new ArrayList<ICoverageData>();
		return new IMethodStructureOutput() {
			public void block(final int id, final int instructionCount,
					final int[] lineNumbers) {
				final boolean c = covered == null ? false : covered[id];
				blocks.add(new BlockCoverageData(instructionCount, lineNumbers,
						c));
			}

			public void end() {
				container.add(new MethodCoverageData(name, desc, signature,
						blocks));
			}
		};
	}

	private SourceFileCoverageData getSourceFile(final String filename,
			final String packagename, final String bundle) {
		// TODO look for existing file.
		return new SourceFileCoverageData(filename, packagename, bundle);
	}

}
