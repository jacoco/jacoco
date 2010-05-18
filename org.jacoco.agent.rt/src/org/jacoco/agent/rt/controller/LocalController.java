/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 * $Id: $
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
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class LocalController implements IAgentController {

	private File execFile;

	private boolean append;

	private IRuntime runtime;

	public final void startup(final AgentOptions options, final IRuntime runtime) {
		this.runtime = runtime;
		execFile = new File(options.getDestfile()).getAbsoluteFile();
		append = options.getAppend();
		final File folder = execFile.getParentFile();
		if (folder != null) {
			folder.mkdirs();
		}
	}

	public void writeExecutionData() throws IOException {

		final OutputStream output = new BufferedOutputStream(
				new FileOutputStream(execFile, append));
		try {
			final ExecutionDataWriter writer = new ExecutionDataWriter(output);
			runtime.collect(writer, writer, false);
		} finally {
			output.close();
		}
	}

	public void shutdown() throws IOException {
	}

}
