/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.jar.JarFile;

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
		InstrumentationMock inst = new InstrumentationMock();
		ModifiedSystemClassRuntime.createFor(inst, TARGET_CLASS_NAME);
	}

	/** This static member emulate the instrumented system class. */
	public static Object accessField;

	private static final String TARGET_CLASS_NAME = "org/jacoco/core/runtime/ModifiedSystemClassRuntimeTest";

	private static class InstrumentationMock implements Instrumentation {

		boolean added = false;

		boolean removed = false;

		public void addTransformer(ClassFileTransformer transformer) {
			assertFalse(added);
			added = true;
			try {
				// Our class should get instrumented:
				final byte[] data = TargetLoader
						.getClassDataAsBytes(ModifiedSystemClassRuntimeTest.class);
				verifyInstrumentedClass(TARGET_CLASS_NAME,
						transformer.transform(null, TARGET_CLASS_NAME, null,
								null, data));

				// Other classes will not be instrumented:
				assertNull(transformer.transform(getClass().getClassLoader(),
						"some/other/Class", null, null, new byte[0]));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public boolean removeTransformer(ClassFileTransformer transformer) {
			assertTrue(added);
			assertFalse(removed);
			removed = true;
			return true;
		}

		public Class<?>[] getAllLoadedClasses() {
			fail();
			return null;
		}

		public Class<?>[] getInitiatedClasses(ClassLoader loader) {
			fail();
			return null;
		}

		public long getObjectSize(Object objectToSize) {
			fail();
			return 0;
		}

		public boolean isRedefineClassesSupported() {
			fail();
			return false;
		}

		public void redefineClasses(ClassDefinition[] definitions) {
			fail();
		}

		// JDK 1.6 Methods:

		@SuppressWarnings("unused")
		public void addTransformer(ClassFileTransformer transformer,
				boolean canRetransform) {
			fail();
		}

		@SuppressWarnings("unused")
		public void appendToBootstrapClassLoaderSearch(JarFile jarfile) {
			fail();
		}

		@SuppressWarnings("unused")
		public void appendToSystemClassLoaderSearch(JarFile jarfile) {
			fail();
		}

		@SuppressWarnings("unused")
		public boolean isModifiableClass(Class<?> theClass) {
			fail();
			return false;
		}

		@SuppressWarnings("unused")
		public boolean isNativeMethodPrefixSupported() {
			fail();
			return false;
		}

		@SuppressWarnings("unused")
		public boolean isRetransformClassesSupported() {
			fail();
			return false;
		}

		@SuppressWarnings("unused")
		public void retransformClasses(Class<?>... classes) {
			fail();
		}

		@SuppressWarnings("unused")
		public void setNativeMethodPrefix(ClassFileTransformer transformer,
				String prefix) {
			fail();
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
