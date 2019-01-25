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
package org.jacoco.core.test.validation.java7;

import org.jacoco.core.test.validation.Source.Line;
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

	public void assertTry(final Line line) {
		// without filter this line is covered partly:
		if (!isJDKCompiler || JAVA_VERSION.isBefore("11")) {
			assertFullyCovered(line);
		} else {
			assertEmpty(line);
		}
	}

	public void assertReturnInBodyClose(final Line line) {
		// without filter next line has branches:
		if (isJDKCompiler) {
			// https://bugs.openjdk.java.net/browse/JDK-8134759
			// javac 7 and 8 up to 8u92 are affected
			if (JAVA_VERSION.isBefore("1.8.0_92")) {
				assertFullyCovered(line);
			} else {
				assertEmpty(line);
			}
		} else {
			assertEmpty(line);
		}
	}

	public void assertHandwritten(final Line line) {
		if (isJDKCompiler) {
			assertEmpty(line);
		} else {
			assertFullyCovered(line, 1, 1);
		}
	}

	public void assertEmptyClose(final Line line) {
		if (!isJDKCompiler) {
			assertPartlyCovered(line, 7, 1);
		} else if (JAVA_VERSION.isBefore("8")) {
			assertPartlyCovered(line, 6, 2);
		} else if (JAVA_VERSION.isBefore("9")) {
			assertPartlyCovered(line, 2, 2);
		} else {
			assertFullyCovered(line);
		}
	}

	public void assertThrowInBodyClose(final Line line) {
		// not filtered
		if (!isJDKCompiler) {
			assertNotCovered(line, 6, 0);
		} else if (JAVA_VERSION.isBefore("9")) {
			assertNotCovered(line, 4, 0);
		} else if (JAVA_VERSION.isBefore("11")) {
			assertNotCovered(line);
		} else {
			assertEmpty(line);
		}
	}

}
