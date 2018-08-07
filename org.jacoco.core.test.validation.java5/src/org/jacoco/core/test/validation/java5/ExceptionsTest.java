/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation.java5;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.ExceptionsTarget;

/**
 * Tests of exception based control flow.
 */
public class ExceptionsTest extends ValidationTestBase {

	public ExceptionsTest() {
		super(ExceptionsTarget.class);
	}

	public void assertCatchNoException(int nr) {
		if (isJDKCompiler) {
			assertNotCovered(nr);
		} else {
			assertPartlyCovered(nr);
		}
	}

	public void assertCatchBlockEndNoException(int nr) {
		if (isJDKCompiler) {
			assertFullyCovered(nr);
		} else {
			assertEmpty(nr);
		}
	}

	public void assertCatchImplicitException(int nr) {
		if (isJDKCompiler) {
			assertFullyCovered(nr);
		} else {
			assertPartlyCovered(nr);
		}
	}

	public void assertCatchBlockEndImplicitException(int nr) {
		if (isJDKCompiler) {
			assertNotCovered(nr);
		} else {
			assertEmpty(nr);
		}
	}

	public void assertFinally(int nr) {
		if (isJDKCompiler) {
			assertEmpty(nr);
		} else {
			assertFullyCovered(nr);
		}
	}

	public void assertFinallyImplicitException(int nr) {
		if (isJDKCompiler) {
			assertEmpty(nr);
		} else {
			assertNotCovered(nr);
		}
	}

	public void assertBlockEndImplicitException(int nr) {
		if (isJDKCompiler) {
			assertEmpty(nr);
		} else {
			assertFullyCovered(nr);
		}
	}

}
