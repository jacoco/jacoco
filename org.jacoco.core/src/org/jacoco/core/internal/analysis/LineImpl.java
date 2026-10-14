/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
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
	private static final int SINGLETON_INS_LIMIT = 16;

	/** Max branch counter value for which singletons are created */
	private static final int SINGLETON_BRA_LIMIT = 6;

	private static final LineImpl[][][][] SINGLETONS;

	static {
		SINGLETONS = new LineImpl[SINGLETON_BRA_LIMIT / 2 + 1][][][];
		for (int bt = 0, b = 0; bt <= SINGLETON_BRA_LIMIT; bt += 2, b += 1) {
			SINGLETONS[b] = new LineImpl[SINGLETON_INS_LIMIT][][];
			for (int it = 1,
					i = 0; it <= SINGLETON_INS_LIMIT; it += 1, i += 1) {
				SINGLETONS[b][i] = new LineImpl[it + 1][];
				for (int ic = 0; ic <= it; ic++) {
					SINGLETONS[b][i][ic] = new LineImpl[1 << bt];
					for (int bitMask = 0; bitMask < 1 << bt; bitMask++) {
						final int bc = bc(bt, bitMask);
						final int bm = bt - bc;
						final int im = it - ic;
						SINGLETONS[b][i][ic][bitMask] = new Fix(im, ic, bm, bc,
								bitMask);
					}
				}
			}
		}
	}

	/**
	 * Empty line without instructions and branches.
	 */
	public static final LineImpl EMPTY = new Fix(0, 0, 0, 0, 0);

	/**
	 * @return number of covered branches represented by given bitMask
	 */
	private static int bc(final int bt, final int bitMask) {
		int bc = 0;
		for (int b = 0; b < bt; b++) {
			if (get(bitMask, b)) {
				bc++;
			}
		}
		return bc;
	}

	private static LineImpl getInstance(final CounterImpl instructions,
			final CounterImpl branches, final int bitMask) {
		final int it = instructions.getTotalCount();
		final int bt = branches.getTotalCount();
		if (it == 0 && bt == 0) {
			return EMPTY;
		} else if (0 < it && it <= SINGLETON_INS_LIMIT //
				&& 0 < bt && bt <= SINGLETON_BRA_LIMIT //
				&& bt % 2 == 0) {
			final int ic = instructions.getCoveredCount();
			return SINGLETONS[bt / 2][it - 1][ic][bitMask];
		}
		return new Var(instructions, branches, bitMask);
	}

	/**
	 * @param bitMask
	 *            current bitmask
	 * @param branches
	 *            current branches counter
	 * @param coveredBranches
	 *            covered branches to add or {@code null}
	 * @param branchesIncrement
	 *            branches to add
	 * @return new bitmask
	 */
	private static int appendCoveredBranches(int bitMask,
			final ICounter branches, final BitSet coveredBranches,
			final ICounter branchesIncrement) {
		if (coveredBranches == null) {
			// is not line of MethodCoverageImpl
			final int covered = branches.getCoveredCount()
					+ branchesIncrement.getCoveredCount();
			return (1 << covered) - 1;
		}
		final int bt = branches.getTotalCount();
		for (int i = 0; i < branchesIncrement.getTotalCount(); i++) {
			if (coveredBranches.get(i)) {
				bitMask = set(bitMask, bt + i);
			}
		}
		return bitMask;
	}

	/**
	 * Mutable version.
	 */
	private static final class Var extends LineImpl {
		Var(final CounterImpl instructions, final CounterImpl branches,
				final int bitMask) {
			super(instructions, branches, bitMask);
		}

		@Override
		public LineImpl increment(final ICounter instructions,
				final ICounter branches, final BitSet coveredBranches) {
			this.coveredBranches = appendCoveredBranches(this.coveredBranches,
					this.branches, coveredBranches, branches);
			this.instructions = this.instructions.increment(instructions);
			this.branches = this.branches.increment(branches);
			return this;
		}
	}

	/**
	 * Immutable version.
	 */
	private static final class Fix extends LineImpl {
		public Fix(final int im, final int ic, final int bm, final int bc,
				final int bitMask) {
			super(CounterImpl.getInstance(im, ic),
					CounterImpl.getInstance(bm, bc), bitMask);
		}

		@Override
		public LineImpl increment(ICounter instructions, ICounter branches,
				BitSet coveredBranches) {
			final int bitMask = appendCoveredBranches(this.coveredBranches,
					this.branches, coveredBranches, branches);
			final CounterImpl incrementedInstructions = this.instructions
					.increment(instructions);
			final CounterImpl incrementedBranches = this.branches
					.increment(branches);
			return getInstance(incrementedInstructions, incrementedBranches,
					bitMask);
		}
	}

	/** instruction counter */
	protected CounterImpl instructions;

	/** branch counter */
	protected CounterImpl branches;

	/** bitmask of covered branches */
	protected int coveredBranches;

	private LineImpl(final CounterImpl instructions, final CounterImpl branches,
			final int bitMask) {
		this.instructions = instructions;
		this.branches = branches;
		this.coveredBranches = bitMask;
	}

	/**
	 * @deprecated used only in tests, use
	 *             {@link #increment(ICounter, ICounter, BitSet)} instead
	 */
	@Deprecated
	public final LineImpl increment(final ICounter instructions,
			final ICounter branches) {
		return increment(instructions, branches, null);
	}

	/**
	 * Adds the given counters to this line.
	 *
	 * @param instructions
	 *            instructions to add
	 * @param branches
	 *            branches to add
	 * @param coveredBranches
	 *            covered branches to add or {@code null}
	 * @return instance with new counter values
	 */
	public abstract LineImpl increment(final ICounter instructions,
			final ICounter branches, final BitSet coveredBranches);

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
