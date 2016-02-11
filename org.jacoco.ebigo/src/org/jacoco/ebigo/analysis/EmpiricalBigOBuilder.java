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
package org.jacoco.ebigo.analysis;

import static org.jacoco.ebigo.internal.util.ValidationUtils.validateNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.ebigo.core.WorkloadAttributeMapBuilder;
import org.jacoco.ebigo.fit.FitType;

/**
 * Builder for Empirical Big-O analysis results, which contain both coverage
 * information and Big-O information. The results are feed into the builder
 * through its {@link IEmpiricalBigOVisitor} interface. Afterwards the
 * aggregated data can be obtained from the {@code #getCoverageBuilder}.
 * 
 * @author Omer Azmon
 */
public class EmpiricalBigOBuilder extends CoverageBuilder implements
		IEmpiricalBigOVisitor {

	private final FitType[] fitTypes;
	private final String attributeName;
	private XAxisValues xAxisValues;

	/**
	 * Create a new builder with the default attribute and all supported fit
	 * types.
	 */
	public EmpiricalBigOBuilder() {
		this(null, null);
	}

	/**
	 * Create a new builder.
	 * 
	 * @param attributeName
	 *            X-Axis attribute that will be used for this analysis
	 * @param fitTypes
	 *            The fit types to consider for this analysis.
	 */
	public EmpiricalBigOBuilder(final FitType[] fitTypes,
			final String attributeName) {
		this.fitTypes = fitTypes != null && fitTypes.length != 0 ? fitTypes
				: FitType.values();
		this.attributeName = attributeName != null ? attributeName
				: WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE;
		this.xAxisValues = null;
	}

	/**
	 * Returns the fits types that are considered for this analysis
	 * 
	 * @return the fits types that are considered for this analysis
	 */
	public FitType[] getFitTypes() {
		return fitTypes;
	}

	/**
	 * Returns the name of the X-Axis attribute considered for this analysis.
	 * 
	 * @return the name of the X-Axis attribute considered for this analysis.
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * Returns the X-Axis values extracted from the workloads, that are
	 * associated with the attribute name used for this analysis.
	 * 
	 * @return the X-Axis values extracted from the workloads.
	 */
	public XAxisValues getXAxisValues() {
		return xAxisValues;
	}

	// Used by mergeMatchedClassCoverage to merge MethodCoverage in the
	// ClassCoverage
	private static class MethodMap {
		private final Map<String, MethodCoverageImpl> methodMap;

		public MethodMap(Collection<IMethodCoverage> methods) {
			methodMap = new HashMap<String, MethodCoverageImpl>();
			for (IMethodCoverage method : methods) {
				final String key = method.getName() + method.getDesc();
				methodMap.put(key, (MethodCoverageImpl) method);
			}
		}

		private void merge(IMethodCoverage methodCoverage) {
			final String key = methodCoverage.getName()
					+ methodCoverage.getDesc();
			MethodCoverageImpl exists = methodMap.get(key);
			if (exists == null) {
				methodMap.put(key, (MethodCoverageImpl) methodCoverage);
			} else {
				exists.increment(methodCoverage);
			}
		}

		public void merge(IClassCoverage classCoverage) {
			for (IMethodCoverage methodCoverage : classCoverage.getMethods()) {
				merge(methodCoverage);
			}
		}
	}

	private IClassCoverage mergeMatchedClassCoverage(
			final IClassCoverage[] matchedCoverage) {
		final ClassCoverageImpl mergedCoverage = (ClassCoverageImpl) matchedCoverage[0];
		final MethodMap resultMethodMap = new MethodMap(
				mergedCoverage.getMethods());
		for (int idx = 1; idx < matchedCoverage.length; idx++) {
			mergedCoverage.increment(matchedCoverage[idx]);
			resultMethodMap.merge(matchedCoverage[idx]);
		}
		return mergedCoverage;
	}

	// === IEmpiricalBigOVisitor ===

	public void visitEmpiricalBigO(final IClassEmpiricalBigO empiricalBigO) {
		validateNotNull("empiricalBigO", empiricalBigO);

		final IClassCoverage[] matchedClassCoverage = empiricalBigO
				.getMatchedCoverageClasses();
		if (matchedClassCoverage != null && matchedClassCoverage.length > 0) {
			visitCoverage(mergeMatchedClassCoverage(matchedClassCoverage));
		}
	}

	public void visitXAxis(final XAxisValues xAxisValues) {
		if (xAxisValues == null) {
			throw new IllegalArgumentException("X-Axis value is null");
		}
		if (this.xAxisValues != null) {
			throw new IllegalStateException("X-Axis has already been visited");
		}
		this.xAxisValues = xAxisValues;
	}
}
