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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

final class MethodSnapshotVisitor extends MethodVisitor {

	private final LinkedHashMap<Label, Integer> lineNumbers = new LinkedHashMap<Label, Integer>();

	MethodSnapshotVisitor(final MethodVisitor methodVisitor) {
		super(InstrSupport.ASM_API_VERSION, methodVisitor);
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
