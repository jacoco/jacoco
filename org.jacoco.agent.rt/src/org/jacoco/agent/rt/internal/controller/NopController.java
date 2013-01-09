/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mandrikov Evgeny - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal.controller;

import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;

/**
 * Controller that does nothing.
 */
public class NopController implements IAgentController {

	public final void startup(final AgentOptions options, final RuntimeData data) {
		// Nothing to do
	}

	public void writeExecutionData(final boolean reset) {
		// Nothing to do
	}

	public void shutdown() {
		// Nothing to do
	}

}
