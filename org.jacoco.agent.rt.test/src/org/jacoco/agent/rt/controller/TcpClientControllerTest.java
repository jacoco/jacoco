/*******************************************************************************
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

import java.net.ConnectException;

import org.jacoco.agent.rt.ExceptionRecorder;
import org.jacoco.agent.rt.StubRuntime;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;
import org.junit.Test;

/**
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class TcpClientControllerTest {

	@Test(expected = ConnectException.class)
	public void testNoServerListening() throws Exception {
		AgentOptions options = new AgentOptions();
		IRuntime runtime = new StubRuntime();

		TcpClientController c = new TcpClientController(
				ExceptionRecorder.IGNORE_ALL);
		c.startup(options, runtime);
	}
}
