/*******************************************************************************
 * Copyright (c) 2010 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.agent;

import java.io.InputStream;
import java.net.URL;

/**
 * API to access the agent JAR file as a resource.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class AgentJar {

	/**
	 * Name of the agent JAR file resource within this bunde.
	 */
	public static final String RESOURCE = "/jacocoagent.jar";

	private AgentJar() {
	}

	/**
	 * Returns a URL pointing to the JAR file.
	 * 
	 * @return URL of the JAR file
	 */
	public static URL getResource() {
		return AgentJar.class.getResource(RESOURCE);
	}

	/**
	 * Returns the content of the JAR file as a stream.
	 * 
	 * @return content of the JAR file
	 */
	public static InputStream getResourceAsStream() {
		return AgentJar.class.getResourceAsStream(RESOURCE);
	}

}
