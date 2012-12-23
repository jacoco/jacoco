/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt;

import static org.junit.Assert.assertTrue;

import org.jacoco.agent.rt.controller.LocalController;
import org.jacoco.agent.rt.controller.MBeanController;
import org.jacoco.agent.rt.controller.TcpClientController;
import org.jacoco.agent.rt.controller.TcpServerController;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.AgentOptions.OutputMode;
import org.junit.Test;

/**
 * Unit tests for {@link Agent}.
 */
public class AgentTest {

	@Test
	public void shouldCreateController() {
		AgentOptions options = new AgentOptions();

		options.setOutput(OutputMode.file);
		Agent agent = new Agent(options, null);
		assertTrue(agent.createAgentController() instanceof LocalController);

		options.setOutput(OutputMode.tcpserver);
		assertTrue(agent.createAgentController() instanceof TcpServerController);

		options.setOutput(OutputMode.tcpclient);
		assertTrue(agent.createAgentController() instanceof TcpClientController);

		options.setOutput(OutputMode.mbean);
		assertTrue(agent.createAgentController() instanceof MBeanController);
	}

}
