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
package org.jacoco.report.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;

import org.jacoco.report.MemoryMultiReportOutput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ReportOutputFolder}.
 */
public class ReportOutputFolderTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	@Before
	public void setup() {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
	}

	@After
	public void teardown() throws IOException {
		output.close();
		output.assertAllClosed();
	}

	@Test
	public void testFileInRoot() throws IOException {
		root.createFile("test.html").close();
		output.assertSingleFile("test.html");
	}

	@Test
	public void testSubfolderInstance() throws IOException {
		final ReportOutputFolder folder1 = root.subFolder("folder1");
		final ReportOutputFolder folder2 = root.subFolder("folder1");
		assertSame(folder1, folder2);
	}

	@Test
	public void testFileInSubFolder() throws IOException {
		root.subFolder("folderA").subFolder("folderB").createFile("test.html")
				.close();
		output.assertSingleFile("folderA/folderB/test.html");
		output.close();
		output.assertAllClosed();
	}

	@Test
	public void testRelativeLinkInSameFolder() throws IOException {
		final ReportOutputFolder base = root.subFolder("f1").subFolder("f2");
		assertEquals("test.html", base.getLink(base, "test.html"));
	}

	@Test
	public void testRelativeLinkInParentFolder() throws IOException {
		final ReportOutputFolder base = root.subFolder("f1").subFolder("f2");
		assertEquals("../../test.html", root.getLink(base, "test.html"));
	}

	@Test
	public void testRelativeLinkInSubFolder() throws IOException {
		final ReportOutputFolder folder = root.subFolder("f1").subFolder("f2");
		assertEquals("f1/f2/test.html", folder.getLink(root, "test.html"));
	}

	@Test
	public void testRelativeLinkInSibling1() throws IOException {
		final ReportOutputFolder folder = root.subFolder("f1").subFolder("f2");
		final ReportOutputFolder base = root.subFolder("g1").subFolder("g2");
		assertEquals("../../f1/f2/test.html",
				folder.getLink(base, "test.html"));
	}

	@Test
	public void testRelativeLinkInSibling2() throws IOException {
		final ReportOutputFolder folder = root.subFolder("f1").subFolder("f2");
		final ReportOutputFolder base = root.subFolder("f1").subFolder("g2");
		assertEquals("../f2/test.html", folder.getLink(base, "test.html"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidRelativeLink() throws IOException {
		final ReportOutputFolder folder = root.subFolder("f1").subFolder("f2");
		final ReportOutputFolder base = new ReportOutputFolder(
				new MemoryMultiReportOutput()).subFolder("g1");
		folder.getLink(base, "test.html");
	}
}
