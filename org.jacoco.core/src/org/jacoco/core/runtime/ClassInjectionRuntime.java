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

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * {@link IRuntime} which injects a new class into the <code>java.base</code>
 * module using Java 9 APIs.
 */
public class ClassInjectionRuntime extends AbstractRuntime {

	private static final String FIELD_NAME = "data";

	private static final String FIELD_TYPE = "Ljava/lang/Object;";

	private final Class<?> locator;

	private final String className;

	/**
	 * Creates new instance of runtime.
	 *
	 * @param instrumentation
	 *            instrumentation interface
	 * @return new runtime instance or <code>null</code> if not Java 9
	 */
	public static IRuntime create(final Instrumentation instrumentation) {
		try {
			Class.forName("java.lang.Module");
		} catch (final ClassNotFoundException e) {
			return null;
		}
		try {
			redefineJavaBaseModule(instrumentation);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		return new ClassInjectionRuntime(Object.class, "$JaCoCo");
	}

	/**
	 * Creates a new runtime which will define a class to the same class loader
	 * and in the same package and protection domain as given class.
	 *
	 * @param locator
	 *            class to identify the target class loader and package
	 * @param simpleClassName
	 *            simple name of the class to be defined
	 */
	ClassInjectionRuntime(final Class<?> locator,
			final String simpleClassName) {
		this.locator = locator;
		this.className = locator.getPackage().getName().replace('.', '/') + '/'
				+ simpleClassName;
	}

	@Override
	public void startup(final RuntimeData data) throws Exception {
		super.startup(data);
		Lookup //
				.privateLookupIn(locator, Lookup.lookup()) //
				.defineClass(createClass(className)) //
				.getField(FIELD_NAME) //
				.set(null, data);
	}

	public void shutdown() {
		// nothing to do
	}

	public int generateDataAccessor(final long classid, final String classname,
			final int probecount, final MethodVisitor mv) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, FIELD_NAME, FIELD_TYPE);

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
	 * <pre>
	 * {@code
	 * instrumentation.redefineModule(
	 *   getModule(Object.class),
	 *   Collections.emptySet(),
	 *   Collections.emptyMap(),
	 *   Collections.singletonMap(
	 *     "java.lang",
	 *     Collections.singleton(getModule(ClassInjectionRuntime.class))),
	 *   Collections.emptySet(),
	 *   Collections.emptyMap())
	 * }
	 * </pre>
	 */
	private static void redefineJavaBaseModule(
			final Instrumentation instrumentation) throws Exception {
		Instrumentation.class.getMethod( //
				"redefineModule", //
				Class.forName("java.lang.Module"), //
				Set.class, //
				Map.class, //
				Map.class, //
				Set.class, //
				Map.class //
		).invoke( //
				instrumentation, // instance
				getModule(Object.class), // module
				Collections.emptySet(), // extraReads
				Collections.emptyMap(), // extraExports
				Collections.singletonMap("java.lang",
						Collections.singleton(
								getModule(ClassInjectionRuntime.class))), // extraOpens
				Collections.emptySet(), // extraUses
				Collections.emptyMap() // extraProvides
		);
	}

	/**
	 * @return {@code cls.getModule()}
	 */
	private static Object getModule(final Class<?> cls) throws Exception {
		return Class.class //
				.getMethod("getModule") //
				.invoke(cls);
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