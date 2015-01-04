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
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Restores original classes as they were before offline instrumentation.
 * 
 * @phase prepare-package
 * @goal restore-instrumented-classes
 * @requiresProject true
 * @threadSafe
 * @since 0.6.2
 */
public class RestoreMojo extends AbstractJacocoMojo {

	@Override
	protected void executeMojo() throws MojoExecutionException,
			MojoFailureException {
		final File originalClassesDir = new File(getProject().getBuild()
				.getDirectory(), "generated-classes/jacoco");
		final File classesDir = new File(getProject().getBuild()
				.getOutputDirectory());
		try {
			FileUtils.copyDirectoryStructure(originalClassesDir, classesDir);
		} catch (final IOException e) {
			throw new MojoFailureException("Unable to restore classes.", e);
		}
	}

}
