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
package org.jacoco.ebigo.internal.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.ebigo.internal.util.SortMergeIterator;

/**
 * Helps the Empirical Big-O analysis walk through multiple coverage analysis in
 * lock-step.
 * 
 * @author Omer Azmon
 */
public class MethodCoverageSetIterator extends
		SortMergeIterator<IMethodCoverage> {
	private static class IMethodCoverageComparator implements
			Comparator<IMethodCoverage> {

		public int compare(final IMethodCoverage left, final IMethodCoverage right) {
			if(left == null) {
				return right == null ? 0 : -1;
			}
			if(right == null) {
				return 1;
			}
			final int namesCompare = left.getName().compareTo(right.getName());
			if (namesCompare != 0) {
				return namesCompare;
			}
			return left.getDesc().compareTo(right.getDesc());
		}

	}

	private static List<Collection<IMethodCoverage>> getMethodSet(
			final IClassCoverage[] classCoverageSet) {
		final List<Collection<IMethodCoverage>> methodSet = new ArrayList<Collection<IMethodCoverage>>();
		for(final IClassCoverage classCoverage : classCoverageSet) {
			methodSet.add(classCoverage.getMethods());
		}
		return methodSet;
	}

	public MethodCoverageSetIterator(final IClassCoverage[] classCoverageSet) {
		super(new IMethodCoverageComparator(), getMethodSet(classCoverageSet));
	}
}