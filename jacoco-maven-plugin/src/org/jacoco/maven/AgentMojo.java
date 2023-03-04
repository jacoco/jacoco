/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

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
 * If your project already defines VM arguments for test execution, be sure that
 * they will include property defined by JaCoCo.
 * </p>
 *
 * <p>
 * One of the ways to do this in case of maven-surefire-plugin - is to use
 * syntax for <a href=
 * "http://maven.apache.org/surefire/maven-surefire-plugin/faq.html#late-property-evaluation">late
 * property evaluation</a>:
 * </p>
 *
 * <pre>
 *   &lt;plugin&gt;
 *     &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
 *     &lt;artifactId&gt;maven-surefire-plugin&lt;/artifactId&gt;
 *     &lt;configuration&gt;
 *       &lt;argLine&gt;@{argLine} -your -extra -arguments&lt;/argLine&gt;
 *     &lt;/configuration&gt;
 *   &lt;/plugin&gt;
 * </pre>
 *
 * <p>
 * You can define empty property to avoid JVM startup error
 * <code>Could not find or load main class @{argLine}</code> when using late
 * property evaluation and jacoco-maven-plugin not executed.
 * </p>
 *
 * <p>
 * Another way is to define "argLine" as a Maven property rather than as part of
 * the configuration of maven-surefire-plugin:
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
 *       &lt;!-- no argLine here --&gt;
 *     &lt;/configuration&gt;
 *   &lt;/plugin&gt;
 * </pre>
 *
 * <p>
 * Resulting coverage information is collected during execution and by default
 * written to a file when the process terminates.
 * </p>
 *
 * @since 0.5.3
 */
@Mojo(name = "prepare-agent", defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class AgentMojo extends AbstractAgentMojo {

	/**
	 * Path to the output file for execution data.
	 */
	@Parameter(property = "jacoco.destFile", defaultValue = "${project.build.directory}/jacoco.exec")
	private File destFile;

	/**
	 * @return the destFile
	 */
	@Override
	File getDestFile() {
		return destFile;
	}

}
