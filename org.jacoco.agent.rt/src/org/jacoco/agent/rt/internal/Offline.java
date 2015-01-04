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
package org.jacoco.agent.rt.internal;

import java.util.Properties;

import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;

/**
 * The API for classes instrumented in "offline" mode. The agent configuration
 * is provided through system properties prefixed with <code>jacoco.</code>.
 */
public final class Offline {

	private static final RuntimeData DATA;
	private static final String CONFIG_RESOURCE = "/jacoco-agent.properties";

	static {
		final Properties config = ConfigLoader.load(CONFIG_RESOURCE,
				System.getProperties());
		DATA = Agent.getInstance(new AgentOptions(config)).getData();
	}

	private Offline() {
		// no instances
	}

	/**
	 * API for offline instrumented classes.
	 * 
	 * @param classid
	 *            class identifier
	 * @param classname
	 *            VM class name
	 * @param probecount
	 *            probe count for this class
	 * @return probe array instance for this class
	 */
	public static boolean[] getProbes(final long classid,
			final String classname, final int probecount) {
		return DATA.getExecutionData(Long.valueOf(classid), classname,
				probecount).getProbes();
	}

}
