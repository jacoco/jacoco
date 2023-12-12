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
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.FileUtils;

/**
 * Restores original classes as they were before offline instrumentation.
 *
 * @since 0.6.2
 */
@Mojo(name = "restore-instrumented-classes", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class RestoreMojo extends AbstractJacocoMojo {

	@Override
	protected void executeMojo()
			throws MojoExecutionException, MojoFailureException {
		final File originalClassesDir = new File(
				getProject().getBuild().getDirectory(),
				"generated-classes/jacoco");
		final File classesDir = new File(
				getProject().getBuild().getOutputDirectory());
		try {
			FileUtils.copyDirectoryStructure(originalClassesDir, classesDir);
		} catch (final IOException e) {
			throw new MojoFailureException("Unable to restore classes.", e);
		}
	}

}
