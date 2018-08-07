/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.SynchronizedTarget;

/**
 * Test of filtering of a bytecode that is generated for a synchronized
 * statement.
 */
public class SynchronizedTest extends ValidationTestBase {

	public SynchronizedTest() {
		super(SynchronizedTarget.class);
	}

	public void assertMonitorEnterImplicitException(int nr) {
		if (isJDKCompiler) {
			assertFullyCovered(nr);
		} else {
			assertPartlyCovered(nr);
		}
	}

	public void assertMonitorExit(int nr) {
		if (isJDKCompiler) {
			// without filter next line covered partly:
			assertFullyCovered(nr);
		} else {
			assertEmpty(nr);
		}
	}

	public void assertMonitorExitImplicitException(int nr) {
		if (isJDKCompiler) {
			// without filter next line covered partly:
			assertNotCovered(nr);
		} else {
			assertEmpty(nr);
		}
	}

}
