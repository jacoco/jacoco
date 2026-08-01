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
package org.jacoco.ant;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;

/**
 * Simulates behavior of <a href=
 * "https://github.com/jboss-modules/jboss-modules/blob/2.3.0/src/main/java/org/jboss/modules/Main.java">JBoss
 * Modules</a> that in contrast to {@link java.lang.instrument normal agent
 * execution} loads and executes agents using non
 * {@link ClassLoader#getSystemClassLoader() system class loader}.
 * <p>
 * This class must be declared and executed as both
 * {@link #premain(String, Instrumentation) Premain-Class} and
 * {@link #main(String[]) Main-Class}, so that first execution can capture
 * instance of {@link Instrumentation} used by second.
 * </p>
 */
public final class AgentLoader {

	private static Instrumentation instrumentation;

	/** Captures instance of {@link Instrumentation}. */
	public static void premain(final String agentArgs,
			final Instrumentation instrumentation) {
		AgentLoader.instrumentation = instrumentation;
	}

	/**
	 * Loads and executes {@code Premain-Class} from JAR file passed as first
	 * argument using new (<strong>non system</strong>) class loader.
	 */
	public static void main(final String[] args) throws Exception {
		final URLClassLoader classLoader = new URLClassLoader(
				new URL[] { new File(args[0]).toURI().toURL() });
		final Manifest manifest = new Manifest();
		manifest.read(
				classLoader.findResource("META-INF/MANIFEST.MF").openStream());
		final Class<?> agentClass = classLoader.loadClass(
				manifest.getMainAttributes().getValue("Premain-Class"));
		final Method premain = agentClass.getDeclaredMethod("premain",
				String.class, Instrumentation.class);
		premain.invoke(null, "", instrumentation);
	}

}
