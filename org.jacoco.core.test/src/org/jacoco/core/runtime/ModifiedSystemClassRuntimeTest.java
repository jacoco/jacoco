/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import org.jacoco.core.test.TargetLoader;
import org.junit.Test;

/**
 * Unit tests for {@link ModifiedSystemClassRuntime}.
 */
public class ModifiedSystemClassRuntimeTest extends RuntimeTestBase {

	@Override
	IRuntime createRuntime() {
		return new ModifiedSystemClassRuntime(
				ModifiedSystemClassRuntimeTest.class, "accessField");
	}

	@Test(expected = RuntimeException.class)
	public void testCreateForNegative() throws Exception {
		Instrumentation inst = newInstrumentationMock();
		ModifiedSystemClassRuntime.createFor(inst, TARGET_CLASS_NAME);
	}

	/** This static member emulate the instrumented system class. */
	public static Object accessField;

	private static final String TARGET_CLASS_NAME = "org/jacoco/core/runtime/ModifiedSystemClassRuntimeTest";

	/**
	 * Note that we use Proxy here to mock {@link Instrumentation}, because JDK
	 * 9 adds new method "addModule", whose parameter depends on class
	 * "java.lang.reflect.Module" introduced in JDK 9.
	 */
	private Instrumentation newInstrumentationMock() {
		return (Instrumentation) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[] { Instrumentation.class },
				new MyInvocationHandler());
	}

	private static class MyInvocationHandler implements InvocationHandler {
		boolean added = false;

		boolean removed = false;

		/**
		 * {@link Instrumentation#addTransformer(ClassFileTransformer)}
		 */
		void addTransformer(ClassFileTransformer transformer) {
			assertFalse(added);
			added = true;
			try {
				// Our class should get instrumented:
				final byte[] data = TargetLoader.getClassDataAsBytes(
						ModifiedSystemClassRuntimeTest.class);
				verifyInstrumentedClass(TARGET_CLASS_NAME,
						transformer.transform((ClassLoader) null,
								TARGET_CLASS_NAME, null, null, data));

				// Other classes will not be instrumented:
				assertNull(transformer.transform(getClass().getClassLoader(),
						"some/other/Class", null, null, new byte[0]));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * {@link Instrumentation#removeTransformer(ClassFileTransformer)}
		 */
		Boolean removeTransformer() {
			assertTrue(added);
			assertFalse(removed);
			removed = true;
			return Boolean.TRUE;
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (args.length == 1) {
				if ("removeTransformer".equals(method.getName())) {
					return removeTransformer();
				} else if ("addTransformer".equals(method.getName())) {
					addTransformer((ClassFileTransformer) args[0]);
					return null;
				}
			}
			fail();
			return null;
		}
	}

	private static void verifyInstrumentedClass(String name, byte[] source)
			throws Exception {
		name = name.replace('/', '.');
		final Class<?> targetClass = new TargetLoader().add(name, source);

		// Check added field:
		final Field f = targetClass.getField("$jacocoAccess");
		assertTrue(Modifier.isPublic(f.getModifiers()));
		assertTrue(Modifier.isStatic(f.getModifiers()));
		assertTrue(Modifier.isTransient(f.getModifiers()));
		assertEquals(Object.class, f.getType());
	}
}
