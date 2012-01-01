/*******************************************************************************
 * Copyright (c) Copyright (c) Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.controller;

import java.io.IOException;

/**
 * MBean interface for remote commands to a coverage runtime.
 */
public interface IRuntimeMBean {

	/**
	 * Returns version of JaCoCo.
	 * 
	 * @return version of JaCoCo
	 */
	String getVersion();

	/**
	 * Returns current a session identifier.
	 * 
	 * @return current session identifier
	 */
	String getSessionId();

	/**
	 * Sets a session identifier.
	 * 
	 * @param id
	 *            new session identifier
	 */
	void setSessionId(String id);

	/**
	 * Returns current execution data.
	 * 
	 * @param reset
	 *            if <code>true</code> the current coverage information is also
	 *            cleared
	 * @return dump of current execution data
	 * @throws IOException
	 */
	byte[] dump(boolean reset) throws IOException;

	/**
	 * Resets all coverage information.
	 */
	void reset();

}
