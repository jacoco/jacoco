/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.BitSet;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;

/**
 * Implementation of {@link ILine}.
 */
public abstract class LineImpl implements ILine {

	/** Max instruction counter value for which singletons are created */
	private static final int SINGLETON_INS_LIMIT = 8;

	/** Max branch counter value for which singletons are created */
	private static final int SINGLETON_BRA_LIMIT = 0;

	private static final LineImpl[][][][] SINGLETONS = new LineImpl[SINGLETON_INS_LIMIT
			+ 1][][][];

	static {
		for (int i = 0; i <= SINGLETON_INS_LIMIT; i++) {
			SINGLETONS[i] = new LineImpl[SINGLETON_INS_LIMIT + 1][][];
			for (int j = 0; j <= SINGLETON_INS_LIMIT; j++) {
				SINGLETONS[i][j] = new LineImpl[SINGLETON_BRA_LIMIT + 1][];
				for (int k = 0; k <= SINGLETON_BRA_LIMIT; k++) {
					SINGLETONS[i][j][k] = new LineImpl[SINGLETON_BRA_LIMIT + 1];
					for (int l = 0; l <= SINGLETON_BRA_LIMIT; l++) {
						SINGLETONS[i][j][k][l] = new Fix(i, j, k, l);
					}
				}
			}
		}
	}

	/**
	 * Empty line without instructions or branches.
	 */
	public static final LineImpl EMPTY = SINGLETONS[0][0][0][0];

	private static LineImpl getInstance(final CounterImpl instructions,
			final CounterImpl branches) {
		final int im = instructions.getMissedCount();
		final int ic = instructions.getCoveredCount();
		final int bm = branches.getMissedCount();
		final int bc = branches.getCoveredCount();
		if (im <= SINGLETON_INS_LIMIT && ic <= SINGLETON_INS_LIMIT
				&& bm <= SINGLETON_BRA_LIMIT && bc <= SINGLETON_BRA_LIMIT) {
			return SINGLETONS[im][ic][bm][bc];
		}
		return new Var(instructions, branches);
	}

	/**
	 * Mutable version.
	 */
	private static final class Var extends LineImpl {
		Var(final CounterImpl instructions, final CounterImpl branches) {
			super(instructions, branches);
		}

		@Override
		public LineImpl increment(final ICounter instructions,
				final ICounter branches) {
			this.instructions = this.instructions.increment(instructions);
			this.branches = this.branches.increment(branches);
			return this;
		}
	}

	/**
	 * Immutable version.
	 */
	private static final class Fix extends LineImpl {
		public Fix(final int im, final int ic, final int bm, final int bc) {
			super(CounterImpl.getInstance(im, ic),
					CounterImpl.getInstance(bm, bc));
		}

		@Override
		public LineImpl increment(final ICounter instructions,
				final ICounter branches) {
			return getInstance(this.instructions.increment(instructions),
					this.branches.increment(branches));
		}

		@Override
		public void appendCoveredBranches(int oldBranchesTotalCount,
				int newBranchesTotalCount, BitSet coveredBranches) {
			throw new IllegalStateException();
		}
	}

	/** instruction counter */
	protected CounterImpl instructions;

	/** branch counter */
	protected CounterImpl branches;

	protected int coveredBranches;

	private LineImpl(final CounterImpl instructions,
			final CounterImpl branches) {
		this.instructions = instructions;
		this.branches = branches;
	}

	/**
	 * Adds the given counters to this line.
	 *
	 * @param instructions
	 *            instructions to add
	 * @param branches
	 *            branches to add
	 * @return instance with new counter values
	 */
	public abstract LineImpl increment(final ICounter instructions,
			final ICounter branches);

	// === ILine implementation ===

	public int getStatus() {
		return instructions.getStatus() | branches.getStatus();
	}

	public ICounter getInstructionCounter() {
		return instructions;
	}

	public ICounter getBranchCounter() {
		return branches;
	}

	/**
	 * @return covered branches in the order of bytecode traversal
	 */
	public final BitSet getCoveredBranches() {
		final int size = Math.min(branches.getTotalCount(), 31);
		final BitSet result = new BitSet(size);
		for (int i = 0; i < size; i++) {
			result.set(i, get(coveredBranches, i));
		}
		return result;
	}

	/**
	 * This method must be called after {@link #increment(ICounter, ICounter)}.
	 *
	 * @param oldBranchesTotalCount
	 *            total count of branches before
	 *            {@link #increment(ICounter, ICounter)}
	 * @param newBranchesTotalCount
	 *            total count of branches after
	 *            {@link #increment(ICounter, ICounter)}
	 * @param coveredBranches
	 *            to append
	 */
	public void appendCoveredBranches(final int oldBranchesTotalCount,
			final int newBranchesTotalCount, final BitSet coveredBranches) {
		final int size = newBranchesTotalCount - oldBranchesTotalCount;
		for (int i = 0; i < size; i++) {
			if (coveredBranches.get(i)) {
				this.coveredBranches = set(this.coveredBranches,
						i + oldBranchesTotalCount);
			}
		}
	}

	private static int set(final int bitSet, final int index) {
		if (index > 31) {
			return bitSet;
		}
		return bitSet | (1 << index);
	}

	private static boolean get(final int bitSet, final int index) {
		if (index > 31) {
			return false;
		}
		return (bitSet & (1 << index)) != 0;
	}

	@Override
	public int hashCode() {
		return 23 * instructions.hashCode() ^ branches.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ILine) {
			final ILine that = (ILine) obj;
			return this.instructions.equals(that.getInstructionCounter())
					&& this.branches.equals(that.getBranchCounter());
		}
		return false;
	}

}
