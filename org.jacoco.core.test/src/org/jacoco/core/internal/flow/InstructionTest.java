/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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
		instruction.setPredecessor(predecessor);
		assertEquals(1, predecessor.getBranches());
	}

	@Test
	public void testSetCovered() {
		final Instruction predecessor = new Instruction(
				new InsnNode(Opcodes.NOP), 122);
		instruction.setPredecessor(predecessor);
		instruction.setCovered();
		assertEquals(1, instruction.getCoveredBranches());
		assertEquals(1, predecessor.getCoveredBranches());

		instruction.setCovered();
		assertEquals(2, instruction.getCoveredBranches());
		assertEquals(1, predecessor.getCoveredBranches());
	}

	@Test
	public void testSetCoveredOnLongSequence() {
		final Instruction first = new Instruction(new InsnNode(Opcodes.NOP), 0);
		Instruction next = first;
		for (int i = 0; i < 0x10000; i++) {
			final Instruction insn = new Instruction(new InsnNode(Opcodes.NOP),
					i);
			insn.setPredecessor(next);
			next = insn;
		}

		// The implementation must not cause an StackOverflowError even on very
		// long sequences:
		next.setCovered();
		assertEquals(1, first.getCoveredBranches());
	}

}
