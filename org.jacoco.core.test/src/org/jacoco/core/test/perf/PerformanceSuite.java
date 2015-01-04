/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.perf;

import java.io.PrintWriter;

import org.jacoco.core.test.perf.targets.Target01;
import org.jacoco.core.test.perf.targets.Target02;
import org.jacoco.core.test.perf.targets.Target03;

/**
 * The main test suite.
 */
public class PerformanceSuite implements IPerfScenario {

	public void run(IPerfOutput output) throws Exception {
		new ExecuteInstrumentedCodeScenario("plain method calls",
				Target01.class).run(output);
		new ExecuteInstrumentedCodeScenario("loop only", Target02.class)
				.run(output);
		new ExecuteInstrumentedCodeScenario("game of life", Target03.class)
				.run(output);
		new InstrumentationSizeSzenario(Target03.class).run(output);
		new InstrumentationTimeScenario(Target03.class, 1000).run(output);
		new AnalysisTimeScenario(Target03.class, 1000).run(output);
	}

	public static void main(String[] args) throws Exception {
		final PrintWriter writer;
		if (args.length == 0) {
			writer = new PrintWriter(System.out, true);
		} else {
			writer = new PrintWriter(args[0]);
		}
		IPerfOutput output = new PerfOutputWriter(writer);
		new PerformanceSuite().run(output);
		writer.close();
	}

}
