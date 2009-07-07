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
package org.jacoco.report.html;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link HTMLElement}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class HTMLElementTest {

	private StringWriter buffer;

	private HTMLElement root;

	@Before
	public void setUp() throws IOException {
		buffer = new StringWriter();
		root = new HTMLElement(buffer, "root") {
			{
				beginOpenTag();
			}
		};
	}

	@Test
	public void testTitle() throws IOException {
		root.title();
		root.close();
		assertEquals("<root><title/></root>", buffer.toString());
	}

	@Test
	public void testSpan() throws IOException {
		root.span("abc");
		root.close();
		assertEquals("<root><span class=\"abc\"/></root>", buffer.toString());
	}

	@Test
	public void testPre() throws IOException {
		root.pre("mystyle");
		root.close();
		assertEquals("<root><pre class=\"mystyle\"/></root>", buffer.toString());
	}

	@Test
	public void testBr() throws IOException {
		root.br();
		root.close();
		assertEquals("<root><br/></root>", buffer.toString());
	}

}
