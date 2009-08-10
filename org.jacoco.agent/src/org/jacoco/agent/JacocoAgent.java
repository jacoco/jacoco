/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;

import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;

/**
 * The agent which is referred as the <code>Premain-Class</code>.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class JacocoAgent {

	public static void premain(final String agentArgs,
			final Instrumentation inst) {

		final AgentOptions options = new AgentOptions(agentArgs);
		final IRuntime runtime = new LoggerRuntime();
		runtime.startup();

		inst.addTransformer(new CoverageTransformer(runtime, options));

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				writeExecutionData(runtime, options);
			}
		});
	}

	private static void writeExecutionData(IRuntime runtime,
			AgentOptions options) {
		try {
			File execFile = new File(options.getFile()).getAbsoluteFile();
			File folder = execFile.getParentFile();
			if (folder != null) {
				folder.mkdirs();
			}
			OutputStream output = new FileOutputStream(execFile, options
					.getMerge());
			runtime.collect(new ExecutionDataWriter(output), false);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
