/*******************************************************************************
 * Copyright (c) 2009, 2022 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.runtime;

import org.jacoco.core.internal.InputStreams;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * {@link IRuntime} which defines a new class using
 * {@code java.lang.invoke.MethodHandles.Lookup.defineClass} introduced in Java
 * 9. Module where class will be defined must be opened to at least module of
 * this class.
 */
public class InjectedClassRuntime extends AbstractRuntime {

	private static final String FIELD_NAME = "data";

	private static final String FIELD_TYPE = "Ljava/lang/Object;";

	private final Class<?> locator;

	private final String injectedClassName;

	/**
	 * Creates a new runtime which will define a class to the same class loader
	 * and in the same package and protection domain as given class.
	 *
	 * @param locator
	 *            class to identify the target class loader and package
	 * @param simpleClassName
	 *            simple name of the class to be defined
	 */
	public InjectedClassRuntime(final Class<?> locator,
			final String simpleClassName) {
		this.locator = locator;
		this.injectedClassName = locator.getPackage().getName().replace('.',
				'/') + '/' + simpleClassName;
	}

	/**
	 * Creates a new {@link InjectedClassRuntime} that will define class in the
	 * {@code java.lang} package. To do so this method opens package
	 * {@code java.lang} to the unnamed module of a new ClassLoader that is used
	 * to create an instance of {@link InjectedClassRuntime}, so this <a href=
	 * "https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-5.html#jvms-5.3.6">
	 * unnamed module is distinct from all run-time modules (including unnamed
	 * modules) bound to other class loaders</a>.
	 *
	 * @param instrumentation
	 *            instrumentation interface
	 * @return new runtime instance
	 * @throws Exception
	 *             if unable to create
	 */
	public static IRuntime createFor(final Instrumentation instrumentation)
			throws Exception {
		final ClassLoader classLoader = new ClassLoader() {
			@Override
			protected Class<?> loadClass(String name, boolean resolve)
					throws ClassNotFoundException {
				if (!name.equals(InjectedClassRuntime.class.getName())
						&& !name.equals(Lookup.class.getName())) {
					return super.loadClass(name, resolve);
				}
				final InputStream resourceAsStream = getResourceAsStream(
						name.replace('.', '/') + ".class");
				final byte[] bytes;
				try {
					bytes = InputStreams.readFully(resourceAsStream);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return defineClass(name, bytes, 0, bytes.length);
			}
		};
		Instrumentation.class.getMethod("redefineModule", //
				Class.forName("java.lang.Module"), //
				Set.class, //
				Map.class, //
				Map.class, //
				Set.class, //
				Map.class //
		).invoke(instrumentation, // instance
				Class.class.getMethod("getModule").invoke(Object.class), // module
				Collections.emptySet(), // extraReads
				Collections.emptyMap(), // extraExports
				Collections.singletonMap("java.lang",
						Collections.singleton(
								ClassLoader.class.getMethod("getUnnamedModule")
										.invoke(classLoader))), // extraOpens
				Collections.emptySet(), // extraUses
				Collections.emptyMap() // extraProvides
		);
		return (IRuntime) classLoader
				.loadClass(InjectedClassRuntime.class.getName())
				.getConstructor(Class.class, String.class)
				.newInstance(Object.class, "$JaCoCo");
	}

	@Override
	public void startup(final RuntimeData data) throws Exception {
		super.startup(data);
		Lookup //
				.privateLookupIn(locator, Lookup.lookup()) //
				.defineClass(createClass(injectedClassName)) //
				.getField(FIELD_NAME) //
				.set(null, data);
	}

	public void shutdown() {
		// nothing to do
	}

	public int generateDataAccessor(final long classid, final String classname,
			final int probecount, final MethodVisitor mv) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, injectedClassName, FIELD_NAME,
				FIELD_TYPE);

		RuntimeData.generateAccessCall(classid, classname, probecount, mv);

		return 6;
	}

	private static byte[] createClass(final String name) {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V9, Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC,
				name.replace('.', '/'), null, "java/lang/Object", null);
		cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, FIELD_NAME,
				FIELD_TYPE, null, null);
		cw.visitEnd();
		return cw.toByteArray();
	}

	/**
	 * Provides access to classes {@code java.lang.invoke.MethodHandles} and
	 * {@code java.lang.invoke.MethodHandles.Lookup} introduced in Java 8.
	 */
	private static class Lookup {

		private final Object instance;

		private Lookup(final Object instance) {
			this.instance = instance;
		}

		/**
		 * @return a lookup object for the caller of this method
		 */
		static Lookup lookup() throws Exception {
			return new Lookup(Class //
					.forName("java.lang.invoke.MethodHandles") //
					.getMethod("lookup") //
					.invoke(null));
		}

		/**
		 * See corresponding method introduced in Java 9.
		 *
		 * @param targetClass
		 *            the target class
		 * @param lookup
		 *            the caller lookup object
		 * @return a lookup object for the target class, with private access
		 */
		static Lookup privateLookupIn(final Class<?> targetClass,
				final Lookup lookup) throws Exception {
			return new Lookup(Class //
					.forName("java.lang.invoke.MethodHandles") //
					.getMethod("privateLookupIn", Class.class,
							Class.forName(
									"java.lang.invoke.MethodHandles$Lookup")) //
					.invoke(null, targetClass, lookup.instance));
		}

		/**
		 * See corresponding method introduced in Java 9.
		 *
		 * @param bytes
		 *            the class bytes
		 * @return class
		 */
		Class<?> defineClass(final byte[] bytes) throws Exception {
			return (Class<?>) Class //
					.forName("java.lang.invoke.MethodHandles$Lookup")
					.getMethod("defineClass", byte[].class)
					.invoke(this.instance, new Object[] { bytes });
		}

	}

}
