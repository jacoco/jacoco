/*******************************************************************************
 * Copyright (c) Copyright (c) Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.jacoco.agent.rt.StubRuntime;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link LocalController}.
 */
public class LocalControllerTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testWriteData() throws Exception {
		File destFile = folder.newFile("jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setDestfile(destFile.getAbsolutePath());

		IRuntime runtime = new StubRuntime();

		LocalController controller = new LocalController();
		controller.startup(options, runtime);
		controller.writeExecutionData();
		controller.shutdown();

		assertTrue("Execution data file should be created", destFile.exists());
	}

	@Test(expected = IOException.class)
	public void testInvalidDestFile() throws Exception {
		AgentOptions options = new AgentOptions();
		options.setDestfile(folder.newFolder("folder").getAbsolutePath());
		IRuntime runtime = new StubRuntime();
		LocalController controller = new LocalController();

		// Startup should fail as the file can not be created:
		controller.startup(options, runtime);
	}

}
