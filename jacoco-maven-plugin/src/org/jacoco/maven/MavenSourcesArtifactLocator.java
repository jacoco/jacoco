/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Cristiano Costantini
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.artifact.Artifact;
import org.jacoco.report.InputStreamSourceFileLocator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Locator that searches source files in maven sources jar that are found on the
 * maven artifact with the classifier sources.
 */
class MavenSourcesArtifactLocator extends InputStreamSourceFileLocator {

	private final JarFile sourcesJar;

	public MavenSourcesArtifactLocator(final Artifact artifact,
			final String encoding)
					throws IOException {
		super(encoding, 4);
		sourcesJar = new JarFile(searchSourcesJar(artifact));
	}

	private File searchSourcesJar(final Artifact artifact) {
		File sourcesJar = null;
		if (artifact != null && artifact.getFile() != null) {
			final String jarPath = artifact.getFile()
					.getAbsolutePath();

			if (jarPath.endsWith(".jar")) {
				sourcesJar = new File(jarPath.substring(0, jarPath.length() - 4)
						+ "-sources.jar");
			}
		}
		return sourcesJar;
	}

	@Override
	protected InputStream getSourceStream(final String path)
			throws IOException {
		if (sourcesJar != null) {
			final JarEntry jarEntry = sourcesJar.getJarEntry(path);
			if (jarEntry != null) {
				return sourcesJar.getInputStream(jarEntry);
			}
		}
		return null;
	}
}