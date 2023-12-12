/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Mandrikov Evgeny - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal.output;

import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;

/**
 * Output that does nothing.
 */
public class NoneOutput implements IAgentOutput {

	public final void startup(final AgentOptions options,
			final RuntimeData data) {
		// Nothing to do
	}

	public void writeExecutionData(final boolean reset) {
		// Nothing to do
	}

	public void shutdown() {
		// Nothing to do
	}

}
