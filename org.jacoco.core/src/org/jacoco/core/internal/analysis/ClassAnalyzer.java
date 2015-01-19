/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.internal.flow.ClassProbesVisitor;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Analyzes the structure of a class.
 */
public class ClassAnalyzer extends ClassProbesVisitor {

	private final long classid;
	private final boolean noMatch;
	private final boolean[] probes;
	private final StringPool stringPool;

	private ClassCoverageImpl coverage;

	/**
	 * Creates a new analyzer that builds coverage data for a class.
	 * 
	 * @param classid
	 *            id of the class
	 * @param noMatch
	 *            <code>true</code> if class id does not match with execution
	 *            data
	 * @param probes
	 *            execution data for this class or <code>null</code>
	 * @param stringPool
	 *            shared pool to minimize the number of {@link String} instances
	 */
	public ClassAnalyzer(final long classid, final boolean noMatch,
			final boolean[] probes, final StringPool stringPool) {
		this.classid = classid;
		this.noMatch = noMatch;
		this.probes = probes;
		this.stringPool = stringPool;
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
				noMatch, stringPool.get(signature), stringPool.get(superName),
				stringPool.get(interfaces));
	}

	@Override
	public void visitSource(final String source, final String debug) {
		this.coverage.setSourceFileName(stringPool.get(source));
	}

	@Override
	public MethodProbesVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {

		InstrSupport.assertNotInstrumented(name, coverage.getName());

		if (isMethodFiltered(access, name)) {
			return null;
		}

		return new MethodAnalyzer(stringPool.get(name), stringPool.get(desc),
				stringPool.get(signature), probes) {
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
	}

	// TODO: Use filter hook in future
	private boolean isMethodFiltered(final int access, final String name) {
		return (access & Opcodes.ACC_SYNTHETIC) != 0
				&& !name.startsWith("lambda$");
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

}
