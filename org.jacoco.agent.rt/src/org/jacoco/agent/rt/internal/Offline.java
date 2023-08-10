/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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

	private static final String CONFIG_RESOURCE = "/jacoco-agent.properties";

	private Offline() {
		// no instances
	}

	private static RuntimeData data;

	private static synchronized RuntimeData getRuntimeData() {
		if (data == null) {
			final Properties config = ConfigLoader.load(CONFIG_RESOURCE,
					System.getProperties());
			try {
				data = Agent.getInstance(new AgentOptions(config)).getData();
			} catch (final Exception e) {
				throw new RuntimeException("Failed to initialize JaCoCo.", e);
			}
		}
		return data;
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
		return getRuntimeData()
				.getExecutionData(Long.valueOf(classid), classname, probecount)
				.getProbes();
	}

}
