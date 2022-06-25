/*******************************************************************************
 * Copyright (c) 2009, 2022 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.agent.rt.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.InjectedClassRuntime;
import org.jacoco.core.runtime.ModifiedSystemClassRuntime;

/**
 * The agent which is referred as the <code>Premain-Class</code>. The agent
 * configuration is provided with the agent parameters in the command line.
 */
public final class PreMain {

	private PreMain() {
		// no instances
	}

	/**
	 * This method is called by the JVM to initialize Java agents.
	 *
	 * @param options
	 *            agent options
	 * @param inst
	 *            instrumentation callback provided by the JVM
	 * @throws Exception
	 *             in case initialization fails
	 */
	public static void premain(final String options, final Instrumentation inst)
			throws Exception {

		final AgentOptions agentOptions = new AgentOptions(options);

		final Agent agent = Agent.getInstance(agentOptions);

		final IRuntime runtime = createRuntime(inst);
		runtime.startup(agent.getData());
		inst.addTransformer(new CoverageTransformer(runtime, agentOptions,
				IExceptionLogger.SYSTEM_ERR));
	}

	private static IRuntime createRuntime(final Instrumentation inst)
			throws Exception {

		final IRuntime injectedClassRuntime = createInjectedClassRuntime(inst);
		if (injectedClassRuntime != null) {
			return injectedClassRuntime;
		}

		return ModifiedSystemClassRuntime.createFor(inst,
				"java/lang/UnknownError");
	}

	private static IRuntime createInjectedClassRuntime(
			final Instrumentation instrumentation) throws Exception {
		try {
			Class.forName("java.lang.Module");
		} catch (final ClassNotFoundException e) {
			return null;
		}
		final IRuntime runtime = (IRuntime) (new ClassLoader() {
			@Override
			public Class<?> loadClass(String name)
					throws ClassNotFoundException {
				if (!name.startsWith(InjectedClassRuntime.class.getName())) {
					return super.loadClass(name);
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
		}.loadClass(InjectedClassRuntime.class.getName())
				.getConstructor(Class.class, String.class)
				.newInstance(Object.class, "$JaCoCo"));
		final Object module = getModule(runtime.getClass());
		if (module.equals(getModule(PreMain.class))) {
			throw new IllegalStateException();
		}
		Instrumentation.class.getMethod("redefineModule", //
				Class.forName("java.lang.Module"), //
				Set.class, //
				Map.class, //
				Map.class, //
				Set.class, //
				Map.class //
		).invoke(instrumentation, // instance
				getModule(Object.class), // module
				Collections.emptySet(), // extraReads
				Collections.emptyMap(), // extraExports
				Collections.singletonMap("java.lang",
						Collections.singleton(module)), // extraOpens
				Collections.emptySet(), // extraUses
				Collections.emptyMap() // extraProvides
		);
		return runtime;
	}

	/**
	 * @return {@code cls.getModule()}
	 */
	private static Object getModule(final Class<?> cls) throws Exception {
		return Class.class //
				.getMethod("getModule") //
				.invoke(cls);
	}

}
