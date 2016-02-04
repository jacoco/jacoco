/*******************************************************************************
 *  Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.core.WorkloadAttributeMapBuilder;
import org.junit.Test;

/**
 * 
 */
public class EmpiricalBigOFileLoaderTest {
	private static final File NULL_FILE = null;

	@Test
	public void testConstructor_default() {
		EmpiricalBigOFileLoader instance = new EmpiricalBigOFileLoader();
		EmpiricalBigOWorkloadStore result = instance.getWorkloadstore();
		assertEquals(0, result.size());
		assertEquals(WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE,
				result.getDefaultAttribute());
		assertEquals(1, result.getRequiredAttributes().size());
	}

	@Test
	public void testConstructor_null() {
		EmpiricalBigOFileLoader instance = new EmpiricalBigOFileLoader(null);
		EmpiricalBigOWorkloadStore result = instance.getWorkloadstore();
		assertEquals(0, result.size());
		assertEquals(WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE,
				result.getDefaultAttribute());
		assertEquals(1, result.getRequiredAttributes().size());
	}

	@Test
	public void testConstructor() {
		EmpiricalBigOFileLoader instance = new EmpiricalBigOFileLoader("OMER");
		EmpiricalBigOWorkloadStore result = instance.getWorkloadstore();
		assertEquals(0, result.size());
		assertEquals("OMER", result.getDefaultAttribute());
		assertEquals(1, result.getRequiredAttributes().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLoad_nullArg() throws IOException {
		EmpiricalBigOFileLoader instance = new EmpiricalBigOFileLoader();
		instance.load(NULL_FILE);
	}

	@Test(expected = FileNotFoundException.class)
	public void testLoad_noSuchFile() throws IOException {
		EmpiricalBigOFileLoader instance = new EmpiricalBigOFileLoader();
		instance.load(new File("/no/such/file/at/all"));
	}

	@Test
	public void testLoad_emptyDirectory() throws IOException {
		File testDir = File.createTempFile("test", ".dir");
		testDir.delete();
		testDir.mkdirs();
		testDir.deleteOnExit();
		EmpiricalBigOFileLoader instance = new EmpiricalBigOFileLoader();
		instance.load(testDir);
		assertEquals(0, instance.getWorkloadstore().size());
	}

	@Test
	public void testLoad_goodFile_noMap() throws IOException {
		String path = EmpiricalBigOFileLoaderTest.class.getResource(
				"../data/nomap/sample1.exec").getPath();
		EmpiricalBigOFileLoader instance = new EmpiricalBigOFileLoader();

		try {
			instance.load(new File(path));
			fail("Failed to throw FileNotFoundException");
		} catch (FileNotFoundException e) {
		}
		assertEquals(0, instance.getWorkloadstore().size());
	}

	@Test
	public void testLoad_goodFile_hasMap() throws IOException {
		String path = EmpiricalBigOFileLoaderTest.class.getResource("../data")
				.getPath();
		EmpiricalBigOFileLoader instance = new EmpiricalBigOFileLoader();

		instance.load(new File(path));

		assertEquals(2, instance.getWorkloadstore().size());
	}
}