/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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
 * A counter holds the missed and the covered number of particular items like
 * classes, methods, branches or instructions.
 */
public interface ICounter {

	/**
	 * Different values provided by a counter.
	 */
	public enum CounterValue {

		/** Total number of items */
		TOTALCOUNT,

		/** Number of missed items */
		MISSEDCOUNT,

		/** Number of covered items */
		COVEREDCOUNT,

		/** Ratio of missed to total items */
		MISSEDRATIO,

		/** Ratio of covered to total items */
		COVEREDRATIO
	}

	/**
	 * Status flag for no items (value is 0x00).
	 */
	public static final int EMPTY = 0x00;

	/**
	 * Status flag when all items are not covered (value is 0x01).
	 */
	public static final int NOT_COVERED = 0x01;

	/**
	 * Status flag when all items are covered (value is 0x02).
	 */
	public static final int FULLY_COVERED = 0x02;

	/**
	 * Status flag when items are partly covered (value is 0x03).
	 */
	public static final int PARTLY_COVERED = NOT_COVERED | FULLY_COVERED;

	/**
	 * Returns the counter value of the given type.
	 * 
	 * @param value
	 *            value type to return
	 * @return counter value
	 */
	public double getValue(CounterValue value);

	/**
	 * Returns the total count of items.
	 * 
	 * @return total count of items
	 */
	public int getTotalCount();

	/**
	 * Returns the count of covered items.
	 * 
	 * @return count of covered items
	 */
	public int getCoveredCount();

	/**
	 * Returns the count of missed items.
	 * 
	 * @return count of missed items
	 */
	public int getMissedCount();

	/**
	 * Calculates the ratio of covered to total count items. If total count
	 * items is 0 this method returns NaN.
	 * 
	 * @return ratio of covered to total count items
	 */
	public double getCoveredRatio();

	/**
	 * Calculates the ratio of missed to total count items. If total count items
	 * is 0 this method returns NaN.
	 * 
	 * @return ratio of missed to total count items
	 */
	public double getMissedRatio();

	/**
	 * Returns the coverage status of this counter.
	 * 
	 * @see ICounter#EMPTY
	 * @see ICounter#NOT_COVERED
	 * @see ICounter#PARTLY_COVERED
	 * @see ICounter#FULLY_COVERED
	 * 
	 * @return status of this line
	 */
	public int getStatus();

	/**
	 * Get a counter that indicates that all the branches in this counter have
	 * been covered, and nothing has been missed.
	 * 
	 * @return Counter with same number of branches as this counter.
	 */
	public ICounter treatAsFullyCovered();

}
