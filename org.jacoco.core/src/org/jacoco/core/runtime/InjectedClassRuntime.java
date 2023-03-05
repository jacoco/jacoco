/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
