/*******************************************************************************
 *  Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.fit;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * Sort Set of @{code Fit} objects in descending confidence value.
 * 
 * @author Omer Azmon
 */
public class SortedFitSet extends TreeSet<Fit> {
	private static final long serialVersionUID = -4550560927458489722L;

	/**
	 * Used to sort the fits from best to worst according to each sort's
	 * confidence.
	 */
	private static class FitConfidenceComparator implements Comparator<Fit> {
		public int compare(final Fit left, final Fit right) {
			if (left.confidence != right.confidence) {
				// ascending
				return (int) Math.signum(left.confidence - right.confidence);
			}
			// ascending
			return left.type.ordinal() - right.type.ordinal();
		}

	}

	public SortedFitSet() {
		super(new FitConfidenceComparator());
	}

}
