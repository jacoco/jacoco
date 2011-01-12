/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Unit tests for {@link FileSingleReportOutput}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class FileSingleReportOutputTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testCreateFileWithDirectories() throws IOException {
		final File f = new File(folder.getRoot(), "a/b/c/test");

		final ISingleReportOutput output = new FileSingleReportOutput(f);
		final OutputStream stream = output.createFile();
		stream.write(1);
		stream.write(2);
		stream.write(3);
		stream.close();

		final InputStream actual = new FileInputStream(f);
		assertEquals(1, actual.read());
		assertEquals(2, actual.read());
		assertEquals(3, actual.read());
		assertEquals(-1, actual.read());
	}

	@Test(expected = IOException.class)
	public void testCreateFileNegative() throws IOException {
		final File d = folder.newFile("a");
		final File f = new File(d, "b/c/test");
		final ISingleReportOutput output = new FileSingleReportOutput(f);
		output.createFile();
	}

}
