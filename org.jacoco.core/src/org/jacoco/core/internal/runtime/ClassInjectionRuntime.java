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
package org.jacoco.core.internal.runtime;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.runtime.AbstractRuntime;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * {@link IRuntime} that uses introduced in Java 9
 * {@code java.lang.instrument.Instrumentation.redefineModule} and
 * {@code java.lang.invoke.MethodHandles.Lookup.defineClass}.
 */
public class ClassInjectionRuntime extends AbstractRuntime {

	private static final String FIELD_NAME = "data";

	private static final String FIELD_TYPE = "Ljava/lang/Object;";

	private final String injectedClassName;

	private final MethodHandle dataFieldSetter;

	/**
	 * Creates new instance of runtime.
	 *
	 * @param instrumentation
	 *            instrumentation interface
	 * @return new runtime instance or <code>null</code> if unable to create
	 */
	public static IRuntime create(final Instrumentation instrumentation) {
		try {
			redefineJavaBaseModule(instrumentation);
			return new ClassInjectionRuntime(Object.class);
		} catch (final Exception e) {
			return null;
		}
	}

	ClassInjectionRuntime(final Class cls) throws Exception {
		this.injectedClassName = cls.getName().replace('.', '/') + "$JaCoCo";
		final Class injectedClass = Lookup //
				.privateLookupIn(cls, Lookup.lookup()) //
				.defineClass(createClass(injectedClassName));
		this.dataFieldSetter = Lookup.lookup().findStaticSetter(injectedClass,
				FIELD_NAME, Object.class);
	}

	public void startup(final RuntimeData data) throws Exception {
		super.startup(data);
		dataFieldSetter.invokeWithArguments(data);
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
		Lookup.lookup() //
				.bind( //
						instrumentation, //
						"redefineModule", //
						MethodType.methodType( //
								void.class, //
								Class.forName("java.lang.Module"), //
								Set.class, //
								Map.class, //
								Map.class, //
								Set.class, //
								Map.class //
						) //
				) //
				.invokeWithArguments( //
						getModule(Object.class), // module
						Collections.emptySet(), // extraReads
						Collections.emptyMap(), // extraExports
						Collections.singletonMap("java.lang",
								Collections.singleton(getModule(
										ClassInjectionRuntime.class))), // extraOpens
						Collections.emptySet(), // extraUses
						Collections.emptyMap() // extraProvides
				);
	}

	/**
	 * @return {@code cls.getModule()}
	 */
	private static Object getModule(final Class cls) throws Exception {
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

		private Lookup(Object instance) {
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
		 * @param obj
		 *            the object from which the method is accessed
		 * @param name
		 *            the name of the method
		 * @param methodType
		 *            the type of the method
		 * @return a method handle
		 */
		MethodHandle bind(final Object obj, final String name,
				final MethodType methodType) throws Exception {
			return new MethodHandle(Class //
					.forName("java.lang.invoke.MethodHandles$Lookup") //
					.getMethod("bind", Object.class, String.class,
							Class.forName("java.lang.invoke.MethodType")) //
					.invoke(this.instance, obj, name, methodType.instance));
		}

		/**
		 * @param refc
		 *            the class or interface from which the method is accessed
		 * @param name
		 *            the field's name
		 * @param type
		 *            the field's type
		 * @return a method handle which can store values into the field
		 */
		MethodHandle findStaticSetter(final Class refc, final String name,
				final Class type) throws Exception {
			return new MethodHandle(Class //
					.forName("java.lang.invoke.MethodHandles$Lookup") //
					.getMethod("findStaticSetter", Class.class, String.class,
							Class.class) //
					.invoke(this.instance, refc, name, type));
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
					.invoke(this.instance, (Object) bytes);
		}

	}

	/**
	 * Provides access to class {@code java.lang.invoke.MethodHandle} introduced
	 * in Java 8.
	 */
	private static class MethodHandle {

		private final Object instance;

		private MethodHandle(Object instance) {
			this.instance = instance;
		}

		/**
		 * @param arguments
		 *            the arguments to pass to the target
		 */
		Object invokeWithArguments(final Object... arguments) throws Exception {
			return Class //
					.forName("java.lang.invoke.MethodHandle") //
					.getMethod("invokeWithArguments", List.class) //
					.invoke(instance, Arrays.asList(arguments));
		}

	}

	/**
	 * Provides access to class {@code java.lang.invoke.MethodType} introduced
	 * in Java 8.
	 */
	private static class MethodType {

		private final Object instance;

		private MethodType(final Object instance) {
			this.instance = instance;
		}

		/**
		 * @param rType
		 *            the return type
		 * @param pTypes
		 *            the parameter types
		 * @return a method type
		 */
		static MethodType methodType(final Class rType, final Class... pTypes)
				throws Exception {
			return new MethodType(Class //
					.forName("java.lang.invoke.MethodType") //
					.getMethod("methodType", Class.class, Class[].class)
					.invoke(null, rType, pTypes));
		}

	}

}
