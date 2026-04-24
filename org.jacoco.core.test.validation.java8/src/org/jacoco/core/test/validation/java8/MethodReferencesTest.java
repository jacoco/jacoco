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
package org.jacoco.core.test.validation.java8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;

import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.Source;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java8.targets.MethodReferencesTarget;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

/**
 * Test of code coverage in {@link MethodReferencesTarget}.
 */
public class MethodReferencesTest extends ValidationTestBase {

	public MethodReferencesTest() {
		super(MethodReferencesTarget.class);
	}

	private static int bytecodeVersion() throws IOException {
		return InstrSupport.getMajorVersion(
				TargetLoader.getClassDataAsBytes(MethodReferencesTarget.class));
	}

	public void assertMethodReferenceToPrivate(final Source.Line line)
			throws IOException {
		if (isJDKCompiler && bytecodeVersion() < Opcodes.V15) {
			assertPartlyCovered(line);
		} else {
			assertFullyCovered(line);
		}
	}

	public void assertMethodReferenceToArrayConstructor(
			final Source.Line line) {
		if (isJDKCompiler) {
			assertPartlyCovered(line);
		} else {
			assertFullyCovered(line);
		}
	}

	/**
	 * TODO report to ECJ that it generates LINENUMBER 1
	 */
	@Override
	public void first_line_in_coverage_data_should_be_greater_than_one() {
		if (isJDKCompiler) {
			super.first_line_in_coverage_data_should_be_greater_than_one();
		}
	}

	@Test
	public void compiler_should_generate_synthetic_lambdas()
			throws IOException {
		final HashSet<String> names = new HashSet<>();
		for (final Method method : MethodReferencesTarget.class
				.getDeclaredMethods()) {
			if (method.isSynthetic()) {
				names.add(method.getName());
			}
		}

		final HashSet<String> accessors = new HashSet<>();
		for (final Method method : MethodReferencesTarget.PrivateMethod.class
				.getDeclaredMethods()) {
			if (method.isSynthetic()) {
				accessors.add(method.getName());
			}
		}

		if (isJDKCompiler) {
			// references to array constructor
			assertTrue(names.contains("lambda$main$0"));
			assertTrue(names.contains("lambda$main$1"));
			if (bytecodeVersion() < Opcodes.V15) {
				// references to private constructor,
				// private static, private bound and
				// private unbound methods
				assertTrue(names.contains("lambda$main$2"));
				assertTrue(names.contains("lambda$main$3"));
				assertTrue(names.contains("lambda$main$4"));
				assertTrue(names.contains("lambda$main$5"));
				assertTrue(names.contains("lambda$main$6"));
				assertTrue(names.contains("lambda$main$7"));
				assertTrue(names.contains("lambda$main$8"));
				assertTrue(names.contains("lambda$main$9"));
				assertEquals(10, names.size());
				if (bytecodeVersion() < Opcodes.V11) {
					// accessor methods used by above lambda methods
					assertTrue(accessors.contains("access$000"));
					assertTrue(accessors.contains("access$100"));
					assertEquals(2, accessors.size());
				} else {
					// JEP 181: Nest-Based Access Control
					assertEquals(0, accessors.size());
				}
			} else {
				assertEquals(2, names.size());
				assertEquals(0, accessors.size());
			}
		} else {
			// references to array constructor,
			// and private constructor
			assertTrue(names.contains("lambda$2"));
			assertTrue(names.contains("lambda$3"));
			assertTrue(names.contains("lambda$4"));
			// note that ECJ unlike javac performs
			// deduplication of references to array constructor
			assertEquals(3, names.size());
			// indy instructions for other method references use generated
			// accessor methods directly without intermediate lambda methods
			// unlike it is with javac
			assertTrue(accessors.contains("access$0"));
			assertTrue(accessors.contains("access$1"));
			assertEquals(2, accessors.size());
		}
		assertMethodCount( //
				names.size() // lambdas
						+ 4 // constructors
						+ 5 // methods
		);
	}

}
