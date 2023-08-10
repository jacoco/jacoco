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
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link RecordsFilter}.
 */
public class RecordsFilterTest extends FilterTestBase {

	private final RecordsFilter filter = new RecordsFilter();

	@Test
	public void should_filter_generated_toString_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"toString", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInvokeDynamicInsn("toString", "(LPoint;)Ljava/lang/String;",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/ObjectMethods", "bootstrap",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;",
						false));
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_custom_toString_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"toString", "()Ljava/lang/String;", null, null);
		m.visitLdcInsn("");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_non_toString_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"toString", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_filter_generated_hashCode_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInvokeDynamicInsn("hashCode", "(LPoint;)I", new Handle(
				Opcodes.H_INVOKESTATIC, "java/lang/runtime/ObjectMethods",
				"bootstrap",
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;",
				false));
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_custom_hashCode_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_non_hashCode_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_filter_generated_equals_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"equals", "(Ljava/lang/Object;)Z", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInvokeDynamicInsn("equals", "(LPoint;Ljava/lang/Object;)Z",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/ObjectMethods", "bootstrap",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;",
						false));
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_custom_equals_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"equals", "(Ljava/lang/Object;)Z", null, null);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_non_equals_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"equals", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_non_records() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"toString", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInvokeDynamicInsn("toString", "(LPoint;)Ljava/lang/String;",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/ObjectMethods", "bootstrap",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;",
						false));
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_filter_field_int() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Z", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Dunno", "foo", "Z");
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_field_object() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Dunno", "foo", "Ljava/lang/String");
		m.visitInsn(Opcodes.LRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_redirect_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		// The method name is different from the field name
		m.visitFieldInsn(Opcodes.GETFIELD, "Dunno", "bar", "Ljava/lang/String");
		m.visitInsn(Opcodes.LRETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_noreturn_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Dunno", "foo", "Ljava/lang/String");

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_other_method() {
		context.superClassName = "java/lang/Record";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Dunno", "foo", "Ljava/lang/String");
		// Not a return statement
		m.visitFieldInsn(Opcodes.GETFIELD, "Dunno", "foo", "Ljava/lang/String");

		filter.filter(m, context, output);

		assertIgnored();
	}
}
