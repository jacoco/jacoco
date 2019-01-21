/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

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

		if (redefineJavaBaseModule(inst)) {
			return new InjectedClassRuntime(Object.class, "$JaCoCo");
		}

		return ModifiedSystemClassRuntime.createFor(inst, "java/lang/UnknownError");
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
