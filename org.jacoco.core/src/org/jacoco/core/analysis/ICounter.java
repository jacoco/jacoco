/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
 * A counter holds the total and the covered number of particular items like
 * classes, methods, blocks or instructions. Counters provide canonical
 * comparison of their covered/total ratios.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: 11 $
 */
public interface ICounter {

	/**
	 * Returns the total count of instrumented items.
	 * 
	 * @return total count of instrumented items
	 */
	public int getTotalCount();

	/**
	 * Returns the total count of covered items.
	 * 
	 * @return total count of covered items
	 */
	public int getCoveredCount();

	/**
	 * Returns the total count of missed items.
	 * 
	 * @return total count of missed items
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

}
