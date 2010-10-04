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

	/** status for each line */
	private byte[] status;

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

	/**
	 * Created a line counter with the given lines
	 * 
	 * @param lines
	 *            line numbers to add
	 * @param covered
	 *            <code>true</code> if all lines are covered
	 * 
	 */
	public LinesImpl(final int[] lines, final boolean covered) {
		this();
		increment(lines, covered);
	}

	private void ensureCapacity(final int first, final int last) {
		if (status == null) {
			offset = first;
			status = new byte[last - first + 1];
		} else {
			final int newFirst = Math.min(getFirstLine(), first);
			final int newLast = Math.max(getLastLine(), last);
			final int newLength = newLast - newFirst + 1;
			if (newLength > status.length) {
				final byte[] newStatus = new byte[newLength];
				System.arraycopy(status, 0, newStatus, offset - newFirst,
						status.length);
				offset = newFirst;
				status = newStatus;
			}
		}
	}

	/**
	 * Adds the given lines as fully covered or not covered.
	 * 
	 * @param lines
	 *            line numbers to add
	 * @param covered
	 *            <code>true</code> if all lines are covered
	 * 
	 */
	public void increment(final int[] lines, final boolean covered) {
		if (lines.length > 0) {
			ensureCapacity(lines[0], lines[lines.length - 1]);
			final byte newStatus = covered ? FULLY_COVERED : NOT_COVERED;
			for (final int line : lines) {
				incrementLine(line, newStatus);
			}
		}
	}

	/**
	 * Adds the lines of the given line counter. The states are updated
	 * accordingly.
	 * 
	 * @param counter
	 *            line counter to add
	 */
	public void increment(final ILines counter) {
		if (counter.getTotalCount() == 0) {
			return;
		}
		ensureCapacity(counter.getFirstLine(), counter.getLastLine());
		for (int line = counter.getFirstLine(); line <= counter.getLastLine(); line++) {
			incrementLine(line, counter.getStatus(line));
		}
	}

	private void incrementLine(final int line, final byte newStatus) {
		final byte oldStatus = status[line - offset];
		status[line - offset] = (byte) (oldStatus | newStatus);
		if (oldStatus == NO_CODE && newStatus != NO_CODE) {
			total++;
		}
		if ((oldStatus == NO_CODE || oldStatus == NOT_COVERED)
				&& (newStatus == PARTLY_COVERED || newStatus == FULLY_COVERED)) {
			covered++;
		}
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
		return status[line - offset];
	}

}
