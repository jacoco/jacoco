/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import java.util.HashSet;
import java.util.Set;

import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.internal.analysis.filter.Filters;
import org.jacoco.core.internal.analysis.filter.IFilterContext;
import org.jacoco.core.internal.flow.ClassProbesVisitor;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;

/**
 * Analyzes the structure of a class.
 */
public class ClassAnalyzer extends ClassProbesVisitor
		implements IFilterContext {

	private final ClassCoverageImpl coverage;
	private final boolean[] probes;
	private final StringPool stringPool;

	private final Set<String> classAnnotations = new HashSet<String>();

	/**
	 * Creates a new analyzer that builds coverage data for a class.
	 * 
	 * @param coverage
	 *            coverage node for the analyzed class data
	 * @param probes
	 *            execution data for this class or <code>null</code>
	 * @param stringPool
	 *            shared pool to minimize the number of {@link String} instances
	 */
	public ClassAnalyzer(final ClassCoverageImpl coverage,
			final boolean[] probes, final StringPool stringPool) {
		this.coverage = coverage;
		this.probes = probes;
		this.stringPool = stringPool;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		coverage.setSignature(stringPool.get(signature));
		coverage.setSuperName(stringPool.get(superName));
		coverage.setInterfaces(stringPool.get(interfaces));
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		classAnnotations.add(desc);
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public void visitSource(final String source, final String debug) {
		coverage.setSourceFileName(stringPool.get(source));
	}

	@Override
	public MethodProbesVisitor visitMethod(final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions) {

		InstrSupport.assertNotInstrumented(name, coverage.getName());

		return new MethodAnalyzer(stringPool.get(name), stringPool.get(desc),
				stringPool.get(signature), probes, Filters.ALL, this) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				final IMethodCoverage methodCoverage = getCoverage();
				if (methodCoverage.getInstructionCounter()
						.getTotalCount() > 0) {
					// Only consider methods that actually contain code
					coverage.addMethod(methodCoverage);
				}
			}
		};
	}

	@Override
	public FieldVisitor visitField(final int access, final String name,
			final String desc, final String signature, final Object value) {
		InstrSupport.assertNotInstrumented(name, coverage.getName());
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public void visitTotalProbeCount(final int count) {
		// nothing to do
	}

	// IFilterContext implementation

	public String getClassName() {
		return coverage.getName();
	}

	public String getSuperClassName() {
		return coverage.getSuperName();
	}

	public Set<String> getClassAnnotations() {
		return classAnnotations;
	}

	public String getSourceFileName() {
		return coverage.getSourceFileName();
	}

}
