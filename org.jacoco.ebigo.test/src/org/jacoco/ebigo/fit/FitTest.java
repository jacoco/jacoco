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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FitTest {

	@Test
	public void testConstructor() {
		Fit fit = new Fit(FitType.PowerLaw);
		assertEquals(FitType.PowerLaw, fit.type);
	}

	@Test
	public void testToString() {
		Fit fit = new Fit(FitType.PowerLaw);
		assertEquals(
				"Fit [type=PowerLaw, n=0, slope=0.0, intercept=0.0, r-squared=0.0, confidence=0.0]",
				fit.toString());
	}
}