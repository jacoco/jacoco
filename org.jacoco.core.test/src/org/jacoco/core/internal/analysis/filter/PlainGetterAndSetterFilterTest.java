/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Daniel Kraft - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.Set;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link PrivateEmptyNoArgConstructorFilter}.
 */
public class PlainGetterAndSetterFilterTest extends FilterTestBase {

	private final IFilter filter = new PlainGetterAndSetterFilter();

	@Test
	public void ignoreGetter() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "getName", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Foo", "name", "java/lang/String");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void ignoreSetter() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "setName", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Foo", "name", "java/lang/String");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
