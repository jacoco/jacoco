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
package org.jacoco.ebigo.fit;

/**
 * The curve type to which the fitting is done
 * 
 * @author Omer Azmon
 */
public enum FitType {
	// In Preference Order (in case two have same match)
	Log(true, false), Linear(false, false), PowerLaw(true, true), Exp(false,
			true);

	private boolean doLogX;
	private boolean doLogY;

	FitType(boolean doLogX, boolean doLogY) {
		this.doLogX = doLogX;
		this.doLogY = doLogY;
	}

	/**
	 * Should the X axis be logarithmic.to make this fit linear.
	 * 
	 * @return <code>true</code> if logarithmic; <code>false</code> if linear.
	 */
	public boolean isDoLogX() {
		return doLogX;
	}

	/**
	 * Should the Y axis be logarithmic.to make this fit linear.
	 * 
	 * @return <code>true</code> if logarithmic; <code>false</code> if linear.
	 */
	public boolean isDoLogY() {
		return doLogY;
	}
}
