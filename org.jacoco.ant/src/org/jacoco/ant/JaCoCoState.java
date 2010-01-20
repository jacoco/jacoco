/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
package org.jacoco.ant;

import java.io.File;
import java.io.IOException;

import org.jacoco.agent.AgentJar;

/**
 * Static class for holding the state of the JaCoCo agent
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class JaCoCoState {
	private static File agentFile;

	/**
	 * Gets the current location of the JaCoCo agent jar file. The agent jar may
	 * be extracted to a temporary location
	 * 
	 * @return Physical location of the JaCoCo Agent Jar
	 * @throws IOException
	 *             Unable to extract the agent jar file
	 */
	public static synchronized File getAgentFile() throws IOException {
		if (agentFile == null) {
			agentFile = AgentJar.extractToTempLocation();
		}

		return agentFile;
	}
}
