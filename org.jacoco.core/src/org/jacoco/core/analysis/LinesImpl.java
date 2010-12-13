/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.analysis;

/**
 * {@link ILines} implementation.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class LinesImpl extends AbstractCounter implements ILines {

	/**
	 * Status for each line. Each entry is used as follows:
	 * 
	 * <pre>
	 * Bits          | 7654321076543210
	 * ================================
	 * Status        |               xx
	 * Tot. Branches |        xxxxxxx
	 * Mis. Branches | xxxxxxx
	 * 
	 * </pre>
	 */
	private char[] status;

	private static final char MASK_STATUS = 0x0003;
	private static final char MASK_TOTBR = 0x01fc;
	private static final char OFFSET_TOTBR = 2;
	private static final char MASK_MISBR = 0xfe00;
	private static final char OFFSET_MISBR = 9;

	/** first line number in lines */
	private int offset;

	/**
	 * Creates an empty line counter.
	 */
	public LinesImpl() {
		super(0, 0);
		status = null;
		offset = -1;
	}

	private void ensureCapacity(final int first, final int last) {
		if (status == null) {
			offset = first;
			status = new char[last - first + 1];
		} else {
			final int newFirst = Math.min(getFirstLine(), first);
			final int newLast = Math.max(getLastLine(), last);
			final int newLength = newLast - newFirst + 1;
			if (newLength > status.length) {
				final char[] newStatus = new char[newLength];
				System.arraycopy(status, 0, newStatus, offset - newFirst,
						status.length);
				offset = newFirst;
				status = newStatus;
			}
		}
	}

	/**
	 * Adds a instruction for the given line.
	 * 
	 * @param line
	 *            line number to add
	 * @param covered
	 *            <code>true</code> if the line is covered
	 * 
	 */
	public void incrementInsn(final int line, final boolean covered) {
		ensureCapacity(line, line);
		final byte newStatus = covered ? FULLY_COVERED : NOT_COVERED;
		increment(line, newStatus, 0, 0);
	}

	/**
	 * Adds the lines of the given line counter. The states are updated
	 * accordingly.
	 * 
	 * @param counter
	 *            line counter to add
	 */
	public void increment(final ILines counter) {
		if (counter.getFirstLine() == -1) {
			return;
		}
		ensureCapacity(counter.getFirstLine(), counter.getLastLine());
		for (int line = counter.getFirstLine(); line <= counter.getLastLine(); line++) {
			increment(line, counter.getStatus(line),
					counter.getTotalBranches(line),
					counter.getMissedBranches(line));
		}
	}

	private void increment(final int line, final byte newStatus,
			final int totalBranches, final int missedBranches) {
		final int s = status[line - offset];
		final int oldStatus = s & MASK_STATUS;
		final int oldTotbr = s & MASK_TOTBR;
		final int oldCovbr = s & MASK_MISBR;
		final int newTotbr = Math.min(MASK_TOTBR, oldTotbr
				+ (totalBranches << OFFSET_TOTBR));
		final int newCovbr = Math.min(MASK_MISBR, oldCovbr
				+ (missedBranches << OFFSET_MISBR));
		status[line - offset] = (char) (oldStatus | newStatus | newTotbr | newCovbr);
		if (oldStatus == NO_CODE && newStatus != NO_CODE) {
			total++;
		}
		if ((oldStatus == NO_CODE || oldStatus == NOT_COVERED)
				&& (newStatus == PARTLY_COVERED || newStatus == FULLY_COVERED)) {
			covered++;
		}
	}

	/**
	 * Add branches to the given line
	 * 
	 * @param line
	 *            line to add branches to
	 * @param totalBranches
	 *            number of total branches to add
	 * @param coveredBranches
	 *            number of covered branches to add
	 */
	public void incrementBranches(final int line, final int totalBranches,
			final int coveredBranches) {
		ensureCapacity(line, line);
		increment(line, NO_CODE, totalBranches, totalBranches - coveredBranches);
	}

	// === ILineCounter ===

	public int getFirstLine() {
		return offset;
	}

	public int getLastLine() {
		return status == null ? -1 : (offset + status.length - 1);
	}

	public byte getStatus(final int line) {
		if (status == null || line < getFirstLine() || line > getLastLine()) {
			return NO_CODE;
		}
		return (byte) (status[line - offset] & MASK_STATUS);
	}

	public int getTotalBranches(final int line) {
		if (status == null || line < getFirstLine() || line > getLastLine()) {
			return 0;
		}
		return (status[line - offset] & MASK_TOTBR) >> OFFSET_TOTBR;
	}

	public int getMissedBranches(final int line) {
		if (status == null || line < getFirstLine() || line > getLastLine()) {
			return 0;
		}
		return (status[line - offset] & MASK_MISBR) >> OFFSET_MISBR;
	}

	public int getCoveredBranches(final int line) {
		return getTotalBranches(line) - getMissedBranches(line);
	}

}
