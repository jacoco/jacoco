/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
