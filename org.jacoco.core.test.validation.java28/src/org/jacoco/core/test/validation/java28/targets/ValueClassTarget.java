/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java28.targets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <a href="https://openjdk.org/jeps/401">JEP 401: Value Classes and Objects
 * (Preview)</a>
 *
 * <a href= "https://cr.openjdk.org/~dlsmith/jep401/latest">Draft of changes to
 * JLS and JVMS</a>
 *
 * <a href="https://openjdk.org/jeps/539">JEP 539: Strict Field Initialization
 * in the JVM (Preview)</a>
 *
 * <a href= "https://cr.openjdk.org/~dlsmith/jep539/latest">Draft of changes to
 * JVMS</a>
 */
public class ValueClassTarget {

	/* @formatter:off */

	/**
	 * Contains {@code early_larval_frame}.
	 */
	private static value class S {
		private final int v; // assertEmpty()
		S(int v) { // assertEmpty()
			if (v < 0) { // assertEmpty()
				this.v = -v; // assertEmpty()
			} else { // assertEmpty()
				this.v = v; // assertEmpty()
			} // assertEmpty()
		} // assertEmpty()
	}

	/**
	 * Contains strictly-initialized field, but no {@code early_larval_frame}.
	 */
	private static value class C { // assertEmpty()
		private final int v; // assertEmpty()
		C(int v) { // assertFullyCovered()
			this.v = v; // assertFullyCovered()
		} // assertFullyCovered()
	} // assertEmpty()

	private value record R(int v) { // assertFullyCovered()
	} // assertEmpty()

	/* @formatter:on */

	public static void main(String[] args) throws Exception {
		/*
		 * In contrast to this validation test, when the JaCoCo agent fails to
		 * instrument the class, NoClassDefFoundError is not thrown. Instead the
		 * original non-instrumented class is executed after the root cause is
		 * logged. However report generation will fail with the same root cause,
		 * which can be worked around by excluding the class from analysis.
		 */
		try {
			new S(1);
			fail("NoClassDefFoundError expected");
		} catch (final NoClassDefFoundError e) {
			assertEquals("Unable to instrument", e.getCause().getMessage());
			final Throwable rootCause = e.getCause().getCause().getCause();
			assertEquals(IllegalArgumentException.class, rootCause.getClass());
			assertNull(rootCause.getCause());
			final StackTraceElement stackTraceTop = rootCause
					.getStackTrace()[0];
			assertEquals("org.objectweb.asm.ClassReader",
					stackTraceTop.getClassName());
			assertEquals("readStackMapFrame", stackTraceTop.getMethodName());
			/*
			 * "https://gitlab.ow2.org/asm/asm/-/blob/ASM_9_10_1/asm/src/main/java/org/objectweb/asm/ClassReader.java?ref_type=tags#L3364"
			 */
			assertEquals(3364, stackTraceTop.getLineNumber());
		}

		new C(1);
		assertTrue(C.class.isValue());
		assertTrue(C.class.getDeclaredField("v").isStrictInit());

		new R(1);
		assertTrue(R.class.isValue());
		assertTrue(R.class.isRecord());
		assertTrue(R.class.getDeclaredField("v").isStrictInit());
	}

}
