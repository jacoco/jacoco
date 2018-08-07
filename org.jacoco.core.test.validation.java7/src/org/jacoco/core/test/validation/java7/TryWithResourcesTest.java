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
package org.jacoco.core.test.validation.java7;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java7.targets.TryWithResourcesTarget;

/**
 * Test of filtering of a bytecode that is generated for a try-with-resources
 * statement.
 */
public class TryWithResourcesTest extends ValidationTestBase {

	public TryWithResourcesTest() {
		super(TryWithResourcesTarget.class);
	}

	public void assertTry(int nr) {
		// without filter this line is covered partly:
		if (!isJDKCompiler || JAVA_VERSION.isBefore("11")) {
			assertFullyCovered(nr);
		} else {
			assertEmpty(nr);
		}
	}

	public void assertReturnInBodyClose(int nr) {
		// without filter next line has branches:
		if (isJDKCompiler) {
			// https://bugs.openjdk.java.net/browse/JDK-8134759
			// javac 7 and 8 up to 8u92 are affected
			if (JAVA_VERSION.isBefore("1.8.0_92")) {
				assertFullyCovered(nr);
			} else {
				assertEmpty(nr);
			}
		} else {
			assertEmpty(nr);
		}
	}

	public void assertHandwritten(int nr) {
		if (isJDKCompiler) {
			assertEmpty(nr);
		} else {
			assertFullyCovered(nr);
		}
	}

	public void assertEmptyClose(int nr) {
		if (!isJDKCompiler) {
			assertPartlyCovered(nr, 7, 1);
		} else if (JAVA_VERSION.isBefore("8")) {
			assertPartlyCovered(nr, 6, 2);
		} else if (JAVA_VERSION.isBefore("9")) {
			assertPartlyCovered(nr, 2, 2);
		} else {
			assertFullyCovered(nr);
		}
	}

	public void assertThrowInBodyClose(int nr) {
		// not filtered
		if (!isJDKCompiler) {
			assertNotCovered(nr, 6, 0);
		} else if (JAVA_VERSION.isBefore("9")) {
			assertNotCovered(nr, 4, 0);
		} else if (JAVA_VERSION.isBefore("11")) {
			assertNotCovered(nr);
		} else {
			assertEmpty(nr);
		}
	}

}
