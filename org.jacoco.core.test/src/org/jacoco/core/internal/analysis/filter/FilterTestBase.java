/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Base class for tests of {@link IFilter} implementations.
 */
public abstract class FilterTestBase {

	protected final FilterContextMock context = new FilterContextMock();

	private final List<Range> ignoredRanges = new ArrayList<Range>();

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
			fail();
		}
	};

	final void assertIgnored(Range... ranges) {
		assertArrayEquals(ranges, ignoredRanges.toArray(new Range[0]));
	}

	final void assertMethodIgnored(final MethodNode m) {
		assertIgnored(
				new Range(m.instructions.getFirst(), m.instructions.getLast()));
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
