/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Same as <code>prepare-agent</code>, but provides default values suitable for
 * integration-tests:
 * <ul>
 * <li>bound to <code>pre-integration-test</code> phase</li>
 * <li>different <code>destFile</code></li>
 * </ul>
 * 
 * @since 0.6.4
 */
@Mojo(name = "prepare-agent-integration", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class AgentITMojo extends AbstractAgentMojo {

	/**
	 * Path to the output file for execution data.
	 */
	@Parameter(property = "jacoco.destFile", defaultValue = "${project.build.directory}/jacoco-it.exec")
	private File destFile;

	/**
	 * @return the destFile
	 */
	@Override
	File getDestFile() {
		return destFile;
	}

}
