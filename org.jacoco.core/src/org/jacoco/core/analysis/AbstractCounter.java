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
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.analysis;

/**
 * Base class for {@link ICounter} implementations.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public abstract class AbstractCounter implements ICounter {

	/** total number of items */
	protected int total;

	/** covered number of items */
	protected int covered;

	/**
	 * Creates a instance with the given numbers.
	 * 
	 * @param total
	 *            number of total items
	 * @param covered
	 *            number of covered items
	 */
	protected AbstractCounter(final int total, final int covered) {
		this.total = total;
		this.covered = covered;
	}

	public int getTotalCount() {
		return total;
	}

	public int getCoveredCount() {
		return covered;
	}

	public int getMissedCount() {
		return total - covered;
	}

	public double getCoveredRatio() {
		return (double) covered / total;
	}

	public double getMissedRatio() {
		return (double) (total - covered) / total;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ICounter) {
			final ICounter that = (ICounter) obj;
			return this.total == that.getTotalCount()
					&& this.covered == that.getCoveredCount();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return total ^ covered * 17;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("Counter["); //$NON-NLS-1$
		b.append(getCoveredCount());
		b.append('/').append(getTotalCount());
		b.append(']');
		return b.toString();
	}

}