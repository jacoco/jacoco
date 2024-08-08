/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.internal.analysis.filter.Filters;
import org.jacoco.core.internal.analysis.filter.IFilter;
import org.jacoco.core.internal.analysis.filter.IFilterContext;
import org.jacoco.core.internal.analysis.filter.KotlinGeneratedFilter;
import org.jacoco.core.internal.analysis.filter.KotlinSMAP;
import org.jacoco.core.internal.flow.ClassProbesVisitor;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Analyzes the structure of a class.
 */
public class ClassAnalyzer extends ClassProbesVisitor
		implements IFilterContext {

	private final ClassCoverageImpl coverage;
	private final boolean[] probes;
	private final StringPool stringPool;

	private final Set<String> classAnnotations = new HashSet<String>();

	private final Set<String> classAttributes = new HashSet<String>();

	private String sourceDebugExtension;
	private KotlinSMAP smap;
	private final HashMap<String, SourceNodeImpl> fragments = new HashMap<String, SourceNodeImpl>();

	private final IFilter filter;

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
		this.filter = Filters.all();
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
	public void visitAttribute(final Attribute attribute) {
		classAttributes.add(attribute.type);
	}

	@Override
	public void visitSource(final String source, final String debug) {
		coverage.setSourceFileName(stringPool.get(source));
		sourceDebugExtension = debug;
	}

	@Override
	public MethodProbesVisitor visitMethod(final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions) {

		InstrSupport.assertNotInstrumented(name, coverage.getName());

		final InstructionsBuilder builder = new InstructionsBuilder(probes);

		return new MethodAnalyzer(builder) {

			@Override
			public void accept(final MethodNode methodNode,
					final MethodVisitor methodVisitor) {
				super.accept(methodNode, methodVisitor);
				addMethodCoverage(stringPool.get(name), stringPool.get(desc),
						stringPool.get(signature), builder, methodNode);
			}
		};
	}

	private void addMethodCoverage(final String name, final String desc,
			final String signature, final InstructionsBuilder icc,
			final MethodNode methodNode) {

		final Map<AbstractInsnNode, Instruction> instructions = icc
				.getInstructions();
		calculateFragments(instructions);

		final MethodCoverageCalculator mcc = new MethodCoverageCalculator(
				instructions);
		filter.filter(methodNode, this, mcc);

		final MethodCoverageImpl mc = new MethodCoverageImpl(name, desc,
				signature);
		mcc.calculate(mc);

		if (mc.containsCode()) {
			// Only consider methods that actually contain code
			coverage.addMethod(mc);
		}

	}

	private void calculateFragments(
			final Map<AbstractInsnNode, Instruction> instructions) {
		if (sourceDebugExtension == null
				|| !KotlinGeneratedFilter.isKotlinClass(this)) {
			return;
		}
		if (smap == null) {
			// Note that visitSource is invoked before visitAnnotation,
			// that's why parsing is done here
			smap = new KotlinSMAP(getSourceFileName(), sourceDebugExtension);
		}
		for (final KotlinSMAP.Mapping mapping : smap.mappings()) {
			if (coverage.getName().equals(mapping.inputClassName())
					&& mapping.inputStartLine() == mapping.outputStartLine()) {
				continue;
			}
			SourceNodeImpl fragment = fragments.get(mapping.inputClassName());
			if (fragment == null) {
				fragment = new SourceNodeImpl(null, mapping.inputClassName());
				fragments.put(mapping.inputClassName(), fragment);
			}
			final int mappingOutputEndLine = mapping.outputStartLine()
					+ mapping.repeatCount() - 1;
			for (Instruction instruction : instructions.values()) {
				if (mapping.outputStartLine() <= instruction.getLine()
						&& instruction.getLine() <= mappingOutputEndLine) {
					final int originalLine = mapping.inputStartLine()
							+ instruction.getLine() - mapping.outputStartLine();
					fragment.increment(instruction.getInstructionCounter(),
							CounterImpl.COUNTER_0_0, originalLine);
				}
			}
		}
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

	@Override
	public void visitEnd() {
		if (!fragments.isEmpty()) {
			coverage.setFragments(Arrays
					.asList(fragments.values().toArray(new SourceNodeImpl[0])));
		}
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

	public Set<String> getClassAttributes() {
		return classAttributes;
	}

	public String getSourceFileName() {
		return coverage.getSourceFileName();
	}

	public String getSourceDebugExtension() {
		return sourceDebugExtension;
	}

}
