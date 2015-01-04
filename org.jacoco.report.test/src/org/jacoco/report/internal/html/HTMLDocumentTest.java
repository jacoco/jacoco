/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal.html;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

/**
 * Unit tests for {@link HTMLDocument}.
 */
public class HTMLDocumentTest {

	@Test
	public void testWriter() throws IOException {
		StringWriter buffer = new StringWriter();
		new HTMLDocument(buffer, "UTF-8").close();
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
						+ "<html xmlns=\"http://www.w3.org/1999/xhtml\"/>",
				buffer.toString());
	}

	@Test
	public void testStream() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		new HTMLDocument(buffer, "UTF-8").close();
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
						+ "<html xmlns=\"http://www.w3.org/1999/xhtml\"/>",
				buffer.toString("UTF-8"));
	}

	@Test
	public void testHead() throws IOException {
		StringWriter buffer = new StringWriter();
		final HTMLDocument doc = new HTMLDocument(buffer, "UTF-8");
		doc.head();
		doc.close();
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
						+ "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head/></html>",
				buffer.toString());
	}

	@Test
	public void testBody() throws IOException {
		StringWriter buffer = new StringWriter();
		final HTMLDocument doc = new HTMLDocument(buffer, "UTF-8");
		doc.body();
		doc.close();
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
						+ "<html xmlns=\"http://www.w3.org/1999/xhtml\"><body/></html>",
				buffer.toString());
	}

	@Test
	public void testMinimalHTMLDocument() throws Exception {
		StringWriter buffer = new StringWriter();
		final HTMLDocument doc = new HTMLDocument(buffer, "UTF-8");
		doc.head().title();
		doc.body();
		doc.close();
		new HTMLSupport().parse(buffer.toString());
	}

}
