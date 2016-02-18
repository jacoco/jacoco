/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.ebigo.internal.util.SortMergeIterator;

/**
 * Helps the Empirical Big-O analysis walk through multiple coverage analysis in
 * lock-step.
 * 
 * @author Omer Azmon
 */
public class ClassCoverageSetIterator extends SortMergeIterator<IClassCoverage> {
	private static class IClassCoverageComparator implements
			Comparator<IClassCoverage> {

		public int compare(final IClassCoverage left, final IClassCoverage right) {
			if(left == null) {
				return right == null ? 0 : -1;
			}
			if(right == null) {
				return 1;
			}
			return left.getName().compareTo(right.getName());
		}

	}

	private static List<Collection<IClassCoverage>> getClassSetList(
			final List<CoverageBuilder> analyzerList) {
		final List<Collection<IClassCoverage>> classSetList = new ArrayList<Collection<IClassCoverage>>();
		if (analyzerList != null) {
			for (final CoverageBuilder analyzer : analyzerList) {
				classSetList.add(analyzer.getClasses());
			}
		}
		return classSetList;
	}

	public ClassCoverageSetIterator(final List<CoverageBuilder> analyzerList) {
		super(new IClassCoverageComparator(), getClassSetList(analyzerList));
	}
}