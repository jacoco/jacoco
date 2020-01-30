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
	public void should_ignore_getter() {

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
	public void should_ignore_getter_of_boolean_field() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");
		fields.add("valid");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "isValid", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Foo", "valid", "java/lang/Boolean");
		m.visitInsn(Opcodes.ARETURN);
		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_ignore_empty_getter() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "getName", "()V", null, null);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_ignore_getter_of_unknown_field_1() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "getName", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Foo", "name", "java/lang/String");
		m.visitInsn(Opcodes.ARETURN);
		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_ignore_getter_of_unknown_field_2() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "isValid", "()V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Foo", "valid", "java/lang/Boolean");
		m.visitInsn(Opcodes.ARETURN);
		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_ignore_invalid_getter_1() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "getName", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Foo", "name", "java/lang/String");
		m.visitInsn(Opcodes.RETURN);
		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_ignore_invalid_getter_2() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "getName", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Foo", "name", "java/lang/String");
		m.visitInsn(Opcodes.LOOKUPSWITCH);
		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_ignore_invalid_getter_3() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");
		fields.add("valid");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "isValid", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_ignore_method_startwith_get() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "get", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Foo", "valid", "java/lang/Boolean");
		m.visitInsn(Opcodes.ARETURN);
		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_ignore_method_startwith_is() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "is", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Foo", "valid", "java/lang/Boolean");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_ignore_setter() {

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

	@Test
	public void should_not_ignore_setter_of_unknown_field() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "setName", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Foo", "name", "java/lang/String");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_ignore_invalid_setter_1() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "setName", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.LDC, 1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Foo", "name", "java/lang/String");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_ignore_invalid_setter_2() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "setName", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.IALOAD, 1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "Foo", "name", "java/lang/String");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_ignore_invalid_setter_3() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "setName", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_ignore_method_startwith_set() {

		Set<String> fields = context.getClassFields();
		fields.add("desc");
		fields.add("name");
		fields.add("id");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "set", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Foo", "name", "java/lang/String");
		m.visitInsn(Opcodes.ARETURN);
		filter.filter(m, context, output);

		assertIgnored();
	}

}
