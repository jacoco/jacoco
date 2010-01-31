/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.jacoco.core.test.TargetLoader;
import org.junit.Test;

/**
 * Unit tests for {@link ModifiedSystemClassRuntime}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ModifiedSystemClassRuntimeTest extends RuntimeTestBase {

	@Override
	IRuntime createRuntime() {
		return new ModifiedSystemClassRuntime(
				ModifiedSystemClassRuntimeTest.class, "accessMethod",
				"dataField");
	}

	@Test
	public void testCreateFor() throws Exception {
		InstrumentationMock inst = new InstrumentationMock(true);
		ModifiedSystemClassRuntime.createFor(inst, TARGET_CLASS_NAME);
		assertTrue(inst.added);
		assertTrue(inst.removed);
	}

	@Test(expected = RuntimeException.class)
	public void testCreateForNegative() throws Exception {
		InstrumentationMock inst = new InstrumentationMock(false);
		ModifiedSystemClassRuntime.createFor(inst, TARGET_CLASS_NAME);
	}

	// these static members emulate the instrumented system class

	public static Map<Long, boolean[]> dataField;

	public static boolean[] accessMethod(long id) {
		return dataField.get(Long.valueOf(id));
	}

	private static final String TARGET_CLASS_NAME = "org/jacoco/core/runtime/ModifiedSystemClassRuntimeTest";

	private static class InstrumentationMock implements Instrumentation {

		private final boolean doTransform;

		boolean added = false;

		boolean removed = false;

		InstrumentationMock(boolean doTransform) {
			this.doTransform = doTransform;
		}

		public void addTransformer(ClassFileTransformer transformer) {
			assertFalse(added);
			added = true;
			if (doTransform) {
				try {
					// Our class should get instrumented:
					final byte[] data = TargetLoader
							.getClassDataAsBytes(ModifiedSystemClassRuntimeTest.class);
					verifyInstrumentedClass(TARGET_CLASS_NAME, transformer
							.transform(null, TARGET_CLASS_NAME, null, null,
									data));

					// Other classes will not be instrumented:
					assertNull(transformer.transform(getClass()
							.getClassLoader(), "some/other/Class", null, null,
							new byte[0]));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
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

		public void redefineClasses(ClassDefinition[] definitions)
				throws ClassNotFoundException, UnmodifiableClassException {
			fail();
		}
	}

	private static void verifyInstrumentedClass(String name, byte[] source)
			throws Exception {
		name = name.replace('/', '.');
		final Class<?> targetClass = new TargetLoader(name, source)
				.getTargetClass();

		// Check added field:
		final Field f = targetClass.getField("$jacocoData");
		assertEquals(Modifier.PUBLIC | Modifier.STATIC, f.getModifiers(), 0.0);
		assertEquals(Map.class, f.getType());

		// Check added method:
		final Method m = targetClass.getMethod("$jacocoGet", Long.TYPE);
		assertEquals(Modifier.PUBLIC | Modifier.STATIC, m.getModifiers(), 0.0);
		assertEquals(boolean[].class, m.getReturnType());

		// See whether the added code works with the runtime:
		final ModifiedSystemClassRuntime r = new ModifiedSystemClassRuntime(
				targetClass, "$jacocoGet", "$jacocoData");
		r.startup();
		boolean[] data = new boolean[] { true, false, false };
		r.registerClass(5555, "Foo", data);
		assertSame(data, m.invoke(null, Long.valueOf(5555)));
	}

}
