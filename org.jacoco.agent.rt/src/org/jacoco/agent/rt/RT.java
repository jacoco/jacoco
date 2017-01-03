/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt;

import org.jacoco.agent.rt.internal.Agent;

/**
 * Entry point to access the JaCoCo agent runtime.
 */
public final class RT {

	private RT() {
	}

	/**
	 * Returns the agent instance of the JaCoCo runtime in this JVM.
	 * 
	 * @return agent instance
	 * @throws IllegalStateException
	 *             if no Agent has been started yet
	 */
	public static IAgent getAgent() throws IllegalStateException {
		return Agent.getInstance();
	}

}
