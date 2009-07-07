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
		StringWriter writer = new StringWriter();
		new XMLDocument("test", null, null, writer).close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><test/>",
				writer.toString());
	}

	@Test
	public void testClose() {

	}

}
