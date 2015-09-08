/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Constructor;

import org.junit.Test;

/**
 * Unit tests for {@link JaCoCo}.
 */
public class JaCoCoTest {

	@Test
	public void testConstructor() throws Exception {
		Constructor<JaCoCo> constructor = JaCoCo.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
		// all's well if no exceptions thrown so far
	}

	@Test
	public void testVERSION() {
		assertNotNull(JaCoCo.VERSION);
	}

	@Test
	public void testHOMEURL() {
		assertNotNull(JaCoCo.HOMEURL);
	}

	@Test
	public void testRUNTIMEPACKAGE() {
		assertNotNull(JaCoCo.RUNTIMEPACKAGE);
	}

}
