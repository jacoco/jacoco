/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;

import org.jacoco.core.instr.MethodRecorder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypeReference;

/**
 * Unit tests for {@link ProbeInserter}.
 */
public class ProbeInserterTest {

	private MethodRecorder actual, expected;

	private MethodVisitor actualVisitor, expectedVisitor;

	private IProbeArrayStrategy arrayStrategy;

	@Before
	public void setup() {
		actual = new MethodRecorder();
		actualVisitor = actual.getVisitor();
		expected = new MethodRecorder();
		expectedVisitor = expected.getVisitor();
		arrayStrategy = new IProbeArrayStrategy() {
			public int storeInstance(MethodVisitor mv, boolean clinit,
					int variable) {
				mv.visitLdcInsn(clinit ? "clinit" : "init");
				return 5;
			}

			public void addMembers(ClassVisitor delegate, int probeCount) {
			}
		};
	}

	@After
	public void verify() {
		assertEquals(expected, actual);
	}

	@Test
	public void probevar_should_be_at_position_0_for_static_method_without_parameters() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "()V",
				actualVisitor, arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 0);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void probevar_should_be_at_position_1_for_instance_method_without_parameters() {
		ProbeInserter pi = new ProbeInserter(0, "m", "()V", actualVisitor,
				arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 1);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void probevar_should_be_at_position_4_for_instance_method_with_3_parameters() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(IZLjava/lang/Object;)V",
				actualVisitor, arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 4);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void probevar_should_be_at_position_5_for_instance_method_with_2_wide_parameters() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(JD)V", actualVisitor,
				arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 5);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void visitCode_should_call_IProbeArrayStrategy_for_any_methods() {
		ProbeInserter pi = new ProbeInserter(0, "m", "()V", actualVisitor,
				arrayStrategy);
		pi.visitCode();

		expectedVisitor.visitLabel(new Label());
		expectedVisitor.visitLdcInsn("init");
	}

	@Test
	public void visitCode_should_call_IProbeArrayStrategy_for_static_initializers() {
		ProbeInserter pi = new ProbeInserter(0, "<clinit>", "()V",
				actualVisitor, arrayStrategy);
		pi.visitCode();

		expectedVisitor.visitLabel(new Label());
		expectedVisitor.visitLdcInsn("clinit");
	}

	@Test
	public void visitVarInsn_should_be_called_with_adjusted_variable_positions() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(II)V", actualVisitor,
				arrayStrategy);

		pi.visitVarInsn(Opcodes.ALOAD, 0);
		pi.visitVarInsn(Opcodes.ILOAD, 1);
		pi.visitVarInsn(Opcodes.ILOAD, 2);
		pi.visitVarInsn(Opcodes.ISTORE, 3);
		pi.visitVarInsn(Opcodes.FSTORE, 4);

		// Argument variables stay at the same position:
		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 0);
		expectedVisitor.visitVarInsn(Opcodes.ILOAD, 1);
		expectedVisitor.visitVarInsn(Opcodes.ILOAD, 2);

		// Local variables are shifted by one:
		expectedVisitor.visitVarInsn(Opcodes.ISTORE, 4);
		expectedVisitor.visitVarInsn(Opcodes.FSTORE, 5);
	}

	@Test
	public void visitIincInsn_should_be_called_with_adjusted_variable_positions() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(II)V", actualVisitor,
				arrayStrategy);
		pi.visitIincInsn(0, 100);
		pi.visitIincInsn(1, 101);
		pi.visitIincInsn(2, 102);
		pi.visitIincInsn(3, 103);
		pi.visitIincInsn(4, 104);

		// Argument variables stay at the same position:
		expectedVisitor.visitIincInsn(0, 100);
		expectedVisitor.visitIincInsn(1, 101);
		expectedVisitor.visitIincInsn(2, 102);

		// Local variables are shifted by one:
		expectedVisitor.visitIincInsn(4, 103);
		expectedVisitor.visitIincInsn(5, 104);
	}

	@Test
	public void visitLocalVariable_should_be_called_with_adjusted_variable_positions() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(II)V", actualVisitor,
				arrayStrategy);

		pi.visitLocalVariable(null, null, null, null, null, 0);
		pi.visitLocalVariable(null, null, null, null, null, 1);
		pi.visitLocalVariable(null, null, null, null, null, 2);
		pi.visitLocalVariable(null, null, null, null, null, 3);
		pi.visitLocalVariable(null, null, null, null, null, 4);

		Label begin = new Label();

		// Argument variables stay at the same position:
		expectedVisitor.visitLocalVariable(null, null, null, begin, null, 0);
		expectedVisitor.visitLocalVariable(null, null, null, begin, null, 1);
		expectedVisitor.visitLocalVariable(null, null, null, begin, null, 2);

		// Local variables are shifted by one:
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 4);
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 5);
	}

	@Test
	public void should_remap_LocalVariableAnnotation() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(I)V", actualVisitor,
				arrayStrategy);

		final Label start = new Label();
		pi.visitLabel(start);
		final Label end = new Label();
		pi.visitLabel(end);

		pi.visitLocalVariableAnnotation(TypeReference.LOCAL_VARIABLE, null,
				new Label[] { start }, new Label[] { end }, new int[] { 2 },
				"LNonNull;", false);

		expectedVisitor.visitLabel(start);
		expectedVisitor.visitLabel(end);
		// Local variables are shifted by one:
		expectedVisitor.visitLocalVariableAnnotation(
				TypeReference.LOCAL_VARIABLE, null, new Label[] { start },
				new Label[] { end }, new int[] { 3 }, "LNonNull;", false);
	}

	@Test
	public void new_stack_size_should_be_big_enought_to_store_probe_array() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(II)V", actualVisitor,
				arrayStrategy);
		pi.visitCode();
		pi.visitMaxs(0, 8);

		expectedVisitor.visitLabel(new Label());
		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitMaxs(5, 9);
	}

	@Test
	public void new_stack_size_should_be_increased_for_probes() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(II)V", actualVisitor,
				arrayStrategy);
		pi.visitCode();
		pi.visitMaxs(10, 8);

		expectedVisitor.visitLabel(new Label());
		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitMaxs(13, 9);
	}

	@Test
	public void visitFrame_should_insert_probe_variable_between_arguments_and_local_variables() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(J)V", actualVisitor,
				arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 3,
				new Object[] { "Foo", Opcodes.LONG, "java/lang/String" }, 0,
				new Object[0]);

		expectedVisitor.visitFrame(Opcodes.F_NEW, 4,
				new Object[] { "Foo", Opcodes.LONG, "[Z", "java/lang/String" },
				0, new Object[0]);
	}

	@Test
	public void visitFrame_should_only_insert_probe_variable_when_no_other_local_variables_exist() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "()V",
				actualVisitor, arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[0]);

		expectedVisitor.visitFrame(Opcodes.F_NEW, 1, new Object[] { "[Z" }, 0,
				new Object[0]);
	}

	@Test
	public void visitFrame_should_insert_probe_variable_first_when_no_parameters_exist() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "()V",
				actualVisitor, arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 2, new Object[] { Opcodes.DOUBLE, "Foo" },
				0, new Object[0]);

		expectedVisitor.visitFrame(Opcodes.F_NEW, 3,
				new Object[] { "[Z", Opcodes.DOUBLE, "Foo" }, 0, new Object[0]);
	}

	@Test
	public void visitFrame_should_fill_one_unused_slots_before_probe_variable_with_TOP() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "(I)V",
				actualVisitor, arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});

		// The locals in this frame are filled with TOP up to the probe variable
		expectedVisitor.visitFrame(Opcodes.F_NEW, 2,
				new Object[] { Opcodes.TOP, "[Z", }, 0, new Object[] {});
	}

	@Test
	public void visitFrame_should_fill_two_unused_slots_before_probe_variable_with_TOP_TOP() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "(J)V",
				actualVisitor, arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});

		// The locals in this frame are filled with TOP up to the probe variable
		expectedVisitor.visitFrame(Opcodes.F_NEW, 3,
				new Object[] { Opcodes.TOP, Opcodes.TOP, "[Z", }, 0,
				new Object[] {});
	}

	@Test
	public void visitFrame_should_fill_three_unused_slots_before_probe_variable_with_TOP_TOP_TOP() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "(DIJ)V",
				actualVisitor, arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 1, new Object[] { Opcodes.DOUBLE }, 0,
				new Object[] {});

		// The locals in this frame are filled with TOP up to the probe variable
		expectedVisitor
				.visitFrame(
						Opcodes.F_NEW, 5, new Object[] { Opcodes.DOUBLE,
								Opcodes.TOP, Opcodes.TOP, Opcodes.TOP, "[Z", },
						0, new Object[] {});
	}

	@Test(expected = IllegalArgumentException.class)
	public void visitFrame_must_only_support_resolved_frames() {
		ProbeInserter pi = new ProbeInserter(0, "m", "()V", actualVisitor,
				arrayStrategy);
		pi.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	}

}
