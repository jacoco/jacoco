/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.After;
import org.junit.Test;

/**
 * Unit tests for {@link AgentJar}.
 */
public class AgentJarTest {

	private File file;

	@After
	public void teardown() {
		if (file != null) {
			file.delete();
		}
	}

	@Test
	public void testGetResource() throws IOException {
		final InputStream in = AgentJar.getResource().openStream();
		assertAgentContents(in);
	}

	@Test
	public void testGetResourceAsStream() throws IOException {
		final InputStream in = AgentJar.getResourceAsStream();
		assertAgentContents(in);
	}

	@Test
	public void testExtractTo() throws IOException {
		file = File.createTempFile("agent", ".jar");
		AgentJar.extractTo(file);
		assertAgentContents(new FileInputStream(file));
	}

	@Test(expected = IOException.class)
	public void testExtractToNegative() throws IOException {
		file = File.createTempFile("folder", null);
		file.delete();
		file.mkdirs();
		AgentJar.extractTo(file);
	}

	@Test
	public void testExtractToTempLocation() throws IOException {
		file = AgentJar.extractToTempLocation();
		assertAgentContents(new FileInputStream(file));
		file.delete();
	}

	private void assertAgentContents(InputStream in) throws IOException {
		final ZipInputStream zip = new ZipInputStream(in);
		while (true) {
			final ZipEntry entry = zip.getNextEntry();
			assertNotNull("Manifest not found.", entry);
			if ("META-INF/MANIFEST.MF".equals(entry.getName())) {
				final Manifest manifest = new Manifest(zip);
				assertEquals("JaCoCo Java Agent", manifest.getMainAttributes()
						.getValue("Implementation-Title"));
				in.close();
				break;
			}
		}
	}

}
