/************************************************************************
   Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
   All rights reserved. This program and the accompanying materials
   are made available under the terms of the Eclipse Public License v1.0
   which accompanies this distribution, and is available at
   http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.jacoco.examples;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author Mirko Friedenhagen
 */
public class MyAppTest {

	/**
	 * Test of viaUnitTest method, of class MyApp.
	 */
	@Test
	public void testViaUnitTest() {
		MyApp sut = new MyApp();
		assertTrue(sut.viaUnitTest());
	}
}
