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
package org.jacoco.core.runtime;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Unit test for {@link ClassInjectionRuntime}.
 */
public class ClassInjectionRuntimeTest extends RuntimeTestBase {

	private static IRuntime runtime;

	@BeforeClass
	public static void setupClass() throws Exception {
		try {
			defineJava9Class();
		} catch (final UnsupportedClassVersionError e) {
			throw new AssumptionViolatedException(
					"this test requires at least Java 9");
		}

		runtime = new ClassInjectionRuntime(ClassInjectionRuntimeTest.class);
	}

	@Override
	public IRuntime createRuntime() {
		return runtime;
	}

	@Test
	public void constructor_should_not_create_duplicate_class_definition()
			throws Exception {
		try {
			new ClassInjectionRuntime(ClassInjectionRuntimeTest.class);
			fail("exception expected");
		} catch (final InvocationTargetException e) {
			assertTrue(e.getCause() instanceof LinkageError);
			assertTrue(e.getCause().getMessage()
					.contains("duplicate class definition"));
		}
	}

	private static void defineJava9Class() {
		new ClassLoader() {
			void defineJava9Class() {
				final String name = "Example";
				final ClassWriter cw = new ClassWriter(0);
				cw.visit(Opcodes.V9, 0, name, null, "java/lang/Object", null);
				cw.visitEnd();
				final byte[] bytes = cw.toByteArray();
				defineClass(name, bytes, 0, bytes.length);
			}
		}.defineJava9Class();
	}

}
