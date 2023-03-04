/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link PrivateEmptyNoArgConstructorFilter}.
 */
public class PrivateEmptyNoArgConstructorFilterTest extends FilterTestBase {

	private final IFilter filter = new PrivateEmptyNoArgConstructorFilter();

	@Test
	public void test() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
