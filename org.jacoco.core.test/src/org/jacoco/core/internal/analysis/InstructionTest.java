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

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Instruction}.
 */
public class InstructionTest {

	private Instruction instruction;

	@Before
	public void setup() {
		instruction = new Instruction(123);
	}

	@Test
	public void getLine_should_return_line_number() {
		assertEquals(123, instruction.getLine());
	}

	@Test
	public void new_instance_should_have_no_coverage_and_no_branches() {
		assertEquals(CounterImpl.COUNTER_1_0,
				instruction.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, instruction.getBranchCounter());
	}

	@Test
	public void addBranchWithInstruction_should_not_increment_branches_when_only_one_branch_is_added() {
		instruction.addBranch(new Instruction(122), 0);

		assertEquals(CounterImpl.COUNTER_0_0, instruction.getBranchCounter());
	}

	@Test
	public void addBranchWithInstruction_should_increment_branches_when_two_branches_are_added() {
		instruction.addBranch(new Instruction(122), 0);
		instruction.addBranch(new Instruction(123), 1);

		assertEquals(CounterImpl.getInstance(2, 0),
				instruction.getBranchCounter());
	}

	@Test
	public void addBranchWithInstruction_should_propagate_existing_coverage_status() {
		final Instruction target = new Instruction(122);
		target.addBranch(true, 0);

		instruction.addBranch(target, 0);

		assertEquals(CounterImpl.COUNTER_0_1,
				instruction.getInstructionCounter());
	}

	@Test
	public void addBranchWithProbe_should_increment_branches_when_covered() {
		instruction.addBranch(true, 0);
		instruction.addBranch(true, 1);

		assertEquals(CounterImpl.getInstance(0, 1),
				instruction.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(0, 2),
				instruction.getBranchCounter());
	}

	@Test
	public void addBranchWithProbe_should_increment_branches_when_not_covered() {
		instruction.addBranch(false, 0);
		instruction.addBranch(false, 1);

		assertEquals(CounterImpl.getInstance(1, 0),
				instruction.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(2, 0),
				instruction.getBranchCounter());
	}

	@Test
	public void addBranchWithProbe_should_increment_branches_when_partly_covered() {
		instruction.addBranch(false, 0);
		instruction.addBranch(true, 1);

		assertEquals(CounterImpl.getInstance(0, 1),
				instruction.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(1, 1),
				instruction.getBranchCounter());
	}

	@Test
	public void addBranchWithProbe_should_propagate_coverage_status_to_existing_predecessors() {
		final Instruction i1 = new Instruction(124);
		final Instruction i2 = new Instruction(125);
		instruction.addBranch(i1, 3);
		i1.addBranch(i2, 5);

		i2.addBranch(true, 8);

		assertEquals(CounterImpl.COUNTER_0_1,
				instruction.getInstructionCounter());
	}

	@Test
	public void addBranch_should_count_large_number_of_branches() {
		for (int branch = 0; branch < 0x1000; branch++) {
			instruction.addBranch(true, branch);
		}

		assertEquals(CounterImpl.getInstance(0, 0x1000),
				instruction.getBranchCounter());
	}

	@Test
	public void addBranch_should_propagate_coverage_status_over_very_long_sequence() {
		Instruction next = instruction;
		for (int i = 0; i < 0x10000; i++) {
			final Instruction insn = new Instruction(i);
			next.addBranch(insn, 0);
			next = insn;
		}
		next.addBranch(true, 0);

		assertEquals(CounterImpl.COUNTER_0_1,
				instruction.getInstructionCounter());
	}

	@Test
	public void merge_should_calculate_superset_of_covered_branches() {
		final Instruction i1 = new Instruction(124);
		i1.addBranch(false, 1);
		i1.addBranch(false, 2);
		i1.addBranch(true, 3);
		i1.addBranch(true, 4);
		final Instruction i2 = new Instruction(124);
		i2.addBranch(false, 1);
		i2.addBranch(true, 2);
		i2.addBranch(false, 3);
		i2.addBranch(true, 4);

		instruction = i1.merge(i2);

		assertEquals(CounterImpl.getInstance(1, 3),
				instruction.getBranchCounter());
	}

	@Test
	public void replaceBranches_should_calculate_coverage_on_new_branches() {
		Instruction i1 = new Instruction(1);
		Instruction i2 = new Instruction(2);
		Instruction i3 = new Instruction(3);
		i3.addBranch(true, 0);

		instruction = instruction.replaceBranches(Arrays.asList(i1, i2, i3));

		assertEquals(CounterImpl.getInstance(2, 1),
				instruction.getBranchCounter());
	}
}
