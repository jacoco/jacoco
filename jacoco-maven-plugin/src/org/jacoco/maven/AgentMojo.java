/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;

/**
 * <p>
 * Prepares a property pointing to the JaCoCo runtime agent that can be passed
 * as a VM argument to the application under test. Depending on the project
 * packaging type by default a property with the following name is set:
 * </p>
 * 
 * <ul>
 * <li>tycho.testArgLine for packaging type eclipse-test-plugin and</li>
 * <li>argLine otherwise.</li>
 * </ul>
 * 
 * <p>
 * Note that these properties must not be overwritten by the test configuration,
 * otherwise the JaCoCo agent cannot be attached. If you need custom parameters
 * please append them. For example:
 * </p>
 * 
 * <pre>
 *   &lt;argLine&gt;${argLine} -your -extra -arguments&lt;/argLine&gt;
 * </pre>
 * 
 * <p>
 * Resulting coverage information is collected during execution and by default
 * written to a file when the process terminates.
 * </p>
 * 
 * @phase initialize
 * @goal prepare-agent
 * @requiresProject true
 * @requiresDependencyResolution runtime
 * @threadSafe
 * @since 0.5.3
 */
public class AgentMojo extends AbstractAgentMojo {

	/**
	 * Path to the output file for execution data.
	 * 
	 * @parameter expression="${jacoco.destFile}"
	 *            default-value="${project.build.directory}/jacoco.exec"
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
