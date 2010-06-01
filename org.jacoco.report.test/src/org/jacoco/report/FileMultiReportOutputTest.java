/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link FileMultiReportOutput}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class FileMultiReportOutputTest {

	private File dir;

	@Before
	public void setup() throws IOException {
		dir = File.createTempFile("jacocoTest", null);
		assertTrue(dir.delete());
		assertTrue(dir.mkdirs());
	}

	@After
	public void teardown() {
		dir.delete();
	}

	@Test
	public void testCreateFileWithDirectories() throws IOException {
		final IMultiReportOutput output = new FileMultiReportOutput(dir);
		final OutputStream stream = output.createFile("a/b/c/test");
		stream.write(1);
		stream.write(2);
		stream.write(3);
		stream.close();

		final InputStream actual = new FileInputStream(new File(dir,
				"a/b/c/test"));
		assertEquals(1, actual.read());
		assertEquals(2, actual.read());
		assertEquals(3, actual.read());
		assertEquals(-1, actual.read());
	}

	@Test(expected = IOException.class)
	public void testCreateFileNegative() throws IOException {
		final File d = new File(dir, "a");
		assertTrue(d.createNewFile());
		final IMultiReportOutput output = new FileMultiReportOutput(dir);
		output.createFile("a/b/c/test");
	}
}
