/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.runtime;

import org.junit.Assume;
import org.junit.BeforeClass;

/**
 * Unit tests for {@link URLStreamHandlerRuntime}.
 */
public class URLStreamHandlerRuntimeTest extends RuntimeTestBase {

	@Override
	IRuntime createRuntime() {
		return new URLStreamHandlerRuntime();
	}

	@BeforeClass
	public static void checkJDK() {
		final boolean jdk9 = System.getProperty("java.version")
				.startsWith("9-");
		Assume.assumeTrue(!jdk9);
	}

}
