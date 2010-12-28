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
 * Base class for {@link ICounter} implementations.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public abstract class AbstractCounter implements ICounter {

	/** number of missed items */
	protected int missed;

	/** number of covered items */
	protected int covered;

	/**
	 * Creates a instance with the given numbers.
	 * 
	 * @param missed
	 *            number of missed items
	 * @param covered
	 *            number of covered items
	 */
	protected AbstractCounter(final int missed, final int covered) {
		this.missed = missed;
		this.covered = covered;
	}

	public int getTotalCount() {
		return missed + covered;
	}

	public int getCoveredCount() {
		return covered;
	}

	public int getMissedCount() {
		return missed;
	}

	public double getCoveredRatio() {
		return (double) covered / (missed + covered);
	}

	public double getMissedRatio() {
		return (double) missed / (missed + covered);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ICounter) {
			final ICounter that = (ICounter) obj;
			return this.missed == that.getMissedCount()
					&& this.covered == that.getCoveredCount();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return missed ^ covered * 17;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("Counter["); //$NON-NLS-1$
		b.append(getMissedCount());
		b.append('/').append(getCoveredCount());
		b.append(']');
		return b.toString();
	}

}