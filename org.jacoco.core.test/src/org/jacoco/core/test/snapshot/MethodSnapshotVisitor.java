/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.snapshot;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

final class MethodSnapshotVisitor extends MethodVisitor {

	private final LinkedHashMap<Label, Integer> lineNumbers = new LinkedHashMap<Label, Integer>();

	MethodSnapshotVisitor(final MethodVisitor methodVisitor) {
		super(InstrSupport.ASM_API_VERSION, methodVisitor);
	}

	/**
	 * Ignores visit, because can not be parsed by {@link MethodSnapshotParser}.
	 */
	@Override
	public AnnotationVisitor visitTypeAnnotation(final int typeRef,
			final TypePath typePath, final String descriptor,
			final boolean visible) {
		return null;
	}

	/**
	 * Ignores visit, because can not be parsed by {@link MethodSnapshotParser}.
	 */
	@Override
	public void visitAnnotableParameterCount(final int parameterCount,
			final boolean visible) {
	}

	/**
	 * Ignores visit, because can not be parsed by {@link MethodSnapshotParser}.
	 */
	@Override
	public AnnotationVisitor visitParameterAnnotation(final int parameter,
			final String descriptor, final boolean visible) {
		return null;
	}

	@Override
	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
		if ("null".equals(type)) {
			// ambiguous when textified and won't be parsed as type name
			throw new UnsupportedOperationException();
		}
		super.visitTryCatchBlock(start, end, handler, type);
	}

	/**
	 * Ignores visit, because can not be parsed by {@link MethodSnapshotParser}.
	 */
	@Override
	public AnnotationVisitor visitTryCatchAnnotation(final int typeRef,
			final TypePath typePath, final String descriptor,
			final boolean visible) {
		return null;
	}

	/**
	 * Ignores visit, because can not be parsed by {@link MethodSnapshotParser}.
	 */
	@Override
	public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef,
			final TypePath typePath, final Label[] start, final Label[] end,
			final int[] index, final String descriptor, final boolean visible) {
		return null;
	}

	/**
	 * Delays visit of line numbers till {@link #visitMaxs(int, int)}.
	 */
	@Override
	public void visitLineNumber(final int line, final Label start) {
		lineNumbers.put(start, line);
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		int minLineNumber = Integer.MAX_VALUE;
		for (final int lineNumber : lineNumbers.values()) {
			minLineNumber = Math.min(minLineNumber, lineNumber);
		}
		for (final Map.Entry<Label, Integer> lineNumber : lineNumbers
				.entrySet()) {
			super.visitLineNumber(lineNumber.getValue() - minLineNumber + 5,
					lineNumber.getKey());
		}
		super.visitMaxs(maxStack, maxLocals);
	}

}
