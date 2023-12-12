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

import java.util.Map;

import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.flow.LabelInfo;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;

/**
 * Unit tests for {@link InstructionsBuilder}.
 */
public class InstructionsBuilderTest {

	private InstructionsBuilder builder;

	@Before
	public void setup() {
		builder = new InstructionsBuilder(new boolean[] { false, true });
	}

	@Test
	public void current_line_number_should_be_applied_to_instructions() {
		InsnNode i1 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i1);

		builder.setCurrentLine(10);
		InsnNode i2 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i2);
		InsnNode i3 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i3);

		builder.setCurrentLine(20);
		InsnNode i4 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i4);

		Map<AbstractInsnNode, Instruction> map = builder.getInstructions();
		assertEquals(ISourceNode.UNKNOWN_LINE, map.get(i1).getLine());
		assertEquals(10, map.get(i2).getLine());
		assertEquals(10, map.get(i3).getLine());
		assertEquals(20, map.get(i4).getLine());
	}

	@Test
	public void null_probearray_should_not_mark_instruction_as_covered() {
		builder = new InstructionsBuilder(null);

		InsnNode i1 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i1);
		builder.addProbe(5, 0);

		Map<AbstractInsnNode, Instruction> map = builder.getInstructions();
		assertEquals(CounterImpl.COUNTER_1_0,
				map.get(i1).getInstructionCounter());
	}

	@Test
	public void unexecuted_probe_should_not_mark_instruction_as_covered() {
		InsnNode i1 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i1);
		builder.addProbe(0, 0);

		Map<AbstractInsnNode, Instruction> map = builder.getInstructions();
		assertEquals(CounterImpl.COUNTER_1_0,
				map.get(i1).getInstructionCounter());
	}

	@Test
	public void executed_probe_should_mark_instruction_as_covered() {
		InsnNode i1 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i1);
		builder.addProbe(1, 0);

		Map<AbstractInsnNode, Instruction> map = builder.getInstructions();
		assertEquals(CounterImpl.COUNTER_0_1,
				map.get(i1).getInstructionCounter());
	}

	@Test
	public void subsequent_instructions_should_be_linked_by_default() {
		InsnNode i1 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i1);

		InsnNode i2 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i2);

		// mark i2 as covered
		builder.addProbe(1, 0);

		// coverage should be propagated to i1
		Map<AbstractInsnNode, Instruction> map = builder.getInstructions();
		assertEquals(CounterImpl.COUNTER_0_1,
				map.get(i1).getInstructionCounter());
	}

	@Test
	public void subsequent_instructions_should_not_be_linked_when_noSuccessor_was_called() {
		InsnNode i1 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i1);
		builder.noSuccessor();

		InsnNode i2 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i2);

		// mark i2 as covered
		builder.addProbe(1, 0);

		// coverage should not be propagated to i1
		Map<AbstractInsnNode, Instruction> map = builder.getInstructions();
		assertEquals(CounterImpl.COUNTER_1_0,
				map.get(i1).getInstructionCounter());
	}

	@Test
	public void subsequent_instructions_should_be_linked_after_label_marked_as_successor() {
		InsnNode i1 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i1);

		Label l = new Label();
		LabelInfo.setSuccessor(l);
		builder.addLabel(l);
		InsnNode i2 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i2);

		// mark i2 as covered
		builder.addProbe(1, 0);

		// coverage should be propagated to i1
		Map<AbstractInsnNode, Instruction> map = builder.getInstructions();
		assertEquals(CounterImpl.COUNTER_0_1,
				map.get(i1).getInstructionCounter());
	}

	@Test
	public void subsequent_instructions_should_not_be_linked_after_label_not_marked_as_successor() {
		InsnNode i1 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i1);

		builder.addLabel(new Label());
		InsnNode i2 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i2);

		// mark i2 as covered
		builder.addProbe(1, 0);

		// coverage should not be propagated to i1
		Map<AbstractInsnNode, Instruction> map = builder.getInstructions();
		assertEquals(CounterImpl.COUNTER_1_0,
				map.get(i1).getInstructionCounter());
	}

	@Test
	public void jumps_should_propagate_coverage_status() {
		InsnNode i1 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i1);
		Label l2 = new Label();
		builder.addJump(l2, 0);

		builder.addLabel(l2);
		InsnNode i2 = new InsnNode(Opcodes.NOP);
		builder.addInstruction(i2);

		// mark i2 as covered
		builder.addProbe(1, 0);

		// coverage should be propagated to i1
		Map<AbstractInsnNode, Instruction> map = builder.getInstructions();
		assertEquals(CounterImpl.COUNTER_0_1,
				map.get(i1).getInstructionCounter());
	}

}
