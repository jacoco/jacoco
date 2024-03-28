/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link DTDLoader}.
 */
public class DTDLoaderTest {

	private String dtd;

	@Before
	public void setup() throws IOException {
		dtd = DTDLoader.load();
	}

	@Test
	public void dtd_should_be_one_line() {
		assertFalse(dtd.contains("\n"));
		assertFalse(dtd.contains("\r"));
	}

	@Test
	public void dtd_should_not_contain_comments() {
		assertFalse(dtd.contains("<!--"));
		assertFalse(dtd.contains("-->"));
	}

	@Test
	public void dtd_should_not_contain_multiple_blanks() {
		assertFalse(dtd.contains("  "));
	}

	@Test
	public void dtd_should_be_trimmed() {
		assertEquals(dtd.trim(), dtd);
	}
}
