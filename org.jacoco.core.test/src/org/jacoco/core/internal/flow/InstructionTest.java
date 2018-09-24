/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;

/**
 * Unit tests for {@link Instruction}.
 */
public class InstructionTest {

	private Instruction instruction;

	@Before
	public void setup() {
		instruction = new Instruction(new InsnNode(Opcodes.NOP), 123);
	}

	@Test
	public void testInit() {
		final InsnNode node = new InsnNode(Opcodes.NOP);
		instruction = new Instruction(node, 123);
		assertSame(node, instruction.getNode());
		assertEquals(123, instruction.getLine());
		assertEquals(0, instruction.getBranches());
		assertEquals(0, instruction.getCoveredBranches());
	}

	@Test
	public void testAddBranch() {
		instruction.addBranch();
		assertEquals(1, instruction.getBranches());
		instruction.addBranch();
		assertEquals(2, instruction.getBranches());
		instruction.addBranch();
		assertEquals(3, instruction.getBranches());
		assertEquals(0, instruction.getCoveredBranches());
	}

	@Test
	public void testSetPredecessor() {
		final Instruction predecessor = new Instruction(
				new InsnNode(Opcodes.NOP), 122);
		instruction.setPredecessor(predecessor, 0);
		assertEquals(1, predecessor.getBranches());
	}

	@Test
	public void setCovered_should_mark_branch_in_predecessor() {
		final Instruction i = new Instruction(new InsnNode(Opcodes.NOP), 122);
		i.setCovered(2);
		assertEquals(1, i.getCoveredBranches());
		assertEquals("{2}", i.toString());

		final Instruction s1 = new Instruction(new InsnNode(Opcodes.NOP), 123);
		s1.setPredecessor(i, 1);
		s1.setCovered(0);
		assertEquals("{0}", s1.toString());
		assertEquals(1, s1.getCoveredBranches());
		assertEquals("{1, 2}", i.toString());
		assertEquals(2, i.getCoveredBranches());

		final Instruction s2 = new Instruction(new InsnNode(Opcodes.NOP), 124);
		s2.setPredecessor(i, 0);
		s2.setCovered(1);
		assertEquals("{0}", s1.toString());
		assertEquals(1, s2.getCoveredBranches());
		assertEquals("{0, 1, 2}", i.toString());
		assertEquals(3, i.getCoveredBranches());
	}

	@Test
	public void should_use_BitSet_to_hold_information_about_branches_of_big_switches() {
		for (int branch = 0; branch < 256; branch++) {
			instruction.setCovered(branch);
		}
		assertEquals(256, instruction.getCoveredBranches());
	}

	@Test
	public void merge_should_add_covered_branches_from_another_instruction() {
		final Instruction i1 = new Instruction(new InsnNode(Opcodes.NOP), 123);
		i1.setCovered(0);
		final Instruction i2 = new Instruction(new InsnNode(Opcodes.NOP), 123);
		i2.setCovered(1);
		i1.merge(i2);
		assertEquals("{0, 1}", i1.toString());
		assertEquals(2, i1.getCoveredBranches());
		assertEquals("{1}", i2.toString());
	}

	@Test
	public void testSetCoveredOnLongSequence() {
		final Instruction first = new Instruction(new InsnNode(Opcodes.NOP), 0);
		Instruction next = first;
		for (int i = 0; i < 0x10000; i++) {
			final Instruction insn = new Instruction(new InsnNode(Opcodes.NOP),
					i);
			insn.setPredecessor(next, 0);
			next = insn;
		}

		// The implementation must not cause an StackOverflowError even on very
		// long sequences:
		next.setCovered(0);
		assertEquals(1, first.getCoveredBranches());
	}

}
