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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jacoco.core.analysis.ISourceNode;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

/**
 * Unit tests for {@link MethodCoverageCalculator}.
 */
public class MethodCoverageCalculatorTest {

	private Map<AbstractInsnNode, Instruction> instructions;

	// The purpose of this list is to link instruction nodes
	private InsnList list;

	private MethodCoverageImpl coverage;

	@Before
	public void setup() {
		instructions = new HashMap<AbstractInsnNode, Instruction>();
		coverage = new MethodCoverageImpl("run", "()V", null);
		list = new InsnList();
	}

	@Test
	public void should_report_instructions() {
		addInsn(1, true);
		addInsn(2, true);
		addInsn(2, false);
		addInsn(3, false);

		MethodCoverageCalculator c = new MethodCoverageCalculator(instructions);
		c.calculate(coverage);

		assertLine(1, 0, 1, 0, 0);
		assertLine(2, 1, 1, 0, 0);
		assertLine(3, 1, 0, 0, 0);
	}

	@Test
	public void should_report_instructions_with_branches() {
		addInsn(1, false, false);
		addInsn(2, false, false, true);
		addInsn(3, false, true, true);

		MethodCoverageCalculator c = new MethodCoverageCalculator(instructions);
		c.calculate(coverage);

		assertLine(1, 1, 0, 2, 0);
		assertLine(2, 0, 1, 2, 1);
		assertLine(3, 0, 1, 1, 2);
	}

	@Test
	public void should_ignore_single_instruction() {
		addInsn(1, true);
		InsnNode i1 = addInsn(1, false);
		addInsn(2, true);

		MethodCoverageCalculator c = new MethodCoverageCalculator(instructions);
		c.ignore(i1, i1);
		c.calculate(coverage);

		assertLine(1, 0, 1, 0, 0); // only one instruction not filtered
		assertLine(2, 0, 1, 0, 0);
	}

	@Test
	public void should_ignore_instruction_range() {
		addInsn(1, true);
		InsnNode i1 = addInsn(2, false);
		addInsn(2, false);
		addInsn(2, false);
		addInsn(2, false);
		InsnNode i2 = addInsn(2, false);
		addInsn(3, true);

		MethodCoverageCalculator c = new MethodCoverageCalculator(instructions);
		c.ignore(i1, i2);
		c.calculate(coverage);

		assertLine(1, 0, 1, 0, 0);
		assertLine(2, 0, 0, 0, 0); // all instructions filtered in line 2
		assertLine(3, 0, 1, 0, 0);
	}

	@Test
	public void should_exclude_ignored_instructions_from_computation_of_first_and_last_lines() {
		InsnNode i1 = addInsn(1, false);
		addInsn(2, false);
		InsnNode i3 = addInsn(3, false);

		MethodCoverageCalculator c = new MethodCoverageCalculator(instructions);
		c.ignore(i1, i1);
		c.ignore(i3, i3);
		c.calculate(coverage);

		assertEquals(2, coverage.getFirstLine());
		assertEquals(2, coverage.getLastLine());
	}

	@Test
	public void should_merge_instructions() {
		addInsn(1, true);
		InsnNode i1 = addInsn(2, false, true);
		InsnNode i2 = addInsn(2, true, false);
		addInsn(3, true);

		MethodCoverageCalculator c = new MethodCoverageCalculator(instructions);
		c.merge(i1, i2);
		c.calculate(coverage);

		assertLine(1, 0, 1, 0, 0);
		assertLine(2, 0, 1, 0, 2); // one fully covered instruction left
		assertLine(3, 0, 1, 0, 0);
	}

	@Test
	public void should_merge_multiple_instructions() {
		InsnNode i1 = addInsn(1, true, false, false);
		InsnNode i2 = addInsn(1, false, true, false);
		InsnNode i3 = addInsn(1, false, false, true);

		MethodCoverageCalculator c = new MethodCoverageCalculator(instructions);
		c.merge(i1, i2);
		c.merge(i2, i3);
		c.calculate(coverage);

		assertLine(1, 0, 1, 0, 3); // one fully covered instruction left
	}

	@Test
	public void should_merge_instructions_redundant() {
		addInsn(1, true);
		InsnNode i1 = addInsn(2, false, true);
		InsnNode i2 = addInsn(2, true, false);
		addInsn(3, true);

		MethodCoverageCalculator c = new MethodCoverageCalculator(instructions);
		c.merge(i1, i2);
		c.merge(i2, i1);
		c.calculate(coverage);

		assertLine(1, 0, 1, 0, 0);
		assertLine(2, 0, 1, 0, 2); // one fully covered instruction left
		assertLine(3, 0, 1, 0, 0);
	}

	@Test
	public void should_replace_branches() {
		InsnNode i1 = addInsn(1);
		InsnNode i2 = addInsn(2, true);
		InsnNode i3 = addInsn(2, true);
		InsnNode i4 = addInsn(2, false);

		MethodCoverageCalculator c = new MethodCoverageCalculator(instructions);
		c.replaceBranches(i1,
				new HashSet<AbstractInsnNode>(Arrays.asList(i2, i3, i4)));
		c.calculate(coverage);

		assertLine(1, 0, 1, 1, 2); // branches coverage status replaced
		assertLine(2, 1, 2, 0, 0); // still in place
	}

	@Test
	public void should_replace_branches_with_merged_instructions() {
		InsnNode i1 = addInsn(1, false, false, false);
		InsnNode i2 = addInsn(2, true);
		InsnNode i3 = addInsn(2, false);
		InsnNode i4 = addInsn(2, false);

		MethodCoverageCalculator c = new MethodCoverageCalculator(instructions);
		c.merge(i4, i3);
		c.merge(i3, i2);
		c.replaceBranches(i1,
				new HashSet<AbstractInsnNode>(Arrays.asList(i2, i3, i4)));
		c.calculate(coverage);

		assertLine(1, 0, 1, 0, 3);
	}

	@Test
	public void should_work_without_lines() {
		addInsn(ISourceNode.UNKNOWN_LINE, false);
		addInsn(ISourceNode.UNKNOWN_LINE, false);
		addInsn(ISourceNode.UNKNOWN_LINE, true);

		MethodCoverageCalculator c = new MethodCoverageCalculator(instructions);
		c.calculate(coverage);

		assertEquals(ISourceNode.UNKNOWN_LINE, coverage.getFirstLine());
		assertEquals(ISourceNode.UNKNOWN_LINE, coverage.getLastLine());
		assertEquals(CounterImpl.getInstance(2, 1),
				coverage.getInstructionCounter());
	}

	private void assertLine(int idx, int mi, int ci, int mb, int cb) {
		assertEquals("instructions", CounterImpl.getInstance(mi, ci),
				coverage.getLine(idx).getInstructionCounter());
		assertEquals("branches", CounterImpl.getInstance(mb, cb),
				coverage.getLine(idx).getBranchCounter());
	}

	private InsnNode addInsn(int line, boolean... branches) {
		Instruction i = new Instruction(line);
		int idx = 0;
		for (boolean covered : branches) {
			i.addBranch(covered, idx++);
		}
		InsnNode node = new InsnNode(Opcodes.NOP);
		list.add(node);
		instructions.put(node, i);
		return node;
	}

}
