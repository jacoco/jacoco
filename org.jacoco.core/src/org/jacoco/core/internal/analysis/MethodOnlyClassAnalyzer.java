/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Simplified analyzer for method-only coverage mode. This analyzer only tracks
 * whether methods were executed, without analyzing control flow, lines, or
 * branches.
 */
public class MethodOnlyClassAnalyzer extends ClassVisitor {

	private final ClassCoverageImpl coverage;
	private final boolean[] probes;
	private final StringPool stringPool;
	private int methodProbeIndex = 0;

	/**
	 * Creates a new analyzer for method-only coverage.
	 *
	 * @param coverage
	 *            coverage node for the analyzed class data
	 * @param probes
	 *            execution data for this class or <code>null</code>
	 * @param stringPool
	 *            shared pool to minimize the number of {@link String} instances
	 */
	public MethodOnlyClassAnalyzer(final ClassCoverageImpl coverage,
			final boolean[] probes, final StringPool stringPool) {
		super(InstrSupport.ASM_API_VERSION);
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
		return null;
	}

	@Override
	public void visitSource(final String source, final String debug) {
		coverage.setSourceFileName(stringPool.get(source));
	}

	@Override
	public FieldVisitor visitField(final int access, final String name,
			final String desc, final String signature, final Object value) {
		InstrSupport.assertNotInstrumented(name, coverage.getName());
		return null;
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions) {

		InstrSupport.assertNotInstrumented(name, coverage.getName());

		// Skip abstract and native methods
		if ((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
			return null;
		}

		// Create method coverage with EMPTY counters for lines and branches
		final MethodCoverageImpl methodCoverage = new MethodCoverageImpl(
				stringPool.get(name), stringPool.get(desc),
				stringPool.get(signature));

		// Determine if method was executed based on probe
		final boolean covered = probes != null
				&& methodProbeIndex < probes.length && probes[methodProbeIndex];

		// Set instruction counter based on whether the method was covered.
		// The method counter is automatically derived from instruction counter.
		// Branch counter remains EMPTY (0/0).
		if (covered) {
			methodCoverage.increment(CounterImpl.COUNTER_0_1,
					CounterImpl.COUNTER_0_0, 0);
		} else {
			methodCoverage.increment(CounterImpl.COUNTER_1_0,
					CounterImpl.COUNTER_0_0, 0);
		}

		// Call incrementMethodCounter to set the method counter based on
		// instructions
		methodCoverage.incrementMethodCounter();

		coverage.addMethod(methodCoverage);
		methodProbeIndex++;

		return null;
	}
}
