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

import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.ExceptionsTarget;

/**
 * Tests of exception based control flow.
 */
public class ExceptionsTest extends ValidationTestBase {

	public ExceptionsTest() {
		super(ExceptionsTarget.class);
	}

	public void assertCatchNoException(final Line line) {
		if (isJDKCompiler) {
			assertNotCovered(line);
		} else {
			assertPartlyCovered(line);
		}
	}

	public void assertCatchBlockEndNoException(final Line line) {
		if (isJDKCompiler) {
			assertFullyCovered(line);
		} else {
			assertEmpty(line);
		}
	}

	public void assertCatchImplicitException(final Line line) {
		if (isJDKCompiler) {
			assertFullyCovered(line);
		} else {
			assertPartlyCovered(line);
		}
	}

	public void assertCatchBlockEndImplicitException(final Line line) {
		if (isJDKCompiler) {
			assertNotCovered(line);
		} else {
			assertEmpty(line);
		}
	}

	public void assertFinally(final Line line) {
		if (isJDKCompiler) {
			assertEmpty(line);
		} else {
			assertFullyCovered(line);
		}
	}

	public void assertFinallyImplicitException(final Line line) {
		if (isJDKCompiler) {
			assertEmpty(line);
		} else {
			assertNotCovered(line);
		}
	}

	public void assertBlockEndImplicitException(final Line line) {
		if (isJDKCompiler) {
			assertEmpty(line);
		} else {
			assertFullyCovered(line);
		}
	}

}
