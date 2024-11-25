/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.ant;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;

public final class AgentLoader {

	private static Instrumentation instrumentation;

	public static void premain(final String args,
			final Instrumentation instrumentation) {
		AgentLoader.instrumentation = instrumentation;
	}

	public static void main(final String[] args) throws IOException,
			ClassNotFoundException, InvocationTargetException,
			IllegalAccessException, NoSuchMethodException {
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
