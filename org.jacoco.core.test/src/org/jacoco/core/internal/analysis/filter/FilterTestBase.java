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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Base class for tests of {@link IFilter} implementations.
 */
public abstract class FilterTestBase {

	protected final FilterContextMock context = new FilterContextMock();

	private final List<Range> ignoredRanges = new ArrayList<Range>();

	private final HashMap<AbstractInsnNode, Iterable<Collection<Replacements.InstructionBranch>>> actualReplacements = new HashMap<AbstractInsnNode, Iterable<Collection<Replacements.InstructionBranch>>>();

	protected final IFilterOutput output = new IFilterOutput() {
		public void ignore(final AbstractInsnNode fromInclusive,
				final AbstractInsnNode toInclusive) {
			final Range range = new Range();
			range.fromInclusive = fromInclusive;
			range.toInclusive = toInclusive;
			ignoredRanges.add(range);
		}

		public void merge(final AbstractInsnNode i1,
				final AbstractInsnNode i2) {
			fail();
		}

		public void replaceBranches(AbstractInsnNode source,
				Replacements replacements) {
			actualReplacements.put(source, replacements.values());
		}
	};

	final void assertIgnored(final MethodNode methodNode,
			final Range... expected) {
		assertEquals("ignored ranges",
				rangesToString(methodNode, Arrays.asList(expected)),
				rangesToString(methodNode, ignoredRanges));
	}

	private static String rangesToString(final MethodNode m,
			final List<Range> ranges) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < ranges.size(); i++) {
			final Range range = ranges.get(i);
			stringBuilder.append("range ").append(i)
					.append(" from instruction ")
					.append(m.instructions.indexOf(range.fromInclusive))
					.append(" to ")
					.append(m.instructions.indexOf(range.toInclusive))
					.append("\n");
		}
		return stringBuilder.toString();
	}

	final void assertMethodIgnored(final MethodNode m) {
		assertIgnored(m,
				new Range(m.instructions.getFirst(), m.instructions.getLast()));
	}

	final void assertNoReplacedBranches() {
		assertTrue(actualReplacements.isEmpty());
	}

	final void assertReplacedBranches(final MethodNode methodNode,
			final AbstractInsnNode source,
			final List<Replacement> expectedReplacements) {
		assertReplacedBranches(methodNode,
				Collections.singletonMap(source, expectedReplacements));
	}

	final void assertReplacedBranches(final MethodNode methodNode,
			final Map<AbstractInsnNode, List<Replacement>> expectedReplacements) {
		assertEquals(expectedReplacements.size(), actualReplacements.size());
		for (final Map.Entry<AbstractInsnNode, List<Replacement>> entry : expectedReplacements
				.entrySet()) {
			final AbstractInsnNode node = entry.getKey();
			final List<Replacement> replacements = entry.getValue();
			assertReplacements(methodNode, node, replacements);
		}
	}

	private void assertReplacements(final MethodNode methodNode,
			final AbstractInsnNode source,
			final List<Replacement> expectedReplacements) {
		final ArrayList<String> expectedStrings = new ArrayList<String>();
		for (final Replacement replacement : expectedReplacements) {
			expectedStrings.add("\n" + replacement.newBranch + " if branch "
					+ replacement.branch + " of instruction "
					+ methodNode.instructions.indexOf(replacement.instruction));
		}
		Collections.sort(expectedStrings);

		final ArrayList<String> actualStrings = new ArrayList<String>();
		int newBranch = 0;
		for (final Collection<Replacements.InstructionBranch> pairs : actualReplacements
				.get(source)) {
			for (Replacements.InstructionBranch pair : pairs) {
				actualStrings.add("\n" + newBranch + " if branch " + pair.branch
						+ " of instruction "
						+ methodNode.instructions.indexOf(pair.instruction));
			}
			newBranch++;
		}
		Collections.sort(actualStrings);

		assertEquals(expectedStrings.toString(), actualStrings.toString());
	}

	static class Replacement {
		final int newBranch;
		final AbstractInsnNode instruction;
		final int branch;

		Replacement(final int newBranch, final AbstractInsnNode instruction,
				final int branch) {
			this.newBranch = newBranch;
			this.instruction = instruction;
			this.branch = branch;
		}
	}

	static class Range {
		AbstractInsnNode fromInclusive;
		AbstractInsnNode toInclusive;

		Range() {
		}

		Range(AbstractInsnNode fromInclusive, AbstractInsnNode toInclusive) {
			this.fromInclusive = fromInclusive;
			this.toInclusive = toInclusive;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() == Range.class) {
				final Range other = (Range) obj;
				return this.fromInclusive.equals(other.fromInclusive)
						&& this.toInclusive.equals(other.toInclusive);
			}
			return false;
		}
	}

}
