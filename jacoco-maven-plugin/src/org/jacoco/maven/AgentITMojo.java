/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
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
 * Prepares a property pointing to the JaCoCo runtime agent that can be passed
 * as a VM argument to the application under test for integration tests.
 * Depending on the project packaging type by default a property with the
 * following name is set:
 * <ul>
 * <li>tycho.testArgLine for packaging type eclipse-test-plugin and</li>
 * <li>argLine otherwise.</li>
 * </ul>
 * Resulting coverage information is collected during execution and by default
 * written to a file when the process terminates.
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
	 * @parameter expression="${jacoco.destFile}"
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
