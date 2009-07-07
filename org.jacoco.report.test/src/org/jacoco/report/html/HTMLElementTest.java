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
	public void testHead() throws IOException {
		root.head();
		root.close();
		assertEquals("<root><head/></root>", buffer.toString());
	}

	@Test
	public void testTitle() throws IOException {
		root.title();
		root.close();
		assertEquals("<root><title/></root>", buffer.toString());
	}

	@Test
	public void testBody() throws IOException {
		root.body();
		root.close();
		assertEquals("<root><body/></root>", buffer.toString());
	}

}
