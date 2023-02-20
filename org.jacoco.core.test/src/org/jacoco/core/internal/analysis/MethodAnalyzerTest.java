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
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.internal.analysis.filter.FilterContextMock;
import org.jacoco.core.internal.analysis.filter.Filters;
import org.jacoco.core.internal.analysis.filter.IFilter;
import org.jacoco.core.internal.analysis.filter.IFilterContext;
import org.jacoco.core.internal.analysis.filter.IFilterOutput;
import org.jacoco.core.internal.flow.IProbeIdGenerator;
import org.jacoco.core.internal.flow.LabelFlowAnalyzer;
import org.jacoco.core.internal.flow.MethodProbesAdapter;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.util.CheckMethodAdapter;

/**
 * Unit tests for {@link MethodAnalyzer}.
 */
public class MethodAnalyzerTest implements IProbeIdGenerator {

	private int nextProbeId;

	private boolean[] probes;

	private MethodNode method;

	private IMethodCoverage result;

	@Before
	public void setup() {
		nextProbeId = 0;
		method = new MethodNode();
		method.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
		probes = new boolean[32];
	}

	public int nextId() {
		return nextProbeId++;
	}

	// === Scenario: linear Sequence with and without ignore filtering ===

	private void createLinearSequence() {
		final Label l0 = new Label();
		method.visitLabel(l0);
		method.visitLineNumber(1001, l0);
		method.visitInsn(Opcodes.NOP);
		method.visitInsn(Opcodes.NOP);
		final Label l1 = new Label();
		method.visitLabel(l1);
		method.visitLineNumber(1002, l1);
		method.visitInsn(Opcodes.RETURN);
	}

	@Test
	public void linear_instruction_sequence_should_create_1_probe() {
		createLinearSequence();
		runMethodAnalzer();
		assertEquals(1, nextProbeId);
	}

	@Test
	public void linear_instruction_sequence_should_show_missed_when_no_probe_is_executed() {
		createLinearSequence();
		runMethodAnalzer();

		assertLine(1001, 2, 0, 0, 0);
		assertLine(1002, 1, 0, 0, 0);
	}

	@Test
	public void linear_instruction_sequence_should_show_missed_when_probearray_is_null() {
		createLinearSequence();
		probes = null;
		runMethodAnalzer();

		assertLine(1001, 2, 0, 0, 0);
		assertLine(1002, 1, 0, 0, 0);
	}

