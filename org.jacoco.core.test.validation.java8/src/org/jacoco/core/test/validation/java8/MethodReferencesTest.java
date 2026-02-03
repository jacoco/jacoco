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

		if (isJDKCompiler) {
			assertTrue(names.contains("lambda$main$0"));
			assertTrue(names.contains("lambda$main$1"));
			if (bytecodeVersion() < Opcodes.V15) {
				assertTrue(names.contains("lambda$main$2"));
				assertTrue(names.contains("lambda$main$3"));
				assertTrue(names.contains("lambda$main$4"));
				assertTrue(names.contains("lambda$main$5"));
				assertEquals(6, names.size());
			} else {
				assertEquals(2, names.size());
			}
		} else {
			assertTrue(names.contains("lambda$2"));
			assertTrue(names.contains("lambda$3"));
			assertTrue(names.contains("lambda$8"));
			assertEquals(3, names.size());
		}
		assertMethodCount(/* constructors */ 4 + /* methods */ 3
				+ /* lambdas */ names.size());
	}

}
