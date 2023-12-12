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
package org.jacoco.report;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link FileMultiReportOutput}.
 */
public class FileMultiReportOutputTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testCreateFileWithDirectories() throws IOException {
		final IMultiReportOutput output = new FileMultiReportOutput(
				folder.getRoot());
		final OutputStream stream = output.createFile("a/b/c/test");
		stream.write(1);
		stream.write(2);
		stream.write(3);
		stream.close();
		output.close();

		final InputStream actual = new FileInputStream(
				new File(folder.getRoot(), "a/b/c/test"));
		assertEquals(1, actual.read());
		assertEquals(2, actual.read());
		assertEquals(3, actual.read());
		assertEquals(-1, actual.read());
		actual.close();
	}

	@Test(expected = IOException.class)
	public void testCreateFileNegative() throws IOException {
		folder.newFile("a");
		final IMultiReportOutput output = new FileMultiReportOutput(
				folder.getRoot());
		output.createFile("a/b/c/test");
	}

}
