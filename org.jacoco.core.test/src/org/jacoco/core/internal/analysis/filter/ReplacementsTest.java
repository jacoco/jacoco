/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

/**
 * Unit test for {@link Replacements}.
 */
public class ReplacementsTest {

	@Test
	public void should_accumulate() {
		final Replacements replacements = new Replacements();
		final InsnNode target1 = new InsnNode(Opcodes.NOP);
		final InsnNode target2 = new InsnNode(Opcodes.NOP);
		final InsnNode fromInstruction = new InsnNode(Opcodes.NOP);

		replacements.add(target1, fromInstruction, 0);
		replacements.add(target2, fromInstruction, 1);
		assertEquals(
				Arrays.<Collection<IFilterOutput.InstructionBranch>> asList(
						Collections.singletonList(
								new IFilterOutput.InstructionBranch(
										fromInstruction, 0)),
						Collections.singletonList(
								new IFilterOutput.InstructionBranch(
										fromInstruction, 1))),
				replacements.values());

		replacements.add(target1, fromInstruction, 2);
		assertEquals(
				Arrays.<Collection<IFilterOutput.InstructionBranch>> asList(
						Arrays.asList(
								new IFilterOutput.InstructionBranch(
										fromInstruction, 0),
								new IFilterOutput.InstructionBranch(
										fromInstruction, 2)),
						Collections.singletonList(
								new IFilterOutput.InstructionBranch(
										fromInstruction, 1))),
				replacements.values());
	}

	@Test
	public void should_ignore_default_branch_of_LookupSwitch_instruction() {
		final LabelNode defaultLabel = new LabelNode();
		final LabelNode caseA = new LabelNode();
		final LabelNode caseB = new LabelNode();
		final LookupSwitchInsnNode switchNode = new LookupSwitchInsnNode(
				defaultLabel, // branch 0
				new int[] { 0, 1, 2, 3 }, new LabelNode[] { //
						caseA, // branch 1
						defaultLabel, // branch 0
						caseA, // branch 1
						caseB // branch 2
				});
		assertEquals(
				Arrays.<Collection<IFilterOutput.InstructionBranch>> asList(
						Collections.singletonList(
								new IFilterOutput.InstructionBranch(switchNode,
										1)),
						Collections.singletonList(
								new IFilterOutput.InstructionBranch(switchNode,
										2))),
				Replacements.ignoreDefaultBranch(switchNode));
	}

	@Test
	public void should_ignore_default_branch_of_TableSwitch_instruction() {
		final LabelNode defaultLabel = new LabelNode();
		final LabelNode caseA = new LabelNode();
		final LabelNode caseB = new LabelNode();
		final TableSwitchInsnNode switchNode = new TableSwitchInsnNode(0, 3,
				defaultLabel, // branch 0
				caseA, // branch 1
				defaultLabel, // branch 0
				caseA, // branch 1
				caseB // branch 2
		);
		assertEquals(
				Arrays.<Collection<IFilterOutput.InstructionBranch>> asList(
						Collections.singletonList(
								new IFilterOutput.InstructionBranch(switchNode,
										1)),
						Collections.singletonList(
								new IFilterOutput.InstructionBranch(switchNode,
										2))),
				Replacements.ignoreDefaultBranch(switchNode));
	}

	private static <T> void assertEquals(final Iterable<T> expected,
			final Iterable<T> actual) {
		final Iterator<T> e = expected.iterator();
		final Iterator<T> a = actual.iterator();
		while (e.hasNext() && a.hasNext()) {
			Assert.assertEquals(e.next(), a.next());
		}
		Assert.assertEquals(e.hasNext(), a.hasNext());
	}

}
