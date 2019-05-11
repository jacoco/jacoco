/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test;

import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 * Unit test for {@link InstrumentingLoader}.
 */
public class InstrumentingLoaderTest {

	@Test
	public void should_use_findLoadedClass() throws Exception {
		final InstrumentingLoader loader = new InstrumentingLoader(
				InstrumentingLoaderTest.class);
		final Class<?> c1 = loader
				.loadClass(InstrumentingLoaderTest.class.getName());
		final Class<?> c2 = loader
				.loadClass(InstrumentingLoaderTest.class.getName());
		assertSame(c1, c2);
	}

}
