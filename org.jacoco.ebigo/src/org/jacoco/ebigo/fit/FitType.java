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
package org.jacoco.ebigo.fit;

import org.jacoco.core.analysis.EBigOFunction;

/**
 * The curve type to which the fitting is done
 * 
 * @author Omer Azmon
 */
public enum FitType {
	// In Preference Order (in case two have same match)
	Logarithmic(EBigOFunction.Type.Logarithmic, true, false), //
	Linear(EBigOFunction.Type.Linear, false, false), //
	PowerLaw(EBigOFunction.Type.PowerLaw, true, true), //
	Exponential(EBigOFunction.Type.Exponential, false, true);

	private final EBigOFunction.Type type;
	private final boolean doLogX;
	private final boolean doLogY;

	FitType(EBigOFunction.Type type, boolean doLogX, boolean doLogY) {
		this.type = type;
		this.doLogX = doLogX;
		this.doLogY = doLogY;
	}

	/**
	 * Should the X axis be logarithmic.to make this fit linear.
	 * 
	 * @return <code>true</code> if logarithmic; <code>false</code> if linear.
	 */
	public final boolean isDoLogX() {
		return doLogX;
	}

	/**
	 * Should the Y axis be logarithmic.to make this fit linear.
	 * 
	 * @return <code>true</code> if logarithmic; <code>false</code> if linear.
	 */
	public final boolean isDoLogY() {
		return doLogY;
	}

	/**
	 * Get the E-Big-O function type associated with this fit.
	 * 
	 * @return the E-Big-O function type associated with this fit.
	 */
	public final EBigOFunction.Type getType() {
		return this.type;
	}
}
