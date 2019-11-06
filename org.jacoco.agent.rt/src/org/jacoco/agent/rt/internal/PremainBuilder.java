/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pavel Reich
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.InjectedClassRuntime;
import org.jacoco.core.runtime.ModifiedSystemClassRuntime;

/**
 * entry point for premain. You can override createTransformer if necessary.
 *
 * @author preich
 *
 */
public class PremainBuilder {

	public void premain(final String options, final Instrumentation inst)
			throws Exception {
		final AgentOptions agentOptions = new AgentOptions(options);

		final Agent agent = Agent.getInstance(agentOptions,
				createShutdownAction());

		final IRuntime runtime = createRuntime(inst);
		runtime.startup(agent.getData());
		inst.addTransformer(createTransformer(runtime, agentOptions));
	}

	/**
	 * override if you want to run an action before agent.shutdown
	 * 
	 * @return
	 */
	protected Runnable createShutdownAction() {
		return null;
	}

	/**
	 * override if you want to create a custom transformer
	 *
	 *
	 * @param runtime
	 * @param agentOptions
	 * @return
	 */
	public ClassFileTransformer createTransformer(final IRuntime runtime,
			final AgentOptions agentOptions) {
		final CoverageTransformer transformer = new CoverageTransformer(runtime,
				agentOptions, IExceptionLogger.SYSTEM_ERR);
		return transformer;
	}

	private static IRuntime createRuntime(final Instrumentation inst)
			throws Exception {

		if (redefineJavaBaseModule(inst)) {
			return new InjectedClassRuntime(Object.class, "$JaCoCo");
		}

		return ModifiedSystemClassRuntime.createFor(inst,
				"java/lang/UnknownError");
	}

	/**
	 * Opens {@code java.base} module for {@link InjectedClassRuntime} when
	 * executed on Java 9 JREs or higher.
	 *
	 * @return <code>true</code> when running on Java 9 or higher,
	 *         <code>false</code> otherwise
	 * @throws Exception
	 *             if unable to open
	 */
	private static boolean redefineJavaBaseModule(
			final Instrumentation instrumentation) throws Exception {
		try {
			Class.forName("java.lang.Module");
		} catch (final ClassNotFoundException e) {
			return false;
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
						Collections.singleton(
								getModule(InjectedClassRuntime.class))), // extraOpens
				Collections.emptySet(), // extraUses
				Collections.emptyMap() // extraProvides
		);
		return true;
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
