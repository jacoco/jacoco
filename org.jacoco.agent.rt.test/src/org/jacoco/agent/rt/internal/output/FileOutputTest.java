/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.test.validation.JavaVersion;
import org.junit.AssumptionViolatedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link FileOutput}.
 */
public class FileOutputTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void startup_should_create_empty_execfile() throws Exception {
		File destFile = folder.newFile("jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setDestfile(destFile.getAbsolutePath());

		FileOutput controller = new FileOutput();
		controller.startup(options, new RuntimeData());

		assertTrue("Execution data file should be created", destFile.exists());
		assertEquals("Execution data file should be empty", 0,
				destFile.length());
	}

	@Test
	public void writeExecutionData_should_write_execdata() throws Exception {
		File destFile = folder.newFile("jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setDestfile(destFile.getAbsolutePath());

		FileOutput controller = new FileOutput();
		controller.startup(options, new RuntimeData());
		controller.writeExecutionData(false);
		controller.shutdown();

		assertTrue("Execution data file should be created", destFile.exists());
		assertTrue("Execution data file should have contents",
				destFile.length() > 0);
	}

	@Test
	public void startup_should_throw_IOException_when_execfile_cannot_be_created()
			throws Exception {
		AgentOptions options = new AgentOptions();
		options.setDestfile(folder.newFolder("folder").getAbsolutePath());
		FileOutput controller = new FileOutput();

		try {
			controller.startup(options, new RuntimeData());
			fail("IOException expected");
		} catch (IOException e) {
			// expected
		}
	}

	@Test
	public void startup_should_throw_OverlappingFileLockException_when_execfile_is_permanently_locked()
			throws Exception {
		if (JavaVersion.current().isBefore("1.6")) {
			throw new AssumptionViolatedException(
					"OverlappingFileLockException only thrown since Java 1.6");
		}

		File destFile = folder.newFile("jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setDestfile(destFile.getAbsolutePath());
		// Note that due to https://bugs.openjdk.org/browse/JDK-8166253
		// (fixed in JDK version 11)
		// reference to lock object must be maintained
		// till the end of this test
		// to guarantee that observation of OverlappingFileLockException
		// does not depend on GC in JDK versions from 6 to 10
		FileLock lock = new FileOutputStream(destFile).getChannel().lock();
		FileOutput controller = new FileOutput();

		try {
			controller.startup(options, new RuntimeData());
			fail("OverlappingFileLockException expected");
		} catch (OverlappingFileLockException e) {
			// expected
		} finally {
			lock.channel().close();
		}
	}

	@Test
	public void startup_should_throw_InterruptedIOException_when_execfile_is_locked_and_thread_is_interrupted()
			throws Exception {
		if (JavaVersion.current().isBefore("1.6")) {
			throw new AssumptionViolatedException(
					"OverlappingFileLockException only thrown since Java 1.6");
		}

		File destFile = folder.newFile("jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setDestfile(destFile.getAbsolutePath());
		// Note that due to https://bugs.openjdk.org/browse/JDK-8166253
		// (fixed in JDK version 11)
		// reference to lock object must be maintained
		// till the end of this test
		// to guarantee that observation of OverlappingFileLockException
		// does not depend on GC in JDK versions from 6 to 10
		FileLock lock = new FileOutputStream(destFile).getChannel().lock();
		FileOutput controller = new FileOutput();
		Thread.currentThread().interrupt();

		try {
			controller.startup(options, new RuntimeData());
			fail("InterruptedIOException expected");
		} catch (InterruptedIOException e) {
			// expected
		} finally {
			lock.channel().close();
		}
	}

}
