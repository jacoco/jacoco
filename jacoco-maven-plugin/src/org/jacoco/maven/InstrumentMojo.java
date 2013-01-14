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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Performs offline instrumentation. Note that after execution of test you must
 * restore original classes with help of "restore-instrumented-classes" goal.
 * 
 * @phase process-classes
 * @goal instrument
 * @requiresProject true
 * @since 0.6.2
 */
public class InstrumentMojo extends AbstractJacocoMojo {

	private static final String WARNING = "The preferred way for code " +
			"coverage analysis with JaCoCo is on-the-fly instrumentation. " +
			"Offline instrumentation has several drawbacks and should only " +
			"be used if a specific scenario explicitly requires this mode. " +
			"Please consult documentation about offline instrumentation " +
			"before using this mode.";

	@Override
	public void executeMojo() throws MojoExecutionException,
			MojoFailureException {
		getLog().warn(WARNING);

		final File originalClassesDir = new File(getProject().getBuild()
				.getDirectory(), "generated-classes/jacoco");
		final File classesDir = new File(getProject().getBuild()
				.getOutputDirectory());

		final List<String> fileNames;
		try {
			fileNames = new FileFilter(this.getIncludes(),
					this.getExcludes()).getFileNames(classesDir);
		} catch (final IOException e1) {
			throw new MojoExecutionException(
					"Unable to get list of files to instrument.", e1);
		}

		final Instrumenter instrumenter = new Instrumenter(
				new OfflineInstrumentationAccessGenerator());
		for (final String fileName : fileNames) {
			if (fileName.endsWith(".class")) {
				final File source = new File(classesDir, fileName);
				final File backup = new File(originalClassesDir, fileName);
				InputStream input = null;
				OutputStream output = null;
				try {
					FileUtils.copyFile(source, backup);
					input = new FileInputStream(backup);
					output = new FileOutputStream(source);
					instrumenter.instrument(input, output);
				} catch (final IOException e2) {
					throw new MojoExecutionException(
							"Unable to instrument file.", e2);
				} finally {
					IOUtil.close(input);
					IOUtil.close(output);
				}
			}
		}
	}

}
