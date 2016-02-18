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
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;

import org.jacoco.core.data.ProbeMode;
import org.junit.Test;

public class InstrumentationConfigTest {
	@Test
	public void defaultConstructor() throws Exception {
		Constructor<InstrumentationConfig> c = InstrumentationConfig.class
				.getDeclaredConstructor();
		c.setAccessible(true);
		c.newInstance();
	}

	@Test
	public void defaultProbeMode() {
		InstrumentationConfig.reset();
		ProbeMode result = InstrumentationConfig.getProbeMode();
		assertEquals(ProbeMode.exists, result);
	}

	@Test
	public void overrideProbeMode() {
		InstrumentationConfig.reset();
		InstrumentationConfig.configure(ProbeMode.parallelcount);
		ProbeMode result = InstrumentationConfig.getProbeMode();
		assertEquals(ProbeMode.parallelcount, result);
	}

	@Test(expected = IllegalStateException.class)
	public void badOverrideProbeMode() {
		InstrumentationConfig.reset();
		InstrumentationConfig.configure(ProbeMode.exists);
		InstrumentationConfig.configure(ProbeMode.parallelcount);
	}

}
