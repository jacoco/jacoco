/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;

/**
 * Local only agent controller that will write coverage data to the filesystem.
 * This controller uses the following agent options:
 * <ul>
 * <li>destfile</li>
 * <li>append</li>
 * </ul>
 */
public class LocalController implements IAgentController {

	private IRuntime runtime;

	private OutputStream output;

	public final void startup(final AgentOptions options, final IRuntime runtime)
			throws IOException {
		this.runtime = runtime;
		final File destFile = new File(options.getDestfile()).getAbsoluteFile();
		final File folder = destFile.getParentFile();
		if (folder != null) {
			folder.mkdirs();
		}
		output = new BufferedOutputStream(new FileOutputStream(destFile,
				options.getAppend()));
	}

	public void writeExecutionData() throws IOException {
		final ExecutionDataWriter writer = new ExecutionDataWriter(output);
		runtime.collect(writer, writer, false);
	}

	public void shutdown() throws IOException {
		output.close();
	}

}
