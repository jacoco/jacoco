/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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
 * If your project already uses the argLine to configure the
 * surefire-maven-plugin, be sure that argLine defined as a property, rather
 * than as part of the plugin configuration. For example:
 * </p>
 * 
 * <pre>
 *   &lt;properties&gt;
 *     &lt;argLine&gt;-your -extra -arguments&lt;/argLine&gt;
 *   &lt;/properties&gt;
 *   ...
 *   &lt;plugin&gt;
 *     &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
 *     &lt;artifactId&gt;maven-surefire-plugin&lt;/artifactId&gt;
 *     &lt;configuration&gt;
 *       &lt;!-- Do not define argLine here! --&gt;
 *     &lt;/configuration&gt;
 *   &lt;/plugin&gt;
 * </pre>
 * 
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
	 * @parameter property="jacoco.destFile"
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
