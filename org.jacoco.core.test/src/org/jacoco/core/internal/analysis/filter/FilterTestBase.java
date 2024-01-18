/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
				final Set<AbstractInsnNode> newTargets) {
			replacedBranches.put(source, newTargets);
		}
	};

	/**
	 * Sorts ranges so that {@link Range} 1 with {@link Range#toInclusive} which
	 * is before {@link Range} 2 appears in the list {@link #ignoredRanges}
	 * before it
	 */
	protected void sortIgnoredRanges() {
		Collections.sort(ignoredRanges, new Comparator<Range>() {
			public int compare(Range o1, Range o2) {
				if (o1.toInclusive == o2.toInclusive)
					return 0;
				AbstractInsnNode node = o1.toInclusive;
				while (node != null) {
					if (node == o2.toInclusive) {
						return -1;
					}
					node = node.getNext();
				}
				return 1;
			}
		});
	}

	/**
	 * Merges {@link #ignoredRanges} so that if there is no gap between 2 ranges
	 * they will be merged to one range
	 */
	protected void mergeIgnoredRanges() {
		sortIgnoredRanges();

		ListIterator<Range> iterator = ignoredRanges.listIterator();

		Range previous = null;
		List<Range> merged = new ArrayList<Range>();
		while (iterator.hasNext()) {
			Range current = iterator.next();
			if (previous == null) {
				previous = current;
				continue;
			}
			AbstractInsnNode prevRangeEnd = previous.toInclusive.getNext();
			while (prevRangeEnd != current.fromInclusive) {
				prevRangeEnd = prevRangeEnd.getPrevious();
				if (prevRangeEnd == null) {
					break;
				}
			}
			if (prevRangeEnd != null) {
				previous.toInclusive = current.toInclusive;
			} else {
				merged.add(previous);
				previous = current;
			}
		}
		ignoredRanges.clear();
		ignoredRanges.addAll(merged);
		if (!merged.contains(previous)) {
			ignoredRanges.add(previous);
		}
	}

	final void assertIgnored(Range... ranges) {
		assertArrayEquals(ranges, ignoredRanges.toArray(new Range[0]));
	}

	final void assertMethodIgnored(final MethodNode m) {
		assertIgnored(
				new Range(m.instructions.getFirst(), m.instructions.getLast()));
	}

	final void assertNoReplacedBranches() {
		assertTrue(replacedBranches.isEmpty());
	}

	final void assertReplacedBranches(final AbstractInsnNode source,
			final Set<AbstractInsnNode> newTargets) {
		assertEquals(Collections.singletonMap(source, newTargets),
				replacedBranches);
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

		@Override
		public int hashCode() {
			int result = fromInclusive != null ? fromInclusive.hashCode() : 0;
			result = 31 * result
					+ (toInclusive != null ? toInclusive.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "Range{" + "fromInclusive=" + fromInclusive
					+ ", toInclusive=" + toInclusive + '}';
		}
	}

}
