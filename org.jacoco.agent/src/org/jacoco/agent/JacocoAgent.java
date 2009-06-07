package org.jacoco.agent;

import java.lang.instrument.Instrumentation;

import org.jacoco.core.data.ExecutionDataDumper;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.SystemPropertiesRuntime;

public class JacocoAgent {

	public static void premain(String agentArgs, final Instrumentation inst) {
		final IRuntime runtime = new SystemPropertiesRuntime();
		runtime.startup();

		inst.addTransformer(new CoverageTransformer(runtime));

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				runtime.collect(new ExecutionDataDumper(System.out), false);
			}

		});
	}
}
