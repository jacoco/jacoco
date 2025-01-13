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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Base class for tests of {@link IFilter} implementations.
 */
public abstract class FilterTestBase {

	protected final FilterContextMock context = new FilterContextMock();

	private final List<Range> ignoredRanges = new ArrayList<Range>();

	private final Map<AbstractInsnNode, Set<AbstractInsnNode>> replacedBranches = new HashMap<AbstractInsnNode, Set<AbstractInsnNode>>();

	private final HashMap<AbstractInsnNode, Iterable<Collection<IFilterOutput.InstructionBranch>>> actualReplacements = new HashMap<AbstractInsnNode, Iterable<Collection<IFilterOutput.InstructionBranch>>>();

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

		public void replaceBranches(final AbstractInsnNode source,
				final Iterable<Collection<InstructionBranch>> newBranches) {
			actualReplacements.put(source, newBranches);
		}

		/**
		 * @deprecated scheduled for removal
		 */
		@Deprecated
		public void replaceBranches(final AbstractInsnNode source,
				final Set<AbstractInsnNode> newTargets) {
			replacedBranches.put(source, newTargets);
		}
	};

	final void assertIgnored(Range... ranges) {
		assertArrayEquals(ranges, ignoredRanges.toArray(new Range[0]));
	}

	final void assertMethodIgnored(final MethodNode m) {
		assertIgnored(
				new Range(m.instructions.getFirst(), m.instructions.getLast()));
	}

	final void assertNoReplacedBranches() {
		assertTrue(replacedBranches.isEmpty());
		assertTrue(actualReplacements.isEmpty());
	}

	/**
	 * @deprecated use
	 *             {@link #assertReplacedBranches(MethodNode, AbstractInsnNode, List)}
	 *             instead
	 */
	@Deprecated
	final void assertReplacedBranches(final AbstractInsnNode source,
			final Set<AbstractInsnNode> newTargets) {
		assertReplacedBranches(Collections.singletonMap(source, newTargets));
	}

	final void assertReplacedBranches(
			final Map<AbstractInsnNode, Set<AbstractInsnNode>> expected) {
		assertEquals(expected, replacedBranches);
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

		Collections.sort(expectedReplacements, new Comparator<Replacement>() {
			public int compare(final Replacement r1, final Replacement r2) {
				return r1.newBranch - r2.newBranch;
			}
		});

		final StringBuilder expectedStringBuilder = new StringBuilder();
		for (final Replacement replacement : expectedReplacements) {
			expectedStringBuilder.append(replacement.newBranch)
					.append(" if branch ").append(replacement.branch)
					.append(" of instruction ").append(methodNode.instructions
							.indexOf(replacement.instruction))
					.append("\n");
		}

		final StringBuilder actualStringBuilder = new StringBuilder();
		int newBranch = 0;
		for (final Collection<IFilterOutput.InstructionBranch> pairs : actualReplacements
				.get(source)) {
			for (IFilterOutput.InstructionBranch pair : pairs) {
				actualStringBuilder.append(newBranch).append(" if branch ")
						.append(pair.branch).append(" of instruction ")
						.append(methodNode.instructions
								.indexOf(pair.instruction))
						.append("\n");
			}
			newBranch++;
		}

		assertEquals(expectedStringBuilder.toString(),
				actualStringBuilder.toString());
	}

	static class Replacement {
		final int newBranch;
		final AbstractInsnNode instruction;
		final int branch;

		Replacement(int newBranch, AbstractInsnNode instruction, int branch) {
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
