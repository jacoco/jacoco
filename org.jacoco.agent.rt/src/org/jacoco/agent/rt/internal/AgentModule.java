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
 *    Marc R. Hoffmann - move to separate class
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.internal.InputStreams;

/**
 * An isolated class loader and distinct module to encapsulate JaCoCo runtime
 * classes. This isolated environment allows to specifically open JDK packages
 * to the agent runtime without changing package accessibility for the
 * application under test.
 * <p>
 * The implementation uses the property that the <a href=
 * "https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-5.html#jvms-5.3.6">
 * unnamed module is distinct from all run-time modules (including unnamed
 * modules) bound to other class loaders</a>.
 */
public class AgentModule {

	/**
	 * Checks whether Java modules are supported by the current Java runtime.
	 *
	 * @return <code>true</code> is modules are supported
	 */
	public static boolean isSupported() {
		try {
			getModuleClass();
		} catch (final ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	private final Set<String> scope = new HashSet<String>();
	private final ClassLoader classLoader;

	/**
	 * Creates a new isolated module.
	 *
	 * @throws Exception
	 *             if it cannot be created
	 */
	public AgentModule() throws Exception {
		classLoader = new ClassLoader() {
			@Override
			protected Class<?> loadClass(final String name,
					final boolean resolve) throws ClassNotFoundException {
				if (!scope.contains(name)) {
					return super.loadClass(name, resolve);
				}
				final InputStream resourceAsStream = getResourceAsStream(
						name.replace('.', '/') + ".class");
				final byte[] bytes;
				try {
					bytes = InputStreams.readFully(resourceAsStream);
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
				return defineClass(name, bytes, 0, bytes.length,
						AgentModule.class.getProtectionDomain());
			}
		};
	}

	/**
	 * Opens the package of the provided class to the module created in this
	 * {@link #AgentModule()} instance.
	 *
	 * @param instrumentation
	 *            service provided to the agent by the Java runtime
	 * @param classInPackage
	 *            example class of the package to open
	 * @throws Exception
	 *             if package cannot be opened
	 */
	public void openPackage(final Instrumentation instrumentation,
			final Class<?> classInPackage) throws Exception {

		// module of the package to open
		final Object module = Class.class.getMethod("getModule")
				.invoke(classInPackage);

		// unnamed module of our classloader
		final Object unnamedModule = ClassLoader.class
				.getMethod("getUnnamedModule").invoke(classLoader);

		// Open package java.lang to the unnamed module of our class loader
		Instrumentation.class.getMethod("redefineModule", //
				getModuleClass(), //
				Set.class, //
				Map.class, //
				Map.class, //
				Set.class, //
				Map.class //
		).invoke(instrumentation, // instance
				module, // module
				Collections.emptySet(), // extraReads
				Collections.emptyMap(), // extraExports
				Collections.singletonMap(classInPackage.getPackage().getName(),
						Collections.singleton(unnamedModule)), // extraOpens
				Collections.emptySet(), // extraUses
				Collections.emptyMap() // extraProvides
		);
	}

	/**
	 * Loads a copy of the given class in the isolated classloader. Also any
	 * inner classes are loader from the isolated classloader.
	 *
	 * @param <T>
	 *            type of the class to load
	 * @param original
	 *            original class definition which is used as source
	 * @return class object from the isolated class loader
	 * @throws Exception
	 *             if the class cannot be loaded
	 */
	@SuppressWarnings("unchecked")
	public <T> Class<T> loadClassInModule(final Class<T> original)
			throws Exception {
		addToScopeWithInnerClasses(original);
		return (Class<T>) classLoader.loadClass(original.getName());
	}

	private void addToScopeWithInnerClasses(final Class<?> c) {
		scope.add(c.getName());
		for (final Class<?> i : c.getDeclaredClasses()) {
			addToScopeWithInnerClasses(i);
		}
	}

	private static Class<?> getModuleClass() throws ClassNotFoundException {
		return Class.forName("java.lang.Module");
	}

}
