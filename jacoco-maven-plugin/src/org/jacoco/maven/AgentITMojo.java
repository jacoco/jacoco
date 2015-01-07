/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    Kyle Lieber - implementation of CheckMojo
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;

/**
 * Same as <code>prepare-agent</code>, but provides default values suitable for
 * integration-tests:
 * <ul>
 * <li>bound to <code>pre-integration-test</code> phase</li>
 * <li>different <code>destFile</code></li>
 * </ul>
 * 
 * @phase pre-integration-test
 * @goal prepare-agent-integration
 * @requiresProject true
 * @requiresDependencyResolution runtime
 * @threadSafe
 * @since 0.6.4
 */
public class AgentITMojo extends AbstractAgentMojo {

	/**
	 * Path to the output file for execution data.
	 * 
	 * @parameter property="jacoco.destFile"
	 *            default-value="${project.build.directory}/jacoco-it.exec"
	 */
	private File destFile;

	/**
	 * @return the destFile
	 */
	@Override
	File getDestFile() {
		return destFile;
	}

}
