/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.jacoco.core.analysis.ICoverageNode.CounterEntity;

/**
 * Used in the configuration of the "check" goal for specifying minimum rates of
 * coverage.
 */
public class CheckConfiguration {

	private double instructionRate;
	private double branchRate;
	private double lineRate;
	private double complexityRate;
	private double methodRate;
	private double classRate;

	/**
	 * Set the minimum allowed code coverage for instructions.
	 * 
	 * @param instructionRate
	 *            percent of instructions covered
	 */
	public void setInstructionRate(final double instructionRate) {
		this.instructionRate = instructionRate;
	}

	/**
	 * Set the minimum allowed code coverage for branches.
	 * 
	 * @param branchRate
	 *            percent of branches covered
	 */
	public void setBranchRate(final double branchRate) {
		this.branchRate = branchRate;
	}

	/**
	 * Set the minimum allowed code coverage for lines.
	 * 
	 * @param lineRate
	 *            percent of lines covered
	 */
	public void setLineRate(final double lineRate) {
		this.lineRate = lineRate;
	}

	/**
	 * Set the minimum allowed code coverage for complexity.
	 * 
	 * @param complexityRate
	 *            percent of complexities covered
	 */
	public void setComplexityRate(final double complexityRate) {
		this.complexityRate = complexityRate;
	}

	/**
	 * Set the minimum allowed code coverage for methods.
	 * 
	 * @param methodRate
	 *            percent of methods covered
	 */
	public void setMethodRate(final double methodRate) {
		this.methodRate = methodRate;
	}

	/**
	 * Set the minimum allowed code coverage for classes.
	 * 
	 * @param classRate
	 *            percent of classes covered
	 */
	public void setClassRate(final double classRate) {
		this.classRate = classRate;
	}

	/**
	 * Get the rate for the given CounterEntity
	 * 
	 * @param entity
	 *            the counter type
	 * @return minimum percent covered for given CounterEntity
	 */
	public double getRate(final CounterEntity entity) {
		double rate = 0;

		if (CounterEntity.INSTRUCTION.equals(entity)) {
			rate = this.instructionRate;
		} else if (CounterEntity.BRANCH.equals(entity)) {
			rate = this.branchRate;
		} else if (CounterEntity.LINE.equals(entity)) {
			rate = this.lineRate;
		} else if (CounterEntity.COMPLEXITY.equals(entity)) {
			rate = this.complexityRate;
		} else if (CounterEntity.METHOD.equals(entity)) {
			rate = this.methodRate;
		} else if (CounterEntity.CLASS.equals(entity)) {
			rate = this.classRate;
		}
		return rate;
	}
}