	@Test
	public void linear_instruction_sequence_should_show_covered_when_probe_is_executed() {
		createLinearSequence();
		probes[0] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 0, 0);
		assertLine(1002, 0, 1, 0, 0);
	}

	/** Filters the NOP instructions as ignored */
	private static final IFilter NOP_FILTER = new IFilter() {
		public void filter(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			final AbstractInsnNode i1 = methodNode.instructions.get(2);
			final AbstractInsnNode i2 = methodNode.instructions.get(3);
			assertEquals(Opcodes.NOP, i1.getOpcode());
			assertEquals(Opcodes.NOP, i2.getOpcode());
			output.ignore(i1, i2);
		}
	};

	@Test
	public void linear_instruction_sequence_should_ignore_instructions_when_filter_is_applied() {
		createLinearSequence();
		probes[0] = true;
		runMethodAnalzer(NOP_FILTER);

		assertEquals(1002, result.getFirstLine());
		assertEquals(1002, result.getLastLine());

		assertLine(1001, 0, 0, 0, 0);
		assertLine(1002, 0, 1, 0, 0);
	}

	// === Scenario: simple if branch ===

	private void createIfBranch() {
		final Label l0 = new Label();
		method.visitLabel(l0);
		method.visitLineNumber(1001, l0);
		method.visitVarInsn(Opcodes.ILOAD, 1);
		Label l1 = new Label();
		method.visitJumpInsn(Opcodes.IFEQ, l1);
		final Label l2 = new Label();
		method.visitLabel(l2);
		method.visitLineNumber(1002, l2);
		method.visitLdcInsn("a");
		method.visitInsn(Opcodes.ARETURN);
		method.visitLabel(l1);
		method.visitLineNumber(1003, l1);
		method.visitLdcInsn("b");
		method.visitInsn(Opcodes.ARETURN);
	}

	@Test
	public void if_branch_should_create_2_probes() {
		createIfBranch();
		runMethodAnalzer();
		assertEquals(2, nextProbeId);
	}

	@Test
	public void if_branch_should_show_missed_when_no_probes_are_executed() {
		createIfBranch();
		runMethodAnalzer();

		assertLine(1001, 2, 0, 2, 0);
		assertLine(1002, 2, 0, 0, 0);
		assertLine(1003, 2, 0, 0, 0);
	}

	@Test
	public void if_branch_should_show_partial_branch_coverage_when_probe_for_first_branch_is_executed() {
		createIfBranch();
		probes[0] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 1, 1);
		assertLine(1002, 0, 2, 0, 0);
		assertLine(1003, 2, 0, 0, 0);
	}

	@Test
	public void if_branch_should_show_partial_branch_coverage_when_probe_for_second_branch_is_executed() {
		createIfBranch();
		probes[1] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 1, 1);
		assertLine(1002, 2, 0, 0, 0);
		assertLine(1003, 0, 2, 0, 0);
	}

	@Test
	public void if_branch_should_show_full_branch_coverage_when_probes_for_both_branches_are_executed() {
		createIfBranch();
		probes[0] = true;
		probes[1] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 0, 2);
		assertLine(1002, 0, 2, 0, 0);
		assertLine(1003, 0, 2, 0, 0);
	}

	// === Scenario: branch before unconditional probe ===

	/**
	 * Covers case of
	 * {@link MethodAnalyzer#visitJumpInsnWithProbe(int, Label, int, org.jacoco.core.internal.flow.IFrame)}.
	 */
	private void createIfBranchBeforeProbe() {
		final Label l0 = new Label();
		final Label l1 = new Label();
		final Label l2 = new Label();
		method.visitLabel(l0);
		method.visitLineNumber(1001, l0);
		method.visitVarInsn(Opcodes.ILOAD, 1);
		method.visitJumpInsn(Opcodes.IFNE, l1);
		method.visitLabel(l2);
		method.visitLineNumber(1002, l2);
		method.visitMethodInsn(Opcodes.INVOKESTATIC, "Foo", "foo", "()V",
				false);
		method.visitLabel(l1);
		method.visitLineNumber(1003, l1);
		method.visitInsn(Opcodes.RETURN);
	}

	@Test
	public void if_branch_before_probes_should_create_4_probes() {
		createIfBranchBeforeProbe();
		runMethodAnalzer();
		assertEquals(4, nextProbeId);
	}

	@Test
	public void if_branch_before_probes_should_show_missed_when_no_probes_are_executed() {
		createIfBranchBeforeProbe();
		runMethodAnalzer();

		assertLine(1001, 2, 0, 2, 0);
		assertLine(1002, 1, 0, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
	}

	@Test
	public void if_branch_before_probes_should_show_partial_branch_coverage_when_probe_for_first_branch_is_executed() {
		createIfBranchBeforeProbe();
		probes[0] = true;
		probes[3] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 1, 1);
		assertLine(1002, 1, 0, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
	}

	@Test
	public void if_branch_before_probes_should_show_partial_branch_coverage_when_probe_for_second_branch_is_executed() {
		createIfBranchBeforeProbe();
		probes[1] = true;
		probes[2] = true;
		probes[3] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 1, 1);
		assertLine(1002, 0, 1, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
	}

	@Test
	public void if_branch_before_probes_should_show_full_branch_coverage_when_probes_for_both_branches_are_executed() {
		createIfBranchBeforeProbe();
		probes[0] = true;
		probes[1] = true;
		probes[2] = true;
		probes[3] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 0, 2);
		assertLine(1002, 0, 1, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
	}

	// === Scenario: branch which merges back ===

	private void createIfBranchMerge() {
		final Label l0 = new Label();
		method.visitLabel(l0);
		method.visitLineNumber(1001, l0);
		method.visitVarInsn(Opcodes.ILOAD, 1);
		Label l1 = new Label();
		method.visitJumpInsn(Opcodes.IFEQ, l1);
		final Label l2 = new Label();
		method.visitLabel(l2);
		method.visitLineNumber(1002, l2);
		method.visitInsn(Opcodes.NOP);
		method.visitLabel(l1);
		method.visitLineNumber(1003, l1);
		method.visitInsn(Opcodes.RETURN);
	}

	@Test
	public void if_branch_merge_should_create_3_probes() {
		createIfBranchMerge();
		runMethodAnalzer();
		assertEquals(3, nextProbeId);
	}

	@Test
	public void if_branch_merge_should_show_missed_when_no_probes_are_executed() {
		createIfBranchMerge();
		runMethodAnalzer();

		assertLine(1001, 2, 0, 2, 0);
		assertLine(1002, 1, 0, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
	}

	@Test
	public void if_branch_merge_should_show_partial_branch_coverage_when_probe_for_first_branch_is_executed() {
		createIfBranchMerge();
		probes[0] = true;
		probes[2] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 1, 1);
		assertLine(1002, 1, 0, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
	}

	@Test
	public void if_branch_merge_should_show_partial_branch_coverage_when_probe_for_second_branch_is_executed() {
		createIfBranchMerge();
		probes[1] = true;
		probes[2] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 1, 1);
		assertLine(1002, 0, 1, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
	}

	@Test
	public void if_branch_merge_should_show_full_branch_coverage_when_probes_for_both_branches_are_executed() {
		createIfBranchMerge();
		probes[0] = true;
		probes[1] = true;
		probes[2] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 0, 2);
		assertLine(1002, 0, 1, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
	}

	// === Scenario: branch which jumps backwards ===

	private void createJumpBackwards() {
		final Label l0 = new Label();
		method.visitLabel(l0);
		method.visitLineNumber(1001, l0);
		final Label l1 = new Label();
		method.visitJumpInsn(Opcodes.GOTO, l1);
		final Label l2 = new Label();
		method.visitLabel(l2);
		method.visitLineNumber(1002, l2);
		method.visitInsn(Opcodes.RETURN);
		method.visitLabel(l1);
		method.visitLineNumber(1003, l1);
		method.visitJumpInsn(Opcodes.GOTO, l2);
	}

	@Test
	public void jump_backwards_should_create_1_probe() {
		createJumpBackwards();
		runMethodAnalzer();
		assertEquals(1, nextProbeId);
	}

	@Test
	public void jump_backwards_should_show_missed_when_no_probes_are_executed() {
		createJumpBackwards();
		runMethodAnalzer();

		assertLine(1001, 1, 0, 0, 0);
		assertLine(1002, 1, 0, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
	}

	@Test
	public void jump_backwards_should_show_covered_when_probe_is_executed() {
		createJumpBackwards();
		probes[0] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 1, 0, 0);
		assertLine(1002, 0, 1, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
	}

	// === Scenario: jump to first instruction ===

	private void createJumpToFirst() {
		final Label l1 = new Label();
		method.visitLabel(l1);
		method.visitLineNumber(1001, l1);
		method.visitVarInsn(Opcodes.ALOAD, 0);
		method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Foo", "test", "()Z",
				false);
		method.visitJumpInsn(Opcodes.IFEQ, l1);
		final Label l2 = new Label();
		method.visitLabel(l2);
		method.visitLineNumber(1002, l2);
		method.visitInsn(Opcodes.RETURN);
	}

	@Test
	public void jump_to_first_instruction_should_create_2_probes() {
		createJumpToFirst();
		runMethodAnalzer();
		assertEquals(2, nextProbeId);
	}

	@Test
	public void jump_to_first_instruction_should_show_missed_when_no_probes_are_executed() {
		createJumpToFirst();
		runMethodAnalzer();

		assertLine(1001, 3, 0, 2, 0);
		assertLine(1002, 1, 0, 0, 0);
	}

	@Test
	public void jump_to_first_instruction_should_show_partial_branch_coverage_when_probe_for_first_branch_is_executed() {
		createJumpToFirst();
		probes[0] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 3, 1, 1);
		assertLine(1002, 1, 0, 0, 0);
	}

	@Test
	public void jump_to_first_instruction_should_show_full_branch_coverage_when_probes_for_both_branches_are_executed() {
		createJumpToFirst();
		probes[0] = true;
		probes[1] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 3, 0, 2);
		assertLine(1002, 0, 1, 0, 0);
	}

	// === Scenario: table switch with and without replace filtering ===

	private void createTableSwitch() {
		final Label l0 = new Label();
		method.visitLabel(l0);
		method.visitLineNumber(1001, l0);
		method.visitVarInsn(Opcodes.ILOAD, 1);
		Label l1 = new Label();
		Label l2 = new Label();
		Label l3 = new Label();
		method.visitTableSwitchInsn(1, 3, l3, new Label[] { l1, l2, l1 });
		method.visitLabel(l1);
		method.visitLineNumber(1002, l1);
		method.visitIntInsn(Opcodes.BIPUSH, 11);
		method.visitVarInsn(Opcodes.ISTORE, 2);
		final Label l4 = new Label();
		method.visitLabel(l4);
		method.visitLineNumber(1003, l4);
		Label l5 = new Label();
		method.visitJumpInsn(Opcodes.GOTO, l5);
		method.visitLabel(l2);
		method.visitLineNumber(1004, l2);
		method.visitIntInsn(Opcodes.BIPUSH, 22);
		method.visitVarInsn(Opcodes.ISTORE, 2);
		final Label l6 = new Label();
		method.visitLabel(l6);
		method.visitLineNumber(1005, l6);
		method.visitJumpInsn(Opcodes.GOTO, l5);
		method.visitLabel(l3);
		method.visitLineNumber(1006, l3);
		method.visitInsn(Opcodes.ICONST_0);
		method.visitVarInsn(Opcodes.ISTORE, 2);
		method.visitLabel(l5);
		method.visitLineNumber(1007, l5);
		method.visitVarInsn(Opcodes.ILOAD, 2);
		method.visitInsn(Opcodes.IRETURN);
	}

	@Test
	public void table_switch_should_create_4_probes() {
		createTableSwitch();
		runMethodAnalzer();
		assertEquals(4, nextProbeId);
	}

	private static final IFilter SWITCH_FILTER = new IFilter() {
		public void filter(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			final AbstractInsnNode i = methodNode.instructions.get(3);
			assertEquals(Opcodes.TABLESWITCH, i.getOpcode());
			final AbstractInsnNode t1 = methodNode.instructions.get(6);
			assertEquals(Opcodes.BIPUSH, t1.getOpcode());
			final AbstractInsnNode t2 = methodNode.instructions.get(13);
			assertEquals(Opcodes.BIPUSH, t2.getOpcode());

			final Set<AbstractInsnNode> newTargets = new HashSet<AbstractInsnNode>();
			newTargets.add(t1);
			newTargets.add(t2);
			output.replaceBranches(i, newTargets);
		}
	};

	@Test
	public void table_switch_with_filter_should_show_2_branches_when_original_replaced() {
		createTableSwitch();
		runMethodAnalzer(SWITCH_FILTER);

		assertLine(1001, 2, 0, 2, 0);
	}

	@Test
	public void table_switch_with_filter_should_show_full_branch_coverage_when_new_targets_covered() {
		createTableSwitch();
		probes[0] = true;
		probes[1] = true;
		runMethodAnalzer(SWITCH_FILTER);

		assertLine(1001, 0, 2, 0, 2);
	}

	@Test
	public void table_switch_should_show_missed_when_no_probes_are_executed() {
		createTableSwitch();
		runMethodAnalzer();

		assertLine(1001, 2, 0, 3, 0);
		assertLine(1002, 2, 0, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
		assertLine(1004, 2, 0, 0, 0);
		assertLine(1005, 1, 0, 0, 0);
		assertLine(1006, 2, 0, 0, 0);
		assertLine(1007, 2, 0, 0, 0);
	}

	@Test
	public void table_switch_should_show_partial_branch_coverage_when_probes_for_first_branch_and_default_are_executed() {
		createTableSwitch();
		probes[0] = true;
		probes[3] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 2, 1);
		assertLine(1002, 0, 2, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
		assertLine(1004, 2, 0, 0, 0);
		assertLine(1005, 1, 0, 0, 0);
		assertLine(1006, 2, 0, 0, 0);
		assertLine(1007, 0, 2, 0, 0);
	}

	@Test
	public void table_switch_should_show_partial_branch_coverage_when_probes_for_third_branch_and_default_are_executed() {
		createTableSwitch();
		probes[2] = true;
		probes[3] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 2, 1);
		assertLine(1002, 2, 0, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
		assertLine(1004, 2, 0, 0, 0);
		assertLine(1005, 1, 0, 0, 0);
		assertLine(1006, 0, 2, 0, 0);
		assertLine(1007, 0, 2, 0, 0);
	}

	@Test
	public void table_switch_should_show_full_branch_coverage_when_all_probes_are_executed() {
		createTableSwitch();
		probes[0] = true;
		probes[1] = true;
		probes[2] = true;
		probes[3] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 0, 3);
		assertLine(1002, 0, 2, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
		assertLine(1004, 0, 2, 0, 0);
		assertLine(1005, 0, 1, 0, 0);
		assertLine(1006, 0, 2, 0, 0);
		assertLine(1007, 0, 2, 0, 0);
	}

	// === Scenario: table switch with merge ===

	private void createTableSwitchMerge() {
		final Label l0 = new Label();
		method.visitLabel(l0);
		method.visitLineNumber(1001, l0);
		method.visitInsn(Opcodes.ICONST_0);
		method.visitVarInsn(Opcodes.ISTORE, 2);
		final Label l1 = new Label();
		method.visitLabel(l1);
		method.visitLineNumber(1002, l1);
		method.visitVarInsn(Opcodes.ILOAD, 1);
		Label l2 = new Label();
		Label l3 = new Label();
		Label l4 = new Label();
		method.visitTableSwitchInsn(1, 3, l4, new Label[] { l2, l3, l2 });
		method.visitLabel(l2);
		method.visitLineNumber(1003, l2);
		method.visitIincInsn(2, 1);
		method.visitLabel(l3);
		method.visitLineNumber(1004, l3);
		method.visitIincInsn(2, 1);
		method.visitLabel(l4);
		method.visitLineNumber(1005, l4);
		method.visitVarInsn(Opcodes.ILOAD, 2);
		method.visitInsn(Opcodes.IRETURN);
	}

	@Test
	public void table_switch_with_merge_should_create_5_probes() {
		createTableSwitchMerge();
		runMethodAnalzer();
		assertEquals(5, nextProbeId);
	}

	@Test
	public void table_switch_with_merge_should_show_missed_when_no_probes_are_executed() {
		createTableSwitchMerge();
		runMethodAnalzer();

		assertLine(1001, 2, 0, 0, 0);
		assertLine(1002, 2, 0, 3, 0);
		assertLine(1003, 1, 0, 0, 0);
		assertLine(1004, 1, 0, 0, 0);
		assertLine(1005, 2, 0, 0, 0);
	}

	@Test
	public void table_switch_with_merge_should_show_two_missed_cases_when_probes_for_these_branches_are_not_executed() {
		createTableSwitchMerge();
		probes[0] = true;
		probes[4] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 0, 0);
		assertLine(1002, 0, 2, 2, 1);
		assertLine(1003, 1, 0, 0, 0);
		assertLine(1004, 1, 0, 0, 0);
		assertLine(1005, 0, 2, 0, 0);
	}

	@Test
	public void table_switch_with_merge_should_show_one_missed_case_when_probe_for_this_branch_is_not_executed() {
		createTableSwitchMerge();
		probes[1] = true;
		probes[3] = true;
		probes[4] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 0, 0);
		assertLine(1002, 0, 2, 2, 1);
		assertLine(1003, 1, 0, 0, 0);
		assertLine(1004, 0, 1, 0, 0);
		assertLine(1005, 0, 2, 0, 0);
	}

	@Test
	public void table_switch_with_merge_should_show_partial_branch_coverage_when_probe_for_one_branch_is_not_executed() {
		createTableSwitchMerge();
		probes[2] = true;
		probes[3] = true;
		probes[4] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 0, 0);
		assertLine(1002, 0, 2, 2, 1);
		assertLine(1003, 0, 1, 0, 0);
		assertLine(1004, 0, 1, 0, 0);
		assertLine(1005, 0, 2, 0, 0);
	}

	@Test
	public void table_switch_with_merge_should_show_full_branch_coverage_when_all_probes_are_executed() {
		createTableSwitchMerge();
		probes[0] = true;
		probes[1] = true;
		probes[2] = true;
		probes[3] = true;
		probes[4] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 0, 0);
		assertLine(1002, 0, 2, 0, 3);
		assertLine(1003, 0, 1, 0, 0);
		assertLine(1004, 0, 1, 0, 0);
		assertLine(1005, 0, 2, 0, 0);
	}

	// === Scenario: try/catch block ===

	private void createTryCatchBlock() {
		Label l1 = new Label();
		Label l2 = new Label();
		Label l3 = new Label();
		Label l4 = new Label();
		method.visitTryCatchBlock(l1, l2, l3, "java/lang/Exception");
		method.visitLabel(l1);
		method.visitLineNumber(1001, l1);
		method.visitVarInsn(Opcodes.ALOAD, 0);
		method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"printStackTrace", "()V", false);
		method.visitLabel(l2);
		method.visitJumpInsn(Opcodes.GOTO, l4);
		method.visitLabel(l3);
		method.visitLineNumber(1002, l3);
		method.visitVarInsn(Opcodes.ASTORE, 1);
		method.visitLabel(l4);
		method.visitLineNumber(1003, l4);
		method.visitInsn(Opcodes.RETURN);
	}

	@Test
	public void try_catch_should_create_3_probes() {
		createTryCatchBlock();
		runMethodAnalzer();
		assertEquals(3, nextProbeId);
	}

	@Test
	public void try_catch_should_show_missed_when_no_probes_are_executed() {
		createTryCatchBlock();
		runMethodAnalzer();

		assertLine(1001, 3, 0, 0, 0);
		assertLine(1002, 1, 0, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
	}

	@Test
	public void try_catch_should_show_exception_handler_missed_when_probe_is_not_executed() {
		createTryCatchBlock();
		probes[0] = true;
		probes[2] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 3, 0, 0);
		assertLine(1002, 1, 0, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
	}

	@Test
	public void try_catch_should_show_all_covered_when_all_probes_are_executed() {
		createTryCatchBlock();
		probes[0] = true;
		probes[1] = true;
		probes[2] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 3, 0, 0);
		assertLine(1002, 0, 1, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
	}

	// === Scenario: try/finally with and without merge filtering ===

	private void createTryFinally() {
		final Label l0 = new Label();
		final Label l1 = new Label();
		final Label l2 = new Label();
		final Label l3 = new Label();

		method.visitTryCatchBlock(l0, l2, l2, null);

		method.visitLabel(l0);
		method.visitLineNumber(1001, l0);

		method.visitJumpInsn(Opcodes.IFEQ, l1);
		// probe[0]
		method.visitInsn(Opcodes.RETURN);
		method.visitLabel(l1);
		// probe[1]
		method.visitInsn(Opcodes.RETURN);

		method.visitLabel(l2);
		method.visitJumpInsn(Opcodes.IFEQ, l3);
		// probe[2]
		method.visitInsn(Opcodes.RETURN);
		method.visitLabel(l3);
		// probe[3]
		method.visitInsn(Opcodes.RETURN);
	}

	@Test
	public void try_finally_should_create_4_probes() {
		createTryFinally();
		runMethodAnalzer();
		assertEquals(4, nextProbeId);
	}

	@Test
	public void try_finally_without_filter_should_show_all_branches() {
		createTryFinally();
		probes[0] = true;
		probes[3] = true;
		runMethodAnalzer();

		assertLine(1001, 2, 4, 2, 2);
	}

	private static final IFilter TRY_FINALLY_FILTER = new IFilter() {
		public void filter(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			final AbstractInsnNode i1 = methodNode.instructions.get(2);
			final AbstractInsnNode i2 = methodNode.instructions.get(7);
			assertEquals(Opcodes.IFEQ, i1.getOpcode());
			assertEquals(Opcodes.IFEQ, i2.getOpcode());
			output.merge(i1, i2);
			// Merging of already merged instructions won't change result:
			output.merge(i1, i2);
		}
	};

	@Test
	public void try_finally_with_filter_should_show_partial_branch_coverage_when_same_branch_is_covered_and_merged() {
		createTryFinally();
		probes[0] = true;
		probes[2] = true;
		runMethodAnalzer(TRY_FINALLY_FILTER);
		assertLine(1001, 2, 3, 1, 1);
	}

	@Test
	public void try_finally_with_filter_should_show_full_branch_coverage_when_different_branches_are_covered_and_merged() {
		createTryFinally();
		probes[0] = true;
		probes[3] = true;
		runMethodAnalzer(TRY_FINALLY_FILTER);
		assertLine(1001, 2, 3, 0, 2);
	}

	// === Scenario: descending line numbers ===

	private void createDescendingLineNumbers() {
		final Label l0 = new Label();
		method.visitLabel(l0);
		method.visitLineNumber(1003, l0);
		method.visitInsn(Opcodes.NOP);
		method.visitInsn(Opcodes.NOP);
		method.visitInsn(Opcodes.NOP);
		final Label l1 = new Label();
		method.visitLabel(l1);
		method.visitLineNumber(1002, l1);
		method.visitInsn(Opcodes.NOP);
		method.visitInsn(Opcodes.NOP);
		final Label l2 = new Label();
		method.visitLabel(l2);
		method.visitLineNumber(1001, l2);
		method.visitInsn(Opcodes.RETURN);
	}

	@Test
	public void decending_line_numbers_should_report_lines_correctly() {
		createDescendingLineNumbers();
		runMethodAnalzer();

		assertEquals(1001, result.getFirstLine());
		assertEquals(1003, result.getLastLine());
		assertLine(1001, 1, 0, 0, 0);
		assertLine(1002, 2, 0, 0, 0);
		assertLine(1003, 3, 0, 0, 0);
	}

	private void runMethodAnalzer() {
		runMethodAnalzer(Filters.NONE);
	}

	private void runMethodAnalzer(IFilter filter) {
		LabelFlowAnalyzer.markLabels(method);
		InstructionsBuilder builder = new InstructionsBuilder(probes);
		final MethodAnalyzer analyzer = new MethodAnalyzer(builder);

		final MethodProbesAdapter probesAdapter = new MethodProbesAdapter(
				analyzer, this);
		// note that CheckMethodAdapter verifies that this test does not violate
		// contracts of ASM API
		analyzer.accept(method, new CheckMethodAdapter(probesAdapter));

		MethodCoverageImpl mc = new MethodCoverageImpl("doit", "V()", null);
		MethodCoverageCalculator mcc = new MethodCoverageCalculator(
				builder.getInstructions());
		filter.filter(method, new FilterContextMock(), mcc);
		mcc.calculate(mc);
		result = mc;
	}

	private void assertLine(int nr, int insnMissed, int insnCovered,
			int branchesMissed, int branchesCovered) {
		final ILine line = result.getLine(nr);
		assertEquals("Instructions in line " + nr,
				CounterImpl.getInstance(insnMissed, insnCovered),
				line.getInstructionCounter());
		assertEquals("Branches in line " + nr,
				CounterImpl.getInstance(branchesMissed, branchesCovered),
				line.getBranchCounter());
	}

}
