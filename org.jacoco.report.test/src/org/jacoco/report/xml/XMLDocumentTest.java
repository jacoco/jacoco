/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
package org.jacoco.report.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

/**
 * Unit tests for {@link XMLDocument}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class XMLDocumentTest {

	@Test
	public void testNoDoctype() throws IOException {
	}

	@Test
	public void testDoctype() throws IOException {
		StringWriter writer = new StringWriter();
		new XMLDocument("test", "sample", "sample.dtd", "UTF-8", writer)
				.close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE test PUBLIC \"sample\" \"sample.dtd\">"
				+ "<test/>", writer.toString());
	}

	@Test
	public void testStream() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		new XMLDocument("test", null, null, "UTF-8", buffer).text(
				"\u00CD\u307e").close();
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><test>\u00CD\u307e</test>",
				buffer.toString("UTF-8"));
	}

	@Test
	public void testClose() throws IOException {
		class CloseVerifier extends StringWriter {

			boolean closed = false;

			@Override
			public void close() throws IOException {
				closed = true;
				super.close();
			}
		}
		CloseVerifier verifier = new CloseVerifier();
		new XMLDocument("test", null, null, "UTF-8", verifier).close();
		assertTrue(verifier.closed);
	}

}
