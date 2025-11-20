/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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

import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link SyntheticClassFilter}.
 */
public class SyntheticClassFilterTest extends FilterTestBase {

	private final SyntheticClassFilter filter = new SyntheticClassFilter();

	@Test
	public void should_filter() {
		context.classAccess = Opcodes.ACC_SYNTHETIC;
		final MethodNode m = new MethodNode();
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter() {
		final MethodNode m = new MethodNode();
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

}
