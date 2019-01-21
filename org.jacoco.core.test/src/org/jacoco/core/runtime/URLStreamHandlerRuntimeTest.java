/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
