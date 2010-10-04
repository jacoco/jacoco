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
 *******************************************************************************/
package org.jacoco.report;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link DirectorySourceFileLocator}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class DirectorySourceFileLocatorTest {

	private DirectorySourceFileLocator locator;

	@Before
	public void setup() {
		locator = new DirectorySourceFileLocator(new File("./src"), "UTF-8");
	}

	@Test
	public void testGetSourceFileNegative() throws IOException {
		assertNull(locator.getSourceFile("org.jacoco.somewhere",
				"DoesNotExist.java"));
	}

	@Test
	public void testGetSourceFile() throws IOException {
		final Reader source = locator.getSourceFile("org/jacoco/report",
				"DirectorySourceFileLocatorTest.java");
		final BufferedReader reader = new BufferedReader(source);
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains("testGetSourceFile()")) {
				return;
			}
		}
		fail("Unexpected source content.");
	}

}
