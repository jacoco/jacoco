/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import org.jacoco.agent.rt.internal.CoverageTransformer.InstrumenterFactory;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.instr.ClassInstrumenter;
import org.jacoco.core.internal.instr.IProbeArrayStrategy;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;
import org.objectweb.asm.ClassVisitor;
import org.pavelreich.saaremaa.tmetrics.TestMetricsCollector;

/**
 * The agent which is referred as the <code>Premain-Class</code>. The agent
 * configuration is provided with the agent parameters in the command line.
 */
public final class PreMain {

	private PreMain() {
		// no instances
	}

	/**
	 * TODO: move it to my own jagent. gather test metrics
	 *
	 * @author preich
	 *
	 */
	private static final class TestMetricsGatheringPreMainBuilder
			extends PremainBuilder {
		final IExceptionLogger exceptionLogger = IExceptionLogger.SYSTEM_ERR;

		@Override
		protected Runnable createShutdownAction() {
			return new Runnable() {

				@Override
				public void run() {
					try {
						TestMetricsCollector.dumpTestingArtifacts();
					} catch (final Exception e) {
						exceptionLogger.logExeption(e);
					}
				}

			};
		}

		@Override
		public ClassFileTransformer createTransformer(final IRuntime runtime,
				final AgentOptions agentOptions) {
			final InstrumenterFactory instrumenterFactory = new TracingInstrumenterFactory(
					agentOptions, exceptionLogger);
			final CoverageTransformer transformer = new CoverageTransformer(
					runtime, agentOptions, exceptionLogger,
					instrumenterFactory);
			return transformer;
		}
	}

	static class TracingInstrumenterFactory implements InstrumenterFactory {
		private final String jacocoDestFileName;
		private final IExceptionLogger exceptionLogger;

		public TracingInstrumenterFactory(final AgentOptions agentOptions,
				final IExceptionLogger exceptionLogger) {
			this.jacocoDestFileName = agentOptions.getDestfile();
			this.exceptionLogger = exceptionLogger;
		}

		@Override
		public Instrumenter create(final IRuntime runtime) {
			return new Instrumenter(runtime) {
				@Override
				protected ClassInstrumenter createClassInstrumenter(
						final ClassVisitor writer,
						final IProbeArrayStrategy strategy) {
					final ClassVisitor visitor = chainVisitor(writer);
					return super.createClassInstrumenter(visitor, strategy);
				}

			};
		}

		protected ClassVisitor chainVisitor(final ClassVisitor nextVisitor) {
			final ClassVisitor visitor = TestMetricsCollector
					.provideClassVisitor(nextVisitor, jacocoDestFileName,
							exceptionLogger);
			return visitor;
		}
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

		final PremainBuilder premainBuilder = new TestMetricsGatheringPreMainBuilder();
		premainBuilder.premain(options, inst);
	}

}
