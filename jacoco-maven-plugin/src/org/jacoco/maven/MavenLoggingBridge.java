/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.plugin.logging.Log;
import org.jacoco.core.tools.LoggingBridge;

/**
 * A bridge between the generic jacoco core logging and the maven logging.
 */
class MavenLoggingBridge implements LoggingBridge {
	private final Log log;

	public MavenLoggingBridge(final Log log) {
		this.log = log;
	}

	public void info(final String msg) {
		log.info(msg);
	}

	public void warning(final String msg) {
		log.warn(msg);
	}

	public void severe(final String msg) {
		log.error(msg);
	}
}