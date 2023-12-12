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
package org.jacoco.report.internal.html.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link SourceFilePage}.
 */
public class SourceFilePageTest extends PageTestBase {

	private Reader sourceReader;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		sourceReader = new InputStreamReader(new FileInputStream(
				"./src/org/jacoco/report/internal/html/page/SourceFilePageTest.java"),
				"UTF-8");
	}

	@Test
	public void testContents() throws Exception {
		final SourceFileCoverageImpl node = new SourceFileCoverageImpl(
				"SourceFilePageTest.java", "org/jacoco/report/internal/html");
		final SourceFilePage page = new SourceFilePage(node, sourceReader, 4,
				null, rootFolder, context);
		page.render();

		final Document result = support
				.parse(output.getFile("SourceFilePageTest.java.html"));

		// additional style sheet
		assertEquals("jacoco-resources/report.css", support.findStr(result,
				"/html/head/link[@rel='stylesheet'][1]/@href"));
		assertEquals("jacoco-resources/prettify.css", support.findStr(result,
				"/html/head/link[@rel='stylesheet'][2]/@href"));

		// highlighting script
		assertEquals("text/javascript",
				support.findStr(result, "/html/head/script/@type"));
		assertEquals("jacoco-resources/prettify.js",
				support.findStr(result, "/html/head/script/@src"));
		assertEquals("window['PR_TAB_WIDTH']=4;prettyPrint()",
				support.findStr(result, "/html/body/@onload"));

		// source code
		assertNotNull(support.findStr(result, "/html/body/pre"));
	}
}
