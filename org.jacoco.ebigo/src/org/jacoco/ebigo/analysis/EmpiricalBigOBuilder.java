/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.ebigo.fit.FitType;

/**
 * Builder for Empirical Big-O analysis results, which contain both coverage
 * information and Big-O information. The results are feed into the builder
 * through its {@link IEmpiricalBigOVisitor} interface. Afterwards the
 * aggregated data can be obtained with {@link #getClasses()}.
 * 
 * @author Omer Azmon
 */
public class EmpiricalBigOBuilder implements IEmpiricalBigOVisitor {

	private final Map<String, IClassEmpiricalBigO> classes;
	private final FitType[] fitTypes;
	private final String attributeName;
	private XAxisValues xAxisValues;

	/**
	 * Create a new builder.
	 * 
	 * @param attributeName
	 *            X-Axis attribute that will be used for this analysis
	 * @param fitTypes
	 *            The fit types to consider for this analysis.
	 * 
	 */
	public EmpiricalBigOBuilder(final FitType[] fitTypes,
			final String attributeName) {
		validateNotNull("fitTypes", fitTypes);
		if (fitTypes.length == 0) {
			throw new IllegalArgumentException(
					"Must specify at least one fit type");
		}
		validateNotNull("attributeName", attributeName);
		
		this.classes = new HashMap<String, IClassEmpiricalBigO>();
		this.fitTypes = fitTypes;
		this.attributeName = attributeName;
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

	/**
	 * Returns all empirical Big-O analysis class nodes currently contained in
	 * this builder.
	 * 
	 * @return all class nodes
	 */
	public Collection<IClassEmpiricalBigO> getClasses() {
		return Collections.unmodifiableCollection(classes.values());
	}

	// TODO: see if we need to provide a shortcut to get nomatch, and bundles.

	// === IEmpiricalBigOVisitor ===

	public void visitEmpiricalBigO(final IClassEmpiricalBigO empiricalBigO) {
		validateNotNull("empiricalBigO", empiricalBigO);
		final IClassCoverage[] coverages = empiricalBigO.getMatchedCoverageClasses();
		if(coverages.length == 0) {
			return;
		}
		final IClassCoverage coverage = coverages[0];
		final String name = coverage.getName();
		final IClassEmpiricalBigO dup = classes.put(name, empiricalBigO);
		if (dup != null
				&& dup.getMatchedCoverageClasses()[0].getId() != coverage
						.getId()) {
			// Should never happen as the CoverageBuilder before us that created
			// ccs[0] should have filter those out
			throw new IllegalArgumentException(
					"Can't add different class with same name: " + name);
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
