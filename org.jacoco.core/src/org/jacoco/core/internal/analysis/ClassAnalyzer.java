/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.core.internal.flow.IClassProbesVisitor;
import org.jacoco.core.internal.flow.IMethodProbesVisitor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Analyzes the structure of a class.
 */
public class ClassAnalyzer implements IClassProbesVisitor {

	private final long classid;
	private final boolean executionData[];
	private final StringPool stringPool;

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
	 */
	public ClassAnalyzer(final long classid, final boolean[] executionData,
			final StringPool stringPool) {
		this.classid = classid;
		this.executionData = executionData;
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

	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		this.coverage = new ClassCoverageImpl(stringPool.get(name), classid,
				stringPool.get(signature), stringPool.get(superName),
				stringPool.get(interfaces));
	}

	public void visitSource(final String source, final String debug) {
		this.coverage.setSourceFileName(stringPool.get(source));
	}

	public IMethodProbesVisitor visitMethod(final int access,
			final String name, final String desc, final String signature,
			final String[] exceptions) {

		// TODO: Use filter hook
		if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
			return null;
		}

		return new MethodAnalyzer(stringPool.get(name), stringPool.get(desc),
				stringPool.get(signature), executionData) {
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

	// Nothing to do here:

	public void visitTotalProbeCount(final int count) {
	}

	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		return null;
	}

	public void visitAttribute(final Attribute attr) {
	}

	public FieldVisitor visitField(final int access, final String name,
			final String desc, final String signature, final Object value) {
		return null;
	}

	public void visitInnerClass(final String name, final String outerName,
			final String innerName, final int access) {
	}

	public void visitOuterClass(final String owner, final String name,
			final String desc) {
	}

	public void visitEnd() {
	}

}
