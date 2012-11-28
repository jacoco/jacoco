/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    Martin Hare Robertson - filters
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.internal.analysis.filters.ICoverageFilterStatus.ICoverageFilter;
import org.jacoco.core.internal.flow.ClassProbesVisitor;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Analyzes the structure of a class.
 */
public class ClassAnalyzer extends ClassProbesVisitor {

	private final long classid;
	private final boolean executionData[];
	private final StringPool stringPool;
	private final ICoverageFilter coverageFilter;

	private ClassCoverageImpl coverage;

	/**
	 * Creates a new analyzer that builds coverage data for a class.
	 * 
	 * @param classid
	 *            id of the class
	 * @param executionData
	 *            execution data for this class or <code>null</code>
	 * @param stringPool
	 *            shared pool to minimize the number of {@link String} instances
	 * @param coverageFilter
	 *            filter which restricts the coverage data
	 */
	public ClassAnalyzer(final long classid, final boolean[] executionData,
			final StringPool stringPool, final ICoverageFilter coverageFilter) {
		this.classid = classid;
		this.executionData = executionData;
		this.stringPool = stringPool;
		this.coverageFilter = coverageFilter;
	}

	/**
	 * Returns the coverage data for this class after this visitor has been
	 * processed.
	 * 
	 * @return coverage data for this class
	 */
	public ClassCoverageImpl getCoverage() {
		return coverage;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		this.coverage = new ClassCoverageImpl(stringPool.get(name), classid,
				stringPool.get(signature), stringPool.get(superName),
				stringPool.get(interfaces));
	}

	@Override
	public void visitSource(final String source, final String debug) {
		this.coverage.setSourceFileName(stringPool.get(source));
	}

	@Override
	public MethodVisitor preVisitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		return coverageFilter.preVisitMethod(name, desc, null);
	}

	@Override
	public MethodProbesVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {

		// TODO: Use filter hook
		if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
			return null;
		}

		final MethodAnalyzer visitor = new MethodAnalyzer(stringPool.get(name),
				stringPool.get(desc), stringPool.get(signature), executionData,
				coverageFilter) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				final IMethodCoverage methodCoverage = getCoverage();
				if (methodCoverage.getInstructionCounter().getTotalCount() > 0) {
					// Only consider methods that actually contain code
					coverage.addMethod(methodCoverage);
				}
			}
		};

		return coverageFilter.visitMethod(name, desc, visitor);
	}

	@Override
	public void visitTotalProbeCount(final int count) {
		// nothing to do
	}
}
