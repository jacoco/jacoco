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

import org.jacoco.ebigo.fit.FitType;

/**
 * Interface for empirical big-o data output as a stream of
 * {@link IClassEmpiricalBigO} instances.
 */
public interface IEmpiricalBigOVisitor {

	/**
	 * Returns the fit types expected by this visitor.
	 * 
	 * @return the fit types expected by this visitor.
	 */
	public FitType[] getFitTypes();

	/**
	 * Returns the name of the X-axis attribute name used to generate any fit
	 * data expected by this visitor.
	 * 
	 * @return the name of the X-axis attribute name
	 */
	public String getAttributeName();

	/**
	 * Analyzed X-axis values are emitted to this method. This method may be
	 * visited only once.
	 * 
	 * @param xAxisValues
	 *            the X-axis values used in the analysis of the classes whose
	 *            analysis is emitted using the @{code visitEmpiricalBigO}
	 *            method.
	 * 
	 * @throws IllegalStateException
	 *             if X-Axis has already been visited
	 */
	public void visitXAxis(final XAxisValues xAxisValues);

	/**
	 * Analyzed class empirical big-o data is emitted to this method.
	 * 
	 * @param classEmpiricalBigO
	 *            coverage data for a class
	 */
	public void visitEmpiricalBigO(final IClassEmpiricalBigO classEmpiricalBigO);

}
