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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.jacoco.core.runtime.AbstractRuntime;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class LocalAgentControllerTest {

	private File coverageFile;

	@Before
	public void setUp() throws Exception {
		coverageFile = File.createTempFile("jacoco", "tmp");
	}

	@After
	public void tearDown() {
		coverageFile.delete();
	}

	@Test
	public void testWriteData() throws Exception {
		AgentOptions options = new AgentOptions();
		options.setDestfile(coverageFile.getAbsolutePath());

		IRuntime runtime = new AbstractRuntime() {

			public int generateDataAccessor(long classid, String classname,
					int probecount, MethodVisitor mv) {
				return 0;
			}

			public void startup() throws Exception {
			}

			public void shutdown() {
			}
		};

		LocalController controller = new LocalController();
		controller.startup(options, runtime);
		controller.writeExecutionData();
		controller.shutdown();

		assertTrue("Coverage file should be created", coverageFile.exists());
	}
}
